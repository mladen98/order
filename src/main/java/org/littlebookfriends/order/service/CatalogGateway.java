package org.littlebookfriends.order.service;

import org.littlebookfriends.order.model.Book;
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

    public List<Book> search(String query) {
        try {
            // Versuche /api/books?query=...
            Book[] result = client.get()
                    .uri(u -> u.path("/api/books").queryParam("query", query).build())
                    .retrieve()
                    .body(Book[].class);

            // Falls null, versuche /api/books/search
            if (result == null || result.length == 0) {
                result = client.get()
                        .uri(u -> {
                            var b = u.path("/api/books/search");
                            for (var word : query.split("\\s+"))
                                b = b.queryParam("keywords", word);
                            return b.build();
                        })
                        .retrieve()
                        .body(Book[].class);
            }

            return result == null ? List.of() : Arrays.asList(result);
        } catch (Exception e) {
            System.out.println("WARN: Catalog service not available or no endpoint found -> returning empty list");
            return List.of(); // Kein Fehler -> einfach leere Liste
        }
    }
}
