package dev.coph.simplerequest.endpointdiscovery;

import dev.coph.simplerequest.handler.RequestHandler;
import dev.coph.simplerequest.handler.RequestMethod;
import dev.coph.simplerequest.server.WebServer;
import dev.coph.simplerequest.util.ResponseUtil;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.json.JSONObject;

/**
 * The {@code EndpointDiscoveryRequestHandler} class is responsible for handling requests
 * to the "/endpoints" URL path. It provides an endpointdiscovery to enumerate and describe
 * other registered request handlers in the system.
 * <p>
 * This class interacts with a {@code WebServer} instance to retrieve details
 * about registered endpointdiscovery handlers and produces a JSON-formatted response
 * with metadata describing each endpointdiscovery. If the discovery endpointdiscovery is
 * disabled in the {@code WebServer}, the handler returns a service unavailable
 * status code.
 * <p>
 * Constructor:
 * - {@link #EndpointDiscoveryRequestHandler(WebServer)}: Initializes the handler with
 * the provided {@code WebServer} instance.
 * <p>
 * Methods:
 * - {@link #handleEndpointRequest(Response, Callback)}: Handles GET requests
 * to the "/endpoints" URL. Returns a JSON object with metadata about
 * registered endpointdiscovery handlers, such as request method, access level,
 * description, and path. If discovery is disabled, responds with a 503
 * Service Unavailable status.
 */
public class EndpointDiscoveryRequestHandler {
    /**
     * Represents the {@code WebServer} instance utilized by the {@code EndpointDiscoveryRequestHandler}
     * to interact with registered endpointdiscovery handlers and manage the discovery endpointdiscovery.
     * <p>
     * This variable is used to:
     * - Retrieve details about registered request handlers within the server.
     * - Determine whether the discovery endpointdiscovery functionality is enabled.
     * - Access the server's request dispatcher to enumerate handlers and their metadata.
     * <p>
     * This instance is critical for generating a JSON-formatted response containing metadata
     * about registered endpointdiscovery handlers, including request methods, access levels, descriptions,
     * and associated paths.
     * <p>
     * The {@code webServer} is assigned a reference at the initialization of the
     * {@code EndpointDiscoveryRequestHandler} and remains constant throughout the lifecycle of the handler.
     */
    private final WebServer webServer;

    /**
     * Constructs an instance of EndpointDiscoveryRequestHandler.
     *
     * @param webServer the WebServer instance used for handling endpointdiscovery requests
     */
    public EndpointDiscoveryRequestHandler(WebServer webServer) {
        this.webServer = webServer;
    }

    /**
     * Handles requests to the "/endpoints" path. This method checks if the discovery
     * endpointdiscovery is enabled on the web server and, if so, retrieves metadata about the
     * registered request handlers, including their HTTP methods, access levels, and
     * descriptions. If the discovery endpointdiscovery is disabled, it responds with a
     * 503 Service Unavailable status.
     *
     * @param response the Response object used to construct and send the HTTP response
     * @param callback the Callback object used to signal the completion of the request handling
     */
    @RequestHandler(path = "/endpoints", method = RequestMethod.GET, description = "Lists all currently available endpoints.")
    public void handleEndpointRequest(Response response, Callback callback) {
        if (!webServer.enableDiscoveryEndpoint()) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE_503);
            callback.succeeded();
            return;
        }
        JSONObject handlers = new JSONObject();
        webServer.requestDispatcher().handlers().forEach((pattern, methodHandler) -> {
            JSONObject handler = new JSONObject();
            handler.put("method", methodHandler.requestMethod());
            handler.put("accessLevel", methodHandler.accessLevel());
            handler.put("description", methodHandler.description() != null && !methodHandler.description().isBlank() ? methodHandler.description() : null);
            handlers.put(methodHandler.path(), handler);
        });
        ResponseUtil.writeSuccessfulAnswer(response, callback, handlers);
    }

}
