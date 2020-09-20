package com.github.amag.beerorderservice.services.listener;

import com.github.amag.beerorderservice.services.BeerOrderManager;
import com.github.amag.model.ActionResult;
import com.github.amag.beerorderservice.config.JMSConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidateResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JMSConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ActionResult actionResult){
        log.debug("Received validation result for order: "+actionResult.getId()+" as isValid: "+actionResult.getIsSuccessful());
        beerOrderManager.processValidationResult(actionResult.getId(), actionResult.getIsSuccessful());
    }
}
