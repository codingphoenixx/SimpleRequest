package dev.coph.simplerequest.handler.field;

import dev.coph.simplerequest.handler.RequestMethod;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * A record representing an HTTP request route in a routing system.
 * This class encapsulates information about the route's instance, method,
 * request path, HTTP method, and other request-related attributes.
 * <p>
 * The {@code FieldRoute} record consolidates data required to process
 * and handle HTTP requests in a routing system, correlating Java methods
 * with specific routes and their constraints.
 * <p>
 * This record is immutable and provides accessor methods for each of its components.
 * The {@link FieldRoute} is often used in server-side frameworks to define and
 * validate HTTP request handlers.
 *
 * @param instance      The object instance containing the method to invoke for this route.
 * @param method        The method to be invoked to handle the request.
 * @param path          The URI path associated with the route.
 * @param requestMethod The HTTP request method (e.g., GET, POST) for the route.
 * @param headerName    The name of the header field required for request validation.
 * @param required      A set of parameter names required for executing the route.
 * @param optional      A set of parameter names that are optional for the route.
 * @param defaults      A set of parameter names with default values for the route.
 */
public record FieldRoute(
        Object instance,
        Method method,
        String path,
        RequestMethod requestMethod,
        String headerName,
        Set<String> required,
        Set<String> optional,
        Set<String> defaults) {

}
