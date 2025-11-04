package org.example.order.config;

import jakarta.servlet.SessionTrackingMode;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Configuration // Diese Klasse wird beim Start automatisch mitgeladen (vor dem ServletContext und nicht erst danach wie @Component)
public class SessionConfig implements ServletContextInitializer { // Mit der Implementierung kann man direkt auf den Servlet-Container (Tomcat) zugreifen
    // Und dies, bevor die Application vollständig geladen ist
    @Override
    public void onStartup(jakarta.servlet.ServletContext servletContext) {
        servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE)); // Hier wird bestimmt, wie der Server Sitzungen wiedererkennt
        /*
        Sobald PlayWright einen Button klickt, welche ihn einen anderen Pfad aufrufen lässt (z.B. cart/add), erstellt er eine neue Session (Browser-Context),
        zusätzlich wird die ID von dieser Session (Session ID) in URL übergeben, etwa so:
        http://localhost:8081/;jsessionid=ABC123?keyword=ES

        Dadurch werden die Regeln von Session-Scope verletzt (Der Warenkorb hat eine neue Session und ist deshalb leer)
        Mit diesem Code verwendet Spring Boot nur Cookies für die Session-Wiedererkennung

        Wenn man die Applikation manuell testet, funktioniert sie ohne diese Klasse, da der Browser automatisch die SessionID im Cookie mitgibt
         */
        System.out.println(">>> Session Tracking Mode forced to COOKIE <<<");
    }
}