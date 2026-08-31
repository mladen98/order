# Order Service

Web-Frontend und Warenkorb des E-Commerce-Systems aus dem Modul
**Agile Application Lifecycle Management**
FHNW Hochschule für Wirtschaft, Olten

Dieser Service liefert die Benutzeroberfläche aus und bezieht die Buchdaten über
REST vom Catalog-Service.

| Teil | Repository |
|---|---|
| Projektübersicht | [ksriganthan/Agile_Application_Lifecycle_Management](https://github.com/ksriganthan/Agile_Application_Lifecycle_Management) |
| Order-Service | dieses Repository |
| Catalog-Service | [ksriganthan/catalog](https://github.com/ksriganthan/catalog) |

---

## Rolle im System

Polyrepo-Architektur: Beide Services liegen in eigenen Repositories, sind
getrennt deploybar und kommunizieren ausschliesslich über REST.

```
  Browser
     │
     ▼
┌──────────────┐   GET /books/search?keyword=…   ┌───────────────┐
│ Order-Service│ ──────────────────────────────▶ │Catalog-Service│
│  Port 8081   │ ◀────────────── JSON ────────── │  Port 8080    │
│  Thymeleaf   │                                 │  REST-API     │
│  Warenkorb   │        Retry + Fallback         └───────┬───────┘
└──────────────┘                                         ▼
                                                  ┌──────────────┐
                                                  │  PostgreSQL  │
                                                  └──────────────┘
```

---

## Oberfläche

| Route | Methode | Zweck |
|---|---|---|
| `/` | GET | Startseite: Suchfeld, Trefferliste, Warenkorb-Übersicht. Optionaler Parameter `keyword` wird an den Catalog-Service durchgereicht. |
| `/cart/add` | POST | Legt ein Buch anhand der `isbn` in den Warenkorb und leitet zurück auf `/`. |

Gerendert wird mit **Thymeleaf** (`templates/catalog.html`). Das Modell enthält
neben Treffern und Warenkorb auch das Flag `catalogUnavailable`, mit dem die
Oberfläche einen Hinweis anzeigt, wenn der Catalog-Service nicht antwortet.

Zusätzlich sind über Spring Boot Actuator sämtliche Endpunkte unter `/actuator`
freigeschaltet.

---

## Resilienz

Der Aufruf des Catalog-Service ist der einzige Punkt, an dem dieser Service von
aussen abhängt – entsprechend ist er abgesichert.

`CatalogGateway.search()` ist mit **Resilience4j** annotiert:

```
@Retry(name = "catalogSearch", fallbackMethod = "fallbackSearch")
```

| Verhalten | Wert |
|---|---|
| Wiederholungsversuche | 3 (`max-attempts`) |
| Wartezeit zwischen Versuchen | 500 ms (`wait-duration`) |
| Fallback | `fallbackSearch()` – liefert eine leere Liste |

Schlagen alle Versuche fehl, setzt der Fallback das Flag `catalogAvailable` auf
`false`. Die Oberfläche zeigt daraufhin einen Hinweis, statt mit einem Fehler
abzubrechen. Ein erfolgreicher Aufruf setzt das Flag wieder zurück.

---

## Warenkorb

Der `ShoppingCartService` ist mit `@SessionScope` annotiert: Jede Browser-Session
erhält ihre eigene Instanz. Der Warenkorb besteht aus `ShoppingCart` und
`CartItem` und wird ausschliesslich im Arbeitsspeicher gehalten.

---

## Starten

### Voraussetzungen

- JDK 21 und Maven (Wrapper `mvnw` liegt bei)
- ein laufender Catalog-Service

### Lokal

Zuerst den [Catalog-Service](https://github.com/ksriganthan/catalog) auf Port
8080 starten, danach:

```bash
./mvnw spring-boot:run
```

Die Oberfläche ist unter `http://localhost:8081` erreichbar.

### Gesamtsystem mit Docker Compose

```bash
docker-compose up -d
```

Startet PostgreSQL, den Catalog- und den Order-Service in einem gemeinsamen
Bridge-Netzwerk. Die beiden Service-Images werden dabei von Docker Hub bezogen,
nicht lokal gebaut. Innerhalb des Netzwerks sprechen sich die Container über
ihre Service-Namen an – deshalb setzt Compose `CATALOG_BASE_URL` auf
`http://catalog:8080` statt auf `localhost`.

---

## Konfiguration

| Property / Variable | Standard | Bemerkung |
|---|---|---|
| `server.port` | `8081` | |
| `catalog.base-url` | `http://localhost:8080` | Fallback für den lokalen Betrieb |
| `CATALOG_BASE_URL` | – | Umgebungsvariable; hat Vorrang vor `catalog.base-url` und wird von Docker Compose gesetzt |
| `resilience4j.retry.instances.catalogSearch.max-attempts` | `3` | |
| `resilience4j.retry.instances.catalogSearch.wait-duration` | `500ms` | |
| `management.endpoints.web.exposure.include` | `*` | alle Actuator-Endpunkte offen |

Die Auflösung der Basis-URL steckt in `RestClientConfig` und ist bewusst
zweistufig: Umgebungsvariable zuerst, Property als Rückfallebene.

---

## Tests

```bash
./mvnw test
```

| Ebene | Test | Werkzeug |
|---|---|---|
| Kontext | `OrderApplicationTests` | Smoke-Test |
| Resilienz | `CatalogGatewayRetryTest` | prüft Retry-Verhalten und Fallback |
| End-to-End | `OrderServiceE2ETest` | Playwright |

Die Coverage wird über **JaCoCo** erhoben.

---

## Bekannte Einschränkungen

- **Kein Bestellabschluss.** Der Warenkorb lässt sich füllen, aber es gibt weder
  Checkout noch Persistenz einer Bestellung. Der Service endet beim Warenkorb.
- **Warenkorb nur in der Session.** Er lebt im Arbeitsspeicher und ist nach
  einem Neustart oder Sessionende verloren; `clear()` existiert im Service, ist
  aber nicht über die Oberfläche erreichbar. Einzelne Positionen lassen sich
  nicht entfernen.
- **Alle Actuator-Endpunkte offen** (`exposure.include=*`), ohne Absicherung.
- **Keine Authentifizierung.** Die Oberfläche ist ohne Anmeldung nutzbar.
