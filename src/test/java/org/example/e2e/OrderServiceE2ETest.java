package org.example.e2e;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import java.util.List;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceE2ETest {

    static Playwright playwright; // Hauptbibliothek, um Browser zu steuern
    static Browser browser; // Gestartete Browser-Instanz z.B. Google Chrome
    BrowserContext context; // Eine Session innerhalb des Browsers
    Page page; // eine einzelne Browser-Seite

    @BeforeAll // Einmalig ganz am Anfang
    static void setupAll() {
        playwright = Playwright.create(); // der interne Controller wird gestartet
        browser = playwright.chromium().launch( // öffnet den Chromium-Browser (Google Chrome)
                new BrowserType.LaunchOptions()
                        .setHeadless(true) // Browser ist sichtbar (man sieht den Testvorgang)
                        .setSlowMo(2000) // Langsam, damit wir es sehen
                        .setArgs(List.of("--start-maximized")) // Vollbild starten
        );
    }

    @AfterAll // Aufräumen nach allen Tests ganz am Schluss
    static void teardownAll() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach // Vor jedem einzelnen Test
    void setup() {
        context = browser.newContext( // Eine neue Browser-Session (eigene Cookies) erstellen
                new Browser.NewContextOptions()
                        .setViewportSize(null) // Feste Grösse wird deaktiviert -> Browser nutzt die volle Fenstergrösse
        );
        page = context.newPage(); // Erstellt eine neue Seite (Tab)
    }

    @AfterEach // Nach jedem einzelnen Test
    void tearDown() {
        context.close(); // Die Session wird beendet (inkl. Page)
    }

    @Test
    void testSearchAndAddToCart() throws InterruptedException {
        // Seite öffnen
        page.navigate("http://localhost:8081/"); // Öffnet den Webshop

        // Suchbegriff eingeben / Locatorobjekt wartet automatisch bis page geladen ist
        Locator searchInput = page.getByTestId("search-input");
        assertThat(searchInput).isVisible();
        searchInput.fill("fiTzek,FrAu");

        // Auf 'Suchen' klicken
        Locator searchButton = page.getByTestId("search-button");
        assertThat(searchButton).isVisible();
        searchButton.click();

        // Überprüfen, ob die Bücher (2) von Sebastian Fitzek angezeigt werden
        Locator resultTable = page.getByTestId("results-table");
        assertThat(resultTable).isVisible();

        Locator rows = resultTable.locator("tbody tr");

        int rowCount = rows.count();
        assertTrue(rowCount >=2); // Es sollten mindestens 2 Bücher sein

        // Testen, ob 2 bestimmte Bücher von Sebastian Fitzek vorhanden sind (Voraussetzung: Diese Bücher sind drin)
        boolean containsNachbar = false;
        boolean containsMimik = false;

        for (int i = 0; i < rows.count(); i++) {
            Locator row = rows.nth(i);

            String title = row.locator("td:nth-of-type(2)").textContent().trim();
            String author = row.locator("td:nth-of-type(3)").textContent().trim();
            String description = row.locator("td:nth-of-type(4)").textContent().trim();

            if (title.equals("Der Nachbar")) {
                containsNachbar = true;
                assertTrue(author.contains("Sebastian Fitzek"));
                assertTrue(description.contains("Monophobie"));
            }

            if (title.equals("Mimik")) {
                containsMimik = true;
                assertTrue(description.contains("Gedächtnisverlust"));
            }
        }

        assertTrue(containsNachbar, "'Der Nachbar' sollte gefunden werden");
        assertTrue(containsMimik, "'Mimik' sollte gefunden werden");


        // „Zum Warenkorb“ für genau jene Tabellenzeile klicken, in der das Buch „Der Nachbar“ steht.
        // Dadurch ist der Test nicht mehr von der Reihenfolge der Suchergebnisse abhängig.
        for (int i = 0; i < rows.count(); i++) {
            Locator row = rows.nth(i);

            String title = row.locator("td:nth-of-type(2)").textContent().trim();

            if (title.equals("Der Nachbar")) {
                // In genau dieser Row den richtigen Button finden
                Locator buttonInRow = row.getByTestId("zumWarenkorb-button");
                buttonInRow.click();
                break;
            }
        }

        // Überprüfen, ob der Warenkorb das Buch enthält
        assertThat(page.getByTestId("warenkorb-header")).containsText("1 Artikel");

        Locator cartTable = page.getByTestId("cart-table");
        assertThat(cartTable).isVisible();

        Locator rowsWarenkorb = page.getByTestId("cart-row");
        assertEquals(1, rowsWarenkorb.count()); // sollte 1 Artikel haben

        Locator firstRow = rowsWarenkorb.nth(0);
        assertThat(firstRow.getByTestId("cart-row-title")).containsText("Der Nachbar");

        assertThat(firstRow.getByTestId("cart-row-qty")).containsText("1");

    }
}
