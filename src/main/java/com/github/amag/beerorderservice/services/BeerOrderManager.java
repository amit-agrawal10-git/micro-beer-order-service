package com.github.amag.beerorderservice.services;

import com.github.amag.beerorderservice.domain.BeerOrder;
import com.github.amag.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);
    void pickUpBeerOrder(BeerOrder beerOrder);
    void cancelBeerOrder(UUID beerOrder);
    void processValidationResult(UUID id, Boolean isValid);
    void beerOrderAllocationPassed(BeerOrderDto beerOrderDto);
    void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto);
    void beerOrderAllocationFailed(BeerOrderDto beerOrderDto);
}
