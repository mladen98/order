package org.example.order.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.example.order.model.Book;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


import java.util.Arrays;
import java.util.List;

@Service
public class CatalogGateway {

    private final RestClient client;

    // Merkt sich, ob der Catalog zuletzt erreichbar war
    private volatile boolean catalogAvailable = true;

    public CatalogGateway(RestClient catalogClient) {
        this.client = catalogClient;
    }

    public boolean isCatalogAvailable() {
        return catalogAvailable;
    }

    // Diese Methode ruft den Catalog-Service auf.
    // Resilience4j überwacht sie und versucht es bei Fehlern automatisch erneut.
    @Retry(name = "catalogSearch", fallbackMethod = "fallbackSearch")
    public List<Book> search(String keyword) {
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

        // Wenn wir hier ankommen, war der Call erfolgreich
        catalogAvailable = true;

        return result == null ? List.of() : Arrays.asList(result);
    }

    // Fallback: wird aufgerufen, wenn alle Versuche scheitern
    public List<Book> fallbackSearch(String keyword, Throwable ex) {
        catalogAvailable = false;
        System.out.println("WARN: Catalog service DOWN – Fallback aktiv. Grund: " + ex.getMessage());
        return List.of();
    }


}
