package guru.sfg.beer.order.service.services.listener;

import guru.sfg.beer.order.service.config.JMSConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.ActionResult;
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
        log.debug("Received validation result for order: %, as isValid: %",actionResult.getId(),actionResult.getIsSuccessful());
        beerOrderManager.processValidationResult(actionResult.getId(), actionResult.getIsSuccessful());
    }
}
