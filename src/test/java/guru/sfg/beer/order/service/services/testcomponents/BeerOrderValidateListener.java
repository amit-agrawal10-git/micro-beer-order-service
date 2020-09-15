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
        System.out.println("I RAN.................: "+beerOrderDto);
        ActionResult actionResult = ActionResult.builder()
                .isSuccessful(true)
                .id(beerOrderDto.getId()).build();
        jmsTemplate.convertAndSend(JMSConfig.VALIDATE_ORDER_RESPONSE_QUEUE,actionResult);
    }

}