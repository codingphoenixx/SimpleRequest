package dev.coph.simplerequest.server;

import dev.coph.simplelogger.Logger;
import dev.coph.simplerequest.handler.AuthenticationHandler;
import dev.coph.simplerequest.handler.RequestDispatcher;
import dev.coph.simplerequest.handler.ServerErrorHandler;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;


@Slf4j
@Getter
@Accessors(fluent = true, chain = true)
public class WebServer {
    private Logger logger = Logger.getInstance();
    private AuthenticationHandler authenticationHandler;
    private RequestDispatcher requestDispatcher;
    private final int port;
    private final Set<String> allowedOrigins = new HashSet<>();
    private boolean enabled = false;
    private Server server;


    public WebServer(int port) {
        this.port = port;
        this.requestDispatcher = new RequestDispatcher(this);
    }

    public void start() {
        logger.info("Set https protocols to: SSLv3,TLSv1.2,TLSv1.3");
        System.setProperty("https.protocols", "SSLv3,TLSv1.2,TLSv1.3");
        if (!isPortAvailable(port)) {
            logger.error("Port is not available and WebServer cannot get started. Available: " + findFreePort(49152, 65535));
            return;
        }

        logger.info("Creating new server instance with port %s.%n".formatted(port));
        server = new Server(port);

        logger.info("Creating ContextHandler");
        server.setHandler(requestDispatcher.createContextHandler());
        logger.info("Settings error handler");
        server.setErrorHandler(new ServerErrorHandler());

        logger.info("Starting server");
        try {
            server.start();
            logger.success("Successfully started webserver.");
        } catch (Exception e) {
            logger.error("Error starting webserver.", e);
        }

        logger.info("Disable webserver version send.");
        for (Connector connector : server.getConnectors()) {
            for (ConnectionFactory connectionFactory : connector.getConnectionFactories()) {
                if (connectionFactory instanceof HttpConnectionFactory factory) {
                    factory.getHttpConfiguration().setSendServerVersion(false);
                }
            }
        }

        logger.success("------------------------------------------------");
        logger.success("|       Successfully started WebServer         |");
        logger.success("------------------------------------------------");
        enabled = true;
    }


    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    private int findFreePort(int startPort, int endPort) {
        for (int port = Math.min(startPort, endPort); port <= Math.max(startPort, endPort); port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }
}
