package guru.sfg.beer.order.service.services.beer;

import guru.sfg.brewery.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = true)
@Service
public class BeerServiceRestTemplateImpl implements BeerService {

    public static final String BEER_PATH_V1 = "/api/v1/beer/";
    public static final String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/";
    private final RestTemplate restTemplate;

    private String beerServiceHost;

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }

    public BeerServiceRestTemplateImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Optional<BeerDto> getBeerDtoByUPC(String upc) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_UPC_PATH_V1 +upc, BeerDto.class));
    }

    @Override
    public Optional<BeerDto> getBeerDtoById(UUID uuid) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_PATH_V1 +uuid, BeerDto.class));
    }
}
