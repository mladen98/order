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

    /*public Book findByIsbn(String isbn) {
        // 1) Versuch: Detail-Endpoint /api/books/{isbn}
        try {
            Book one = client.get()
                    .uri("/books/{isbn}", isbn)
                    .retrieve()
                    .body(Book.class);
            if (one != null) return one;
        } catch (Exception ignored) {
            // Endpoint existiert (noch) nicht -> weiter mit Fallback
        }

        // 2) Fallback: Suche nach ISBN und lokal filtern
        try {
            Book[] arr = client.get()
                    .uri(u -> u.path("/api/books").queryParam("query", isbn).build())
                    .retrieve()
                    .body(Book[].class);

            if (arr != null) {
                return Arrays.stream(arr)
                        .filter(b -> isbn.equals(b.isbn()))
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception ignored) {
            // Katalog-API nicht verfügbar -> null zurückgeben
        }

        return null;
    } */
}
