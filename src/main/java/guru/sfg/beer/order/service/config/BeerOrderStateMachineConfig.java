package guru.sfg.beer.order.service.config;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class BeerOrderStateMachineConfig extends StateMachineConfigurerAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> states) throws Exception {
        states.withStates()
                .initial(BeerOrderStatusEnum.NEW)
                .states(EnumSet.allOf(BeerOrderStatusEnum.class))
                .end(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
                .end(BeerOrderStatusEnum.ALLOCATION_EXCEPTION)
                .end(BeerOrderStatusEnum.DELIVERY_EXCEPTION)
                .end(BeerOrderStatusEnum.DELIVERED)
                .end(BeerOrderStatusEnum.PICKED_UP);

    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> transitions) throws Exception {
        transitions.withExternal()

                .source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.NEW).event(BeerOrderEventEnum.VALIDATE_ORDER)
                .source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.VALIDATED).event(BeerOrderEventEnum.VALIDATION_PASSED)
                .source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.VALIDATION_EXCEPTION).event(BeerOrderEventEnum.VALIDATION_FAILED)

                .source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.ALLOCATED).event(BeerOrderEventEnum.ALLOCATION_SUCCESS)
                .source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.PENDING_INVENTORY).event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)
                .source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.ALLOCATION_EXCEPTION).event(BeerOrderEventEnum.ALLOCATION_FAILED)

                .source(BeerOrderStatusEnum.PENDING_INVENTORY).target(BeerOrderStatusEnum.ALLOCATED).event(BeerOrderEventEnum.ALLOCATION_SUCCESS)
                .source(BeerOrderStatusEnum.PENDING_INVENTORY).target(BeerOrderStatusEnum.PENDING_INVENTORY).event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)
                .source(BeerOrderStatusEnum.PENDING_INVENTORY).target(BeerOrderStatusEnum.ALLOCATION_EXCEPTION).event(BeerOrderEventEnum.ALLOCATION_FAILED)

                .source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.PICKED_UP).event(BeerOrderEventEnum.BEERORDER_PICKED_UP)
          //      .source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.DELIVERED).event(BeerOrderEventEnum.BEERORDER_PICKED_UP)
          //      .source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.DELIVERY_EXCEPTION).event(BeerOrderEventEnum.BEERORDER_PICKED_UP)
;

    }
}
