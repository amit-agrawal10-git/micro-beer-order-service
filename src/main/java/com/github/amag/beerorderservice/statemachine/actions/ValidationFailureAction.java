package com.github.amag.beerorderservice.statemachine.actions;

import com.github.amag.beerorderservice.domain.BeerOrderEventEnum;
import com.github.amag.beerorderservice.domain.BeerOrderStatusEnum;
import com.github.amag.beerorderservice.repositories.BeerOrderRepository;
import com.github.amag.beerorderservice.services.BeerOrderManagerImpl;
import com.github.amag.beerorderservice.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        Object orderId = stateContext.getMessageHeader(BeerOrderManagerImpl.ORDER_ID_HEADER);
        log.error("Order validation failed for orderId: "+orderId.toString());
    }
}
