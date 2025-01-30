package dev.coph.simplerequest.server;

import dev.coph.simplelogger.Logger;
import dev.coph.simplerequest.handler.AuthenticationHandler;
import dev.coph.simplerequest.handler.RequestDispatcher;
import dev.coph.simplerequest.handler.ServerErrorHandler;
import dev.coph.simplerequest.ratelimit.RateLimitHandler;
import dev.coph.simplerequest.util.Time;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

@Getter
@Accessors(fluent = true, chain = true)
public class WebServer {
    private Logger logger = Logger.getInstance();

    @Setter
    private AuthenticationHandler authenticationHandler;
    private RequestDispatcher requestDispatcher;
    private final int port;
    private final Set<String> allowedOrigins = new HashSet<>();
    private boolean enabled = false;
    private Server server;
    private RateLimitHandler rateLimitHandler;


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
        if (rateLimitHandler != null) {
            rateLimitHandler.addHandler(requestDispatcher.createContextHandler());
            enableWebSockets(rateLimitHandler);
            logger.info("Successfully created ContextHandler. Adding RateLimitHandler.");
            server.setHandler(rateLimitHandler);
        } else {
            ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
            handlerCollection.addHandler(requestDispatcher.createContextHandler());
            enableWebSockets(handlerCollection);
            logger.info("Successfully created ContextHandler. Adding it directly.");
            server.setHandler(handlerCollection);
        }


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

        logger.success("+----------------------------------------------+");
        logger.success("|       Successfully started WebServer         |");
        logger.success("+----------------------------------------------+");
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

    public WebServer addAllowedOrigin(String origin) {
        allowedOrigins.add(origin.toLowerCase());
        return this;
    }

    public WebServer useRateLimit(Time time, int maxRequestsPerSpan) {
        Logger.getInstance().info("Creating RateLimit Handler");
        this.rateLimitHandler = new RateLimitHandler(time, maxRequestsPerSpan);
        Logger.getInstance().success("Successfully RateLimit Handler");
        return this;
    }

    private HashSet<Class<?>> websockets = new HashSet<>();

    private void enableWebSockets(ContextHandlerCollection collection) {

        if (websockets.isEmpty()) {
            Logger.getInstance().info("No WebSockets registered.");
            return;
        }
        Logger.getInstance().info("Enabling WebSockets.");

        ServletContextHandler websocketHandlers = new ServletContextHandler(ServletContextHandler.SESSIONS);
        websocketHandlers.setContextPath("/ws");


        JakartaWebSocketServletContainerInitializer.configure(websocketHandlers, (servletContext, wsContainer) -> {
            websockets.forEach((provider) -> {
                try {
                    try {
                        if (!provider.isAnnotationPresent(ServerEndpoint.class)) {
                            Logger.getInstance().error("Error enabling WebSocket for path: %s. It does not have the annotation @ServerEndpoint".formatted(provider.getSimpleName()));
                            return;
                        }
                        wsContainer.addEndpoint(provider);
                        var pathName = provider.getAnnotation(ServerEndpoint.class).value();
                        Logger.getInstance().success("WebSocket for provider '%s' on path '/ws%s' successfully enabled. ".formatted(provider.getSimpleName(), pathName));
                    } catch (Exception e) {
                        Logger.getInstance().error("Error enabling WebSocket for path: ", e);
                    }
                } catch (Exception e) {
                    Logger.getInstance().error("Error enabling WebSocket support", e);
                }
            });
            Logger.getInstance().success("Successfully enabled all WebSockets.");
        });

        collection.addHandler(websocketHandlers);
    }

    public WebServer registerWebsocket(Class<?> websocketProvider) {
        if (websocketProvider.isAnonymousClass()) {
            Logger.getInstance().error("Could not register Websocket. It is an anonymous class.");
            return this;
        }
        if (!websocketProvider.isAnnotationPresent(ServerEndpoint.class)) {
            Logger.getInstance().error("Could not register Websocket. It does not have the annotation @ServerEndpoint");
            return this;
        }
        websockets.add(websocketProvider);
        return this;
    }
}
