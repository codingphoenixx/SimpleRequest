package dev.coph.simplerequest.server;

import dev.coph.simplelogger.Logger;
import dev.coph.simplerequest.handler.AuthenticationAnswer;
import dev.coph.simplerequest.handler.AuthenticationHandler;
import dev.coph.simplerequest.handler.RequestDispatcher;
import dev.coph.simplerequest.handler.ServerErrorHandler;
import dev.coph.simplerequest.handler.endpoint.EndpointRequestHandler;
import dev.coph.simplerequest.ratelimit.RateLimitHandler;
import dev.coph.simplerequest.util.Time;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The {@code WebServer} class is a highly customizable and extensible web server
 * implementation designed for managing incoming HTTP requests, rate limiting,
 * WebSocket support, and authentication/authorization functionalities.
 *
 * The server uses a request dispatcher to handle routing and execution of HTTP requests
 * and supports advanced configurations such as CORS, rate limiting, and WebSocket registration.
 * It provides robust error handling and extensive logging for monitoring server operations.
 *
 * Fields:
 * - {@code logger}: The logger instance used for logging server operations and errors.
 * - {@code authenticationHandler}: An optional handler for managing authentication and authorization logic.
 * - {@code requestDispatcher}: The main request dispatcher responsible for routing and handling HTTP requests.
 * - {@code port}: The port on which the server operates.
 * - {@code allowedOrigins}: A list of domains permitted for CORS.
 * - {@code enabled}: A boolean flag indicating whether the server is currently running.
 * - {@code server}: The server instance, used for handling incoming network connections.
 * - {@code rateLimitHandler}: A handler for managing rate-limiting policies on incoming requests.
 * - {@code websockets}: A collection of WebSocket provider classes registered with the server.
 */
@Getter
@Accessors(fluent = true, chain = true)
public class WebServer {
    /**
     * The logger instance used for logging messages within the {@code WebServer} class.
     * This logger is responsible for recording runtime information, error messages, and
     * operational details to aid in debugging and monitoring the application's behavior.
     *
     * The logger is a singleton instance obtained via {@code Logger.getInstance()},
     * ensuring consistency and preventing multiple logger instances throughout the application.
     */
    private final Logger logger = Logger.getInstance();
    /**
     * Handles user authentication and authorization logic within the WebServer.
     *
     * The authenticationHandler is used for validating user permissions and
     * access rights for incoming requests. It interacts with the implementations
     * of the {@code AuthenticationHandler} interface to enforce security policies.
     * This field must be set for features involving access control to function
     * properly.
     *
     * Responsibilities of the associated {@code AuthenticationHandler} may include:
     * - Verifying whether a user has the necessary permissions to access a resource.
     * - Checking access to specific routes or method handlers.
     * - Parsing and validating authorization credentials from incoming requests.
     *
     * If the authentication handler is not configured, all access-related checks
     * will fail, and certain API endpoints may not be accessible.
     */
    @Setter
    private AuthenticationHandler authenticationHandler;
    /**
     * Manages the dispatching of incoming HTTP requests to the appropriate
     * handler or resource within the web server.
     * <p>
     * The `requestDispatcher` is responsible for processing requests and delegating them
     * to the appropriate method or resource. It acts as the central point for routing
     * and execution of request-response interactions within the server. This variable
     * is initialized during the construction of the `WebServer` and is used
     * throughout its lifecycle to handle incoming HTTP traffic.
     * <p>
     * This field plays a key role in enabling the server to process incoming
     * client requests efficiently, ensuring the correct handlers are invoked
     * for specific routes or API endpoints.
     */
    private final RequestDispatcher requestDispatcher;
    /**
     * Represents the port number on which the web server will listen for incoming connections.
     * <p>
     * This variable is crucial for binding the server to a specific port on the host machine
     * and allows clients to connect to the server using the corresponding network address and port combination.
     * <p>
     * The port must be a valid number within the range of 0 to 65535, and it is recommended
     * to avoid using ports below 1024 unless running with elevated privileges. Configuration
     * of the port happens during the initialization of the WebServer instance.
     * <p>
     * Once the server is started, the value of this variable is final and cannot be changed.
     */
    private final int port;

    /**
     * A set of allowed origins for Cross-Origin Resource Sharing (CORS) in the web server.
     * <p>
     * This collection stores the origins that are permitted to make HTTP requests to the server.
     * It is used to enforce CORS policies by validating the `Origin` header in incoming requests.
     * <p>
     * The origins in this set are stored as case-insensitive strings. To add an origin,
     * use the {@code addAllowedOrigin} method provided in the {@code WebServer} class
     */
    private final Set<String> allowedOrigins = new HashSet<>();
    /**
     * Represents whether the web server is currently enabled or active.
     * <p>
     * This field is used to track the operational state of the server.
     * It is updated during the server lifecycle, particularly when the
     * {@code start()} or shutdown operations are performed. A value of
     * {@code true} indicates that the server is active and running,
     * while {@code false} signifies that the server is stopped or inactive.
     */
    private boolean enabled = false;


