package com.github.amag.beerorderservice.services.listener;

import com.github.amag.beerorderservice.config.JMSConfig;
import com.github.amag.beerorderservice.services.BeerOrderManager;
import com.github.amag.model.BeerOrderDto;
import com.github.amag.model.events.AllocationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AllocateResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JMSConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocationResult allocationResult){
        BeerOrderDto beerOrderDto = allocationResult.getBeerOrderDto();

        if(allocationResult.getErrorOccurred()){
            beerOrderManager.beerOrderAllocationFailed(beerOrderDto);
        } else if (allocationResult.getPartialAllocation()) {
            beerOrderManager.beerOrderAllocationPendingInventory(beerOrderDto);
        } else {
            beerOrderManager.beerOrderAllocationPassed(beerOrderDto);
        }
        log.debug("Received allocation result; "+allocationResult);
    }
}
