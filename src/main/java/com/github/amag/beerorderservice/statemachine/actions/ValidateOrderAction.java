package com.github.amag.beerorderservice.statemachine.actions;

import com.github.amag.beerorderservice.services.BeerOrderManagerImpl;
import com.github.amag.beerorderservice.config.JMSConfig;
import com.github.amag.beerorderservice.domain.BeerOrder;
import com.github.amag.beerorderservice.domain.BeerOrderEventEnum;
import com.github.amag.beerorderservice.domain.BeerOrderStatusEnum;
import com.github.amag.beerorderservice.repositories.BeerOrderRepository;
import com.github.amag.beerorderservice.web.mappers.BeerOrderMapper;
import com.github.amag.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        log.debug("send validate order was called");
        UUID orderId = UUID.fromString(stateContext.getMessageHeader(BeerOrderManagerImpl.ORDER_ID_HEADER).toString());
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(orderId);

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            BeerOrderDto beerOrderDto = beerOrderMapper.beerOrderToDto(beerOrder);
            jmsTemplate.convertAndSend(JMSConfig.VALIDATE_ORDER_REQUEST_QUEUE,beerOrderDto);
        }, () ->
                log.error("Order Not Found Id: "+ orderId.toString())
        );

        log.debug("Sent validation request to queue for order id: "+ orderId.toString());
    }
}
