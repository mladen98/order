package org.littlebookfriends.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient catalogClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8080") // Buchsuche-Service (Sprint 1)
                .build();
    }

}
