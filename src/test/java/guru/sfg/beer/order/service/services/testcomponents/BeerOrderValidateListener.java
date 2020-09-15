package guru.sfg.beer.order.service.services.testcomponents;

import guru.sfg.beer.order.service.config.JMSConfig;
import guru.sfg.brewery.model.ActionResult;
import guru.sfg.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
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
        Boolean isValid = true;
        Boolean sendResponse = true;

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