    private boolean enableDiscoveryEndpoint = false;

    /**
     * Represents the Jetty Server instance used by the WebServer for handling HTTP and HTTPS requests.
     * <p>
     * This `Server` object is responsible for:
     * - Managing the lifecycle of the web server (start, stop, etc.).
     * - Binding to the configured port and handling incoming client connections.
     * - Dispatching requests to the appropriate handlers such as context handlers, rate limit handlers,
     * WebSocket handlers, or custom request logic.
     * <p>
     * The server's configuration and operations are managed by the containing `WebServer` class, which
     * ensures the proper initialization, security settings, and behavior of the server during runtime.
     */
    private Server server;

    /**
     * Handles rate limiting for incoming requests to the web server.
     * <p>
     * This variable references an instance of {@link RateLimitHandler}, which is used
     * to enforce restrictions on the number of requests allowed from a client within
     * a specified time span. When configured, the {@code rateLimitHandler} is responsible
     * for intercepting
     */
    private RateLimitHandler rateLimitHandler;

    /**
     * Constructs a new WebServer instance with the specified port and initializes
     * the request dispatcher for handling incoming requests.
     *
     * @param port the port number on which the web server will listen for incoming connections
     */
    public WebServer(int port) {
        this.port = port;
        this.requestDispatcher = new RequestDispatcher(this);
    }

