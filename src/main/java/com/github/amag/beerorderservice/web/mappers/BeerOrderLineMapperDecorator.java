package com.github.amag.beerorderservice.web.mappers;

import com.github.amag.beerorderservice.domain.BeerOrderLine;
import com.github.amag.model.BeerOrderLineDto;
import com.github.amag.beerorderservice.services.beer.BeerService;
import com.github.amag.model.BeerDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {
private BeerOrderLineMapper beerOrderLineMapper;
private BeerService beerService;

    @Autowired
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);
        Optional<BeerDto> optionalBeerDto = beerService.getBeerDtoByUPC(line.getUpc());
        optionalBeerDto.ifPresent(
                beerDto -> {
                    beerOrderLineDto.setBeerName(beerDto.getBeerName());
                    beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
                    beerOrderLineDto.setPrice(beerDto.getPrice());
                }
        );
        return beerOrderLineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        return beerOrderLineMapper.dtoToBeerOrderLine(dto);
    }

}
