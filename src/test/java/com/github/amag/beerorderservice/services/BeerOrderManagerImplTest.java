package com.github.amag.beerorderservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.amag.beerorderservice.domain.BeerOrderLine;
import com.github.amag.beerorderservice.domain.Customer;
import com.github.amag.beerorderservice.domain.BeerOrder;
import com.github.amag.beerorderservice.domain.BeerOrderStatusEnum;
import com.github.amag.beerorderservice.repositories.BeerOrderRepository;
import com.github.amag.beerorderservice.repositories.CustomerRepository;
import com.github.amag.beerorderservice.services.beer.BeerServiceRestTemplateImpl;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.amag.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(WireMockExtension.class)
@Slf4j
class BeerOrderManagerImplTest {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WireMockServer wireMockServer;

    Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    @TestConfiguration
    static class RestTemplateBuilderProvider{
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer(){
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
        .customerName("Test Customer").build());
    }

    @Test
    void new2Allocate() throws JsonProcessingException, InterruptedException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH_V1+beerDto.getUpc())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();
        System.out.println("Started with order id: "+beerOrder.getId());

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        System.out.println("Started with savedBeerOrder id: "+savedBeerOrder.getId());

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            Assertions.assertEquals(BeerOrderStatusEnum.ALLOCATED,foundOrder.getOrderStatus());
        });

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            BeerOrderLine beerOrderLine = foundOrder.getBeerOrderLines().iterator().next();
            assertEquals(beerOrderLine.getOrderQuantity(),beerOrderLine.getQuantityAllocated());
        });

        BeerOrder finalBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(finalBeerOrder);
        assertEquals(BeerOrderStatusEnum.ALLOCATED,finalBeerOrder.getOrderStatus());
        finalBeerOrder.getBeerOrderLines().forEach(o ->
                assertEquals(o.getOrderQuantity(),o.getQuantityAllocated())
                );

    }

    @Test
    void testValidationException() throws JsonProcessingException, InterruptedException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH_V1+beerDto.getUpc())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("failed-validation");
        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION,foundOrder.getOrderStatus());
        });

        BeerOrder finalBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(finalBeerOrder);
        assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION,finalBeerOrder.getOrderStatus());

    }

    @Test
    void new2PickedUp() throws JsonProcessingException, InterruptedException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH_V1+beerDto.getUpc())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();
        System.out.println("Started with order id: "+beerOrder.getId());

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        System.out.println("Started with savedBeerOrder id: "+savedBeerOrder.getId());

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED,foundOrder.getOrderStatus());
        });

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            BeerOrderLine beerOrderLine = foundOrder.getBeerOrderLines().iterator().next();
            assertEquals(beerOrderLine.getOrderQuantity(),beerOrderLine.getQuantityAllocated());
        });

        BeerOrder savedOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
        beerOrderManager.pickUpBeerOrder(savedOrder);

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.PICKED_UP,foundOrder.getOrderStatus());
        });

        BeerOrder finalBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(finalBeerOrder);
        assertEquals(BeerOrderStatusEnum.PICKED_UP,finalBeerOrder.getOrderStatus());
        finalBeerOrder.getBeerOrderLines().forEach(o ->
                assertEquals(o.getOrderQuantity(),o.getQuantityAllocated())
        );

    }

    @Test
    void testCancelledFromPendingValidaion() throws JsonProcessingException, InterruptedException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH_V1+beerDto.getUpc())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();
        System.out.println("Started with order id: "+beerOrder.getId());
        beerOrder.setCustomerRef("dont-validate");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.VALIDATION_PENDING,foundOrder.getOrderStatus());
        });


        beerOrderManager.cancelBeerOrder(savedBeerOrder.getId());

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.CANCELLED,foundOrder.getOrderStatus());
        });

        BeerOrder finalBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(finalBeerOrder);
        assertEquals(BeerOrderStatusEnum.CANCELLED,finalBeerOrder.getOrderStatus());

    }

    @Test
    void testCancelledFromPendingAllocation() throws JsonProcessingException, InterruptedException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH_V1+beerDto.getUpc())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();
        System.out.println("Started with order id: "+beerOrder.getId());
        beerOrder.setCustomerRef("dont-allocate");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATION_PENDING,foundOrder.getOrderStatus());
        });


        beerOrderManager.cancelBeerOrder(savedBeerOrder.getId());

        Awaitility.await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.CANCELLED,foundOrder.getOrderStatus());
        });

        BeerOrder finalBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(finalBeerOrder);
        assertEquals(BeerOrderStatusEnum.CANCELLED,finalBeerOrder.getOrderStatus());

    }


    private BeerOrder createBeerOrder(){
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .customerRef("Test").build();

        Set<BeerOrderLine> beerOrderLines = new HashSet<>();
        beerOrderLines.add(
                BeerOrderLine.builder()
        .beerOrder(beerOrder)
                        .upc("12345")
                        .orderQuantity(1).build()
        );

        beerOrder.setBeerOrderLines(beerOrderLines);
        return beerOrder;
    }
}