    /**
     * Starts the web server and initializes various components required for its operation.
     * <p>
     * The method performs the following tasks:<br>
     * 1. Configures the supported HTTPS protocols for secure communication.<br>
     * 2. Checks if the specified port is available for the server to bind to.
     * If the port is unavailable, it logs an appropriate error message and the
     * process is halted.<br>
     * 3. Creates a new Jetty `Server` instance with the specified port.<br>
     * 4. Initializes and configures the context handlers. If a rate limit handler is
     * provided, it is wrapped and set as the server's main handler. Otherwise,
     * a default `ContextHandlerCollection` is used.<br>
     * 5. Configures an error handler for the server to handle any unforeseen issues.<br>
     * 6. Starts the server and logs the result. Any exceptions encountered during
     * the server startup are logged as errors.<br>
     * 7. Disables the propagation of the server version in HTTP responses for
     * increased security.<br>
     * 8. Updates the `enabled` status of the server upon successful startup.<br>
     * <p>
     * The method includes robust error handling and informational logs for tracing
     * the state of the server during startup.
     */
    public void start() {
        logger.info("Configure Server");
        logger.debug("Set https protocols to: SSLv3,TLSv1.2,TLSv1.3");
        System.setProperty("https.protocols", "SSLv3,TLSv1.2,TLSv1.3");
        if (!isPortAvailable(port)) {
            logger.error("Port is not available and WebServer cannot get started. Available: " + findFreePort(49152, 65535));
            return;
        }

        logger.debug("Creating new server instance with port %s.%n".formatted(port));
        server = new Server(port);


        logger.debug("Creating ContextHandler");
        if (rateLimitHandler != null) {
            rateLimitHandler.addHandler(requestDispatcher.createContextHandler());
            enableWebSockets(rateLimitHandler);
            logger.debug("Successfully created ContextHandler. Adding RateLimitHandler.");
            server.setHandler(rateLimitHandler);
        } else {
            ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
            handlerCollection.addHandler(requestDispatcher.createContextHandler());
            enableWebSockets(handlerCollection);
            logger.debug("Successfully created ContextHandler. Adding it directly.");
            server.setHandler(handlerCollection);
        }

        logger.debug("Adding EndpointRequestHandler");
        requestDispatcher.register(new EndpointRequestHandler(this));

        logger.debug("Settings error handler");
        server.setErrorHandler(new ServerErrorHandler());

        logger.info("Starting server");
        try {
            server.start();
            logger.success("Successfully started webserver.");
        } catch (Exception e) {
            logger.error("Error starting webserver.", e);
        }

        logger.debug("Disable webserver version send.");
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

    /**
     * Checks if a specified port is available for use.
     * The method attempts to open a `ServerSocket` on the given port.
     * If successful, the port is considered available; otherwise, it is in use.
     *
     * @param port the port number to check for availability
     * @return true if the port is available, false if it is in use
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Finds an available port within the specified range. This method iterates
     * through the range of ports provided, starting from the lower bound,
     * and returns the first port that is available. If no port is found,
     * it returns -1.
     *
     * @param startPort the starting port number of the range to check
     * @param endPort   the ending port number of the range to check
     * @return the first available port within the specified range, or -1 if no port is available
     */
    private int findFreePort(int startPort, int endPort) {
        for (int port = Math.min(startPort, endPort); port <= Math.max(startPort, endPort); port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }

    /**
     * Adds an allowed origin to the web server's list of permissible origins for Cross-Origin Resource Sharing (CORS).
     * This enables you to specify domains that are allowed to make requests to the server.
     *
     * @param origin the origin to be added to the list of allowed origins. It will be stored as a lowercase string.
     * @return the current instance of the WebServer, enabling method chaining.
     */
    public WebServer addAllowedOrigin(String origin) {
        allowedOrigins.add(origin.toLowerCase());
        return this;
    }

    /**
     * Configures the web server to use rate limiting by setting the specified time span
     * and maximum number of requests allowed within that span.
     *
     * @param time               the time duration representing the span for rate limiting
     * @param maxRequestsPerSpan the maximum number of requests allowed within the given time span
     * @return the current instance of the WebServer, enabling method chaining
     */
    public WebServer useRateLimit(Time time, int maxRequestsPerSpan) {
        Logger.getInstance().debug("Creating RateLimit Handler");
        this.rateLimitHandler = new RateLimitHandler(this, time, maxRequestsPerSpan);
        Logger.getInstance().success("Successfully created RateLimit Handler");
        return this;
    }

    /**
     * A collection that holds registered WebSocket provider classes for the WebServer.
     * <p>
     * This set contains classes that are annotated with {@code @ServerEndpoint}
     * and are registered through the appropriate methods. These classes enable WebSocket
     * functionality within the server context. If no WebSocket providers are registered,
     * WebSocket support will not be enabled for the server.
     * <p>
     * Each entry in this collection represents a valid WebSocket endpoint provider
     * that must adhere to the restrictions imposed by the WebSocket API (e.g., non-anonymous class,
     * properly annotated with {@code @ServerEndpoint}).
     */
    private HashSet<Class<?>> websockets = new HashSet<>();

    /**
     * Enables WebSocket support for the given context handlers in the web server.
     * All registered WebSocket providers must be annotated with {@code @ServerEndpoint}.
     * If no WebSocket providers are registered, the method logs a message and returns.
     * Otherwise, it configures a new WebSocket context and registers each provider.
     *
     * @param collection the {@code ContextHandlerCollection} instance to which the WebSocket-enabled handler will be added.
     */
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

    /**
     * Registers a WebSocket provider within the WebServer instance.
     * The provided class must not be an anonymous class and must be annotated with @ServerEndpoint.
     * If the conditions are not met, the WebSocket provider will not be registered and an error will be logged.
     *
     * @param websocketProvider the WebSocket provider class to be registered. This class should be annotated
     *                          with @ServerEndpoint and must not be an anonymous class.
     * @return the WebServer instance to allow method chaining.
     */
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


    /**
     * Registers the provided instance with the {@code RequestDispatcher} to handle HTTP requests.
     * This method scans the instance's methods for annotations indicating request handlers
     * and adds them to the web server's dispatcher.
     *
     * @param instance the object whose methods annotated as request handlers will be registered
     *                 with the web server's {@code RequestDispatcher}
     * @return the current instance of {@code WebServer}, enabling method chaining
     */
    public WebServer registerRequestHandler(Object instance){
        requestDispatcher.register(instance);
        return this;
    }

    /**
     * Checks if a request has access by delegating to the authentication handler.
     * If no authentication handler is configured, access is declined.
     *
     * @param methodHandler an object that handles the HTTP method invoked in the request
     * @param request       the incoming HTTP request containing necessary data
     * @return an {@code AuthenticationAnswer} object indicating whether the access
     *         is granted and including additional information about the result
     */
    public AuthenticationAnswer hasAccess(RequestDispatcher.MethodHandler methodHandler, Request request) {
        Logger.getInstance().error("There is an request need to be authenticated, but there is no AuthenticationHandler. Declined request.");
        if (authenticationHandler == null) return new AuthenticationAnswer(false,null);
        return authenticationHandler.hasAccess(methodHandler, request, methodHandler.accessLevel());
    }

    /**
     * Determines whether a request has the specified permission by using the authentication handler.
     * If the authentication handler is not configured or if the request does not include an
     * "Authorization" header, the permission check is declined.
     *
     * @param permission the required permission to validate for the given request
     * @param request    the current HTTP request containing headers and other contextual information
     * @return an {@code AuthenticationAnswer} object indicating whether access is granted
     *         and providing additional information regarding the result
     */
    public AuthenticationAnswer hasPermission(String permission, Request request) {
        Logger.getInstance().error("There is an request need to be authenticated, but there is no AuthenticationHandler. Declined request.");
        if (authenticationHandler == null) return new AuthenticationAnswer(false,null);
        if (!request.getHeaders().contains("Authorization")) return new AuthenticationAnswer(false,null);
        return authenticationHandler.hasPermission(permission, request.getHeaders().get("Authorization"));
    }
}
