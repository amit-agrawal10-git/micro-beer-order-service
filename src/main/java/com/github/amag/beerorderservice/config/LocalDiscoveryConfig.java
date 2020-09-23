package com.github.amag.beerorderservice.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"local-discovery","digitalocean"})
@EnableDiscoveryClient
@Configuration
public class LocalDiscoveryConfig {
}
