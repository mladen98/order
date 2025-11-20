package org.example.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${CATALOG_BASE_URL:${catalog.base-url}}")
    private String catalogBaseUrl;

    @Bean
    RestClient catalogClient() {
        return RestClient.builder()
                .baseUrl(catalogBaseUrl) // Buchsuche-Service (Sprint 1)
                .build();
    }

}
