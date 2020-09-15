package guru.sfg.beer.order.service.services.testcomponents;

import guru.sfg.beer.order.service.config.JMSConfig;
import guru.sfg.brewery.model.ActionResult;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BeerOrderAllocateListener {

    public final JmsTemplate jmsTemplate;

    @JmsListener(destination = JMSConfig.ALLOCATE_ORDER_REQUEST_QUEUE)
    public void listener(BeerOrderDto beerOrderDto){
        Boolean sendResponse = true;

        if(beerOrderDto.getCustomerRef().equals("dont-allocate"))
            sendResponse = false;

        beerOrderDto.getBeerOrderLines().get(0).setQuantityAllocated(beerOrderDto.getBeerOrderLines().get(0).getOrderQuantity());
        AllocationResult allocationResult = AllocationResult.builder()
                .beerOrderDto(beerOrderDto)
                .errorOccurred(false)
                .partialAllocation(false)
                .build();
        if(sendResponse)
        jmsTemplate.convertAndSend(JMSConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,allocationResult);
    }


}
