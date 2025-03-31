# Einfache Backends mit Embedded-Jetty

## Übersicht

Diese Java-Bibliothek bietet eine benutzerfreundliche Schnittstelle zur einfachen erstellung von Backends und WebSocket Servern.

## Funktionen

- **Einfache Konfiguration**: Intuitive API zur Konfiguration des Webservers
- **Effizientes HTTP-Handling**: Schnelle Verarbeitung von eingehenden Anfragen
- **WebSocket-Unterstützung**: Einfache Einrichtung und Verwaltung von WebSocket-Verbindungen
- **Integrierte Sicherheitsfunktionen**: Authentifizierung, Ratelimiting und Autorisierung
- **CORS-Management**: Einfache Verwaltung von Cross-Origin Resource Sharing

## Voraussetzungen

- Java 21 oder höher
- Maven oder Gradle (zum Build-Management)

## Installation

### Maven

Fügen Sie die folgende Abhängigkeit in Ihre `pom.xml` ein:

```xml
	<repositories>
            <repository>
                <id>cophrepository-releases</id>
                <name>CoPh Repository</name>
                <url>https://repo.coph.dev/releases</url>
            </repository>
	</repositories>

        <dependencies>
	    <dependency>
                <groupId>dev.coph</groupId>
                <artifactId>simplerequest</artifactId>
                <version>TAG</version>
            </dependency>
	<dependencies>
```

### Gradle

Fügen Sie die folgende Zeile in Ihre `build.gradle` ein:

```groovy
    maven {
        name "cophrepositoryReleases"
        url "https://repo.coph.dev/releases"
    }

    implementation "dev.coph:simplerequest:TAG"
```

## Nutzung

### Grundlegende Konfiguration

```java
public class Main {
    public static void main(String[] args) {
        // Erstellen und Konfigurieren des WebServers
        WebServer webServer = new WebServer(8080)
                .addAllowedOrigin("https://example.com");
        
        //Authentication Handler erstellen
        webServer.authenticationHandler(new WebAuthenticationHandler(this));
        
        //RateLimit festlegen
        webServer.useRateLimit(new Time(1, TimeUnit.MINUTES), 120);

        RequestDispatcher dispatcher = webServer.requestDispatcher();

        // Route hinzufügen
        dispatcher.register(new UserLoginRequestHandler(this));
        
        //Register Websocket [ServerEndpoint muss annotated sein]
        webServer.registerWebsocket(ChatWebSocket.java);
                
        // Server starten
        webServer.start();
    }
}
```

## Dokumentation

Aktuell existiert keine öffentliche Dokumentation. Dies kommt in Zukunft dazu. 

## Kontakt

Für Fragen oder Anregungen kontaktieren Sie uns bitte unter: [codingphoenix@atirion.de](mailto:codingphoenix@atirion.de).
