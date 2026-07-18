package com.duoc.minimarket.sales_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CatalogClientConfig {

    @Bean
    public RestClient catalogRestClient(
            @Value("${catalog.service.base-url}")
            String catalogBaseUrl
    ) {
        return RestClient.builder()
                .baseUrl(catalogBaseUrl)
                .build();
    }
}
