package com.github.amag.beerorderservice.services;

import com.github.amag.beerorderservice.domain.BeerOrder;
import com.github.amag.beerorderservice.domain.BeerOrderEventEnum;
import com.github.amag.beerorderservice.domain.BeerOrderStatusEnum;
import com.github.amag.beerorderservice.interceptors.OrderStateChangeInterceptor;
import com.github.amag.beerorderservice.repositories.BeerOrderRepository;
import com.github.amag.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.amag.beerorderservice.domain.BeerOrderStatusEnum.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    public static final String ORDER_ID_HEADER = "order_id";
    private final OrderStateChangeInterceptor orderStateChangeInterceptor;

    @Override
    @Transactional
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER, VALIDATION_PENDING);
        return savedBeerOrder;
    }

    @Override
    public void pickUpBeerOrder(BeerOrder beerOrder) {
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICKED_UP, PICKED_UP);
    }

    @Override
    public void cancelBeerOrder(UUID beerOrder) {
        beerOrderRepository.findById(beerOrder).ifPresentOrElse(order -> {
            sendBeerOrderEvent(order, BeerOrderEventEnum.CANCEL_ORDER, CANCELLED);
                }
        ,() -> log.error("cancelBeerOrder: Order with order id: "+beerOrder));
    }

    @Override
    @Transactional
    public void processValidationResult(UUID id, Boolean isValid) {
        log.debug("Processing validation result for order id: "+id);
        BeerOrder order = beerOrderRepository.getOne(id);
        log.debug("beerOrderOptional.order??: "+order.toString());
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(id);
        log.debug("beerOrderOptional.isPresent??: "+beerOrderOptional.isPresent());

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            if(isValid){
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED, VALIDATED);

                BeerOrder validatedOrder = beerOrderRepository.findById(id).get();
                sendBeerOrderEvent(validatedOrder,BeerOrderEventEnum.ALLOCATE_ORDER, ALLOCATION_PENDING);
                log.debug("Processes validation result for order id: "+validatedOrder.getId());

            } else {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED, VALIDATION_EXCEPTION);
            }

        }, () -> log.error("processValidationResult: Order with order id: "+id.toString()+" not found"));
    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS, ALLOCATED);
            updateAllocatedQuantity(beerOrderDto);
        }, () -> log.error("beerOrderAllocationPassed: Order with order id: "+beerOrderDto.getId().toString()+" not found"));
    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY, PENDING_INVENTORY);
        updateAllocatedQuantity(beerOrderDto);
        }, () -> log.error("beerOrderAllocationPendingInventory: Order with order id: "+beerOrderDto.getId().toString()+" not found"));

    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED, ALLOCATION_EXCEPTION);
        }, () -> log.error("beerOrderAllocationFailed: Order with order id: "+beerOrderDto.getId().toString()+" not found"));
    }

    private void updateAllocatedQuantity(BeerOrderDto beerOrderDto)
    {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            beerOrder.getBeerOrderLines().forEach( orderLine -> beerOrderDto.getBeerOrderLines().forEach(dtoOrderLine -> {
            if(dtoOrderLine.getId().equals(orderLine.getId())){
                orderLine.setQuantityAllocated(dtoOrderLine.getQuantityAllocated());
            }
        }));
                    beerOrderRepository.saveAndFlush(beerOrder);
        }, () -> log.error("updateAllocatedQuantity: Order with order id: "+beerOrderDto.getId().toString()+" not found"));
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum, BeerOrderStatusEnum targetStatusEnum ){
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);
        Message message
                = MessageBuilder.withPayload(eventEnum)
                .setHeader(ORDER_ID_HEADER,beerOrder.getId().toString())
                .build();
        stateMachine.sendEvent(message);
        awaitForStatus(beerOrder.getId(), targetStatusEnum);
    }

    private void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum statusEnum) {

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop Retries exceeded");
            }

            beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
                if (statusEnum.equals(beerOrder.getOrderStatus())) {
                    found.set(true);
                    log.debug("Order Found");
                } else {
                    log.debug("Order Status Not Equal. Expected: " + statusEnum.name() + " Found: " + beerOrder.getOrderStatus().name());
                }
            }, () -> {
                log.debug("Order Id Not Found");
            });

            if (!found.get()) {
                try {
                    log.debug("Sleeping for retry");
                    Thread.sleep(100);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }

    }


    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder){
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = stateMachineFactory.getStateMachine(beerOrder.getId());
        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(
                        sma -> {
                            sma.addStateMachineInterceptor(orderStateChangeInterceptor);
                            sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null,null,null));
                        }
                );
        stateMachine.start();
        return stateMachine;
    }
}
