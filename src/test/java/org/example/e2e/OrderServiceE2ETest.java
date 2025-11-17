package org.example.e2e;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import java.util.List;

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
                        .setHeadless(false) // Browser ist sichtbar (man sieht den Testvorgang)
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
        // 1️⃣ Seite öffnen
        page.navigate("http://localhost:8081/"); // Öffnet den Webshop
        // Wartet bis das Suchfeld sichtbar ist (max. 15 Sek)
        page.waitForSelector("input[name='keyword']", new Page.WaitForSelectorOptions().setTimeout(15000));

        // Suchbegriff eingeben (ES)
        page.fill("input[name='keyword']", "fiTzek,FrAu");

        // Auf 'Suchen' klicken
        page.click("button:has-text('Suchen')");
        page.waitForLoadState(LoadState.NETWORKIDLE); // Wartet bis keine weiteren Netzanfragen mehr laufen z.B.
        // Antwort von LocalHost:8080, Renderung von Thymeleaf, HTML, CSS wird fertig geladen
        Thread.sleep(1000); // kleine Wartezeit nach Reload

        // Überprüfen, ob die Bücher (2) von Sebastian Fitzek angezeigt werden
        String htmlAfterSearch = page.content(); // HTML-Seite wird in String umgewandelt
        assertTrue(htmlAfterSearch.contains("Der Nachbar"), "1. Buchtitel sollte angezeigt werden");
        assertTrue(htmlAfterSearch.contains("Sebastian Fitzek"), "Autor sollte sichtbar sein");
        assertTrue(htmlAfterSearch.contains("Frau leidet an Monophobie, Nachbar verfolgt sie"), "1. Beschreibung sollte sichtbar sein");

        assertTrue(htmlAfterSearch.contains("Mimik"), "2. Buchtitel sollte angezeigt werden");
        assertTrue(htmlAfterSearch.contains("Frau leidet an Gedächtnisverlust und versucht den Mord an Paul zu verhindern"), "2. Beschreibung sollte sichtbar sein");

        // „Zum Warenkorb“ klicken
        page.click("button:has-text('Zum Warenkorb')"); // Auf Button "Zum Warenkorb" klicken
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(1000); // kurze Pause nach Post

        // Überprüfen, ob Warenkorb das Buch enthält
        String cartHeader = page.textContent("h2:has-text('Warenkorb')");
        assertTrue(cartHeader.contains("(1 Artikel)"), "Warenkorb sollte 1 Artikel anzeigen");

        String cartTable = page.textContent("h2:has-text('Warenkorb') + table");
        //Todo Hier wäre die Reihenfolge wichtig - gibt es eine andere Möglichkeit noch den Warenkorb teil zu prüfen?
        assertTrue(cartTable.contains("Der Nachbar"), "Warenkorb-Tabelle sollte das Buch 'Der Nachbar' enthalten");
        assertTrue(cartTable.contains("1"), "Warenkorb-Tabelle sollte Menge 1 anzeigen");
    }
}
