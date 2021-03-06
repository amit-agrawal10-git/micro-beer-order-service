package com.github.amag.beerorderservice.services.beer;

import com.github.amag.beerorderservice.bootstrap.BeerOrderBootStrap;
import com.github.amag.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@Disabled // utility for manual testing
@SpringBootTest
class BeerServiceRestTemplateImplTest {

    @Autowired
    BeerService beerService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void getBeerDtoByUPC() {
        Optional<BeerDto> beerDto = beerService.getBeerDtoByUPC(BeerOrderBootStrap.BEER_2_UPC);
            if(beerDto.isPresent())
            {
                System.out.println(beerDto.get());
            };
    }

/*
    @Test
    void getBeerDtoById() {
        Optional<BeerDto> beerDto = beerService.getBeerDtoById(BeerOrderBootStrap.);
        if(beerDto.isPresent())
        {
            System.out.println(beerDto.get());
        };
    }
*/
}