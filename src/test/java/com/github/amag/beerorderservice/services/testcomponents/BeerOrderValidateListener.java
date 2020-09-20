package com.github.amag.beerorderservice.services.testcomponents;

import com.github.amag.beerorderservice.config.JMSConfig;
import com.github.amag.model.ActionResult;
import com.github.amag.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BeerOrderValidateListener {

    public final JmsTemplate jmsTemplate;

    @JmsListener(destination = JMSConfig.VALIDATE_ORDER_REQUEST_QUEUE)
    public void listener(BeerOrderDto beerOrderDto){
        boolean isValid = true;
        boolean sendResponse = true;

        if(beerOrderDto.getCustomerRef().equals("failed-validation"))
            isValid = false;
        if(beerOrderDto.getCustomerRef().equals("dont-validate"))
            sendResponse = false;

        ActionResult actionResult = ActionResult.builder()
                .isSuccessful(isValid)
                .id(beerOrderDto.getId()).build();
        if(sendResponse)
        jmsTemplate.convertAndSend(JMSConfig.VALIDATE_ORDER_RESPONSE_QUEUE,actionResult);
    }

}
