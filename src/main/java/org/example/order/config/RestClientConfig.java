package org.example.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
/*
Prio 1: ${CATALOG_BASE_URL} -> Falls Docker Compose geladen wird, wird dieser Wert gesetzt und priorisiert
Prio 2: ${catalog.base-url} -> Falls Docker Compose nicht geladen und gesetzt wird, wird dieser Fallback-Wert
        aus application.properties genommen!
 */
    @Value("${CATALOG_BASE_URL:${catalog.base-url}}")
    private String catalogBaseUrl;

    @Bean
    RestClient catalogClient() {
        return RestClient.builder()
                .baseUrl(catalogBaseUrl) // Buchsuche-Service (Sprint 1)
                .build();
    }

}
