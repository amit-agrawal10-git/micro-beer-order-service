package com.github.amag.beerorderservice.services.beer;

import com.github.amag.model.BeerDto;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {
    Optional<BeerDto> getBeerDtoByUPC(String upc);
    Optional<BeerDto> getBeerDtoById(UUID uuid);
}
