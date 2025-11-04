package org.example.order.service;

import org.example.order.model.Book;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
public class CatalogGateway {

    private final RestClient client;

    public CatalogGateway(RestClient catalogClient) {
        this.client = catalogClient;
    }

    public List<Book> search(String keyword) {
        try {
            Book[] result = client.get()
                    .uri(u -> {
                        var b = u.path("/books/search");
                        // Nur queryParam hinzufügen, wenn Keyword vorhanden
                        if (keyword != null) {
                            b = b.queryParam("keyword", keyword);
                        }
                        return b.build();
                    })
                    .retrieve()
                    .body(Book[].class);

            return result == null ? List.of() : Arrays.asList(result);


        } catch (Exception e) {
            System.out.println("WARN: Catalog service not available or no endpoint found -> returning empty list");
            return List.of();
        }
    }
}
