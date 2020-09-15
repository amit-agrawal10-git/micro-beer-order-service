package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocationResult;

import java.util.UUID;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void processValidationResult(UUID id, Boolean isValid);
    void beerOrderAllocationPassed(BeerOrderDto beerOrderDto);
    void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto);
    void beerOrderAllocationFailed(BeerOrderDto beerOrderDto);
}