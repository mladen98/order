package org.example.order;

import org.example.order.model.Book;
import org.example.order.service.CatalogGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CatalogGatewayRetryTest {

    @Autowired
    CatalogGateway catalogGateway;

    @Test
    void whenCatalogIsDown_fallbackReturnsEmptyList() {
        // Act: Wir rufen die Suche auf, obwohl der Catalog (Port 9999) nicht läuft
        List<Book> result = catalogGateway.search("harry"); // Macht Sinn einen richtigen möglichen Suchwort zu benutzen

        // Assert: Kein Fehler, sondern eine leere Liste -> Fallback wurde benutzt
        assertThat(result).isEmpty();

        assertThat(catalogGateway.isCatalogAvailable()).isFalse();

    }
}
