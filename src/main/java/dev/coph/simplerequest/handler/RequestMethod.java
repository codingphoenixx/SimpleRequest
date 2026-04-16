package dev.coph.simplerequest.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing HTTP request methods supported by the application.
 * Each constant corresponds to a common HTTP method.
 */
public enum RequestMethod {
    /**
     * Represents the HTTP GET request method.
     * <p>
     * This method is used to retrieve data from the server without altering the state
     * of the resource. Commonly employed for fetching resources, GET requests are
     * idempotent and safe, meaning they have no side effects on the server when
     * executed multiple times.
     */
    GET,
    /**
     * Represents the HTTP POST request method.
     * <p>
     * This method is used to submit data to the server, typically to create or update a resource.
     * Unlike the GET method, POST requests may modify the state of the server and are not idempotent,
     * meaning multiple identical requests could result in different outcomes.
     */
    POST,
    /**
     * Represents the HTTP PUT request method.
     * <p>
     * This method is used to update or create a resource on the server with the
     * supplied data. In cases where the resource exists, PUT updates the resource
     * with the provided data. If the resource does not exist, it is created at the
     * specified URI.
     * <p>
     * PUT methods are idempotent, meaning multiple identical requests result in the
     * same server state as a single request.
     */
    PUT,
    /**
     * Represents the HTTP DELETE request method.
     * <p>
     * This method is used to delete a resource identified by a URI on the server.
     * DELETE requests are generally idempotent, meaning multiple identical requests
     * should produce the same outcome. It is commonly used to remove resources
     * or trigger deletion operations on the server.
     */
    DELETE,
    /**
     * Represents the HTTP PATCH request method.
     * <p>
     * This method is used to apply partial modifications to a resource on the server.
     * Unlike the PUT method, which replaces the entire resource, PATCH allows for
     * updating specific fields or portions of a resource. PATCH requests are not
     * necessarily idempotent, as the result of multiple identical requests could vary
     * based on the partial updates being performed.
     */
    PATCH,
    /**
     * Represents the HTTP OPTIONS method as a constant within the {@code RequestMethod} class.
     * OPTIONS is an HTTP method used to describe the communication options available
     * for a target resource. This method is commonly used to determine the supported
     * HTTP methods, headers, or other details at the server endpoint without taking
     * additional actions on the resource.
     */
    OPTIONS,
    /**
     * Represents the HTTP method QUERY in the {@link RequestMethod} enumeration.
     * <p>
     * QUERY is commonly used to represent operations that retrieve or filter
     * resources by specific criteria, often involving query-like behavior.
     * This enumeration constant can be utilized for defining or handling routes
     * where the QUERY method is explicitly required.
     */
    QUERY,
    /**
     * Represents the HTTP HEAD method in the {@link RequestMethod} enumeration.
     * <p>
     * The HEAD method is used to request the headers of a resource without
     * fetching its body, making it useful for checking metadata such as
     * content type or size. It is typically employed to determine resource
     * characteristics prior to fetching, optimizing network usage.
     */
    HEAD,
    /**
     * Represents a catch-all HTTP request method in the {@link RequestMethod} enumeration.
     * This is used to indicate that a route or handler is applicable to any HTTP method,
     * without being restricted to a specific one such as GET, POST, PUT, etc.
     * <p>
     * The {@code ANY} constant is particularly useful in scenarios where a generic handler
     * is needed to process requests for multiple, or all, HTTP methods.
     */
    ANY;
    /**
     * A mapping of HTTP request method names (as Strings) to their corresponding {@link RequestMethod} enum constants.
     * This map allows for efficient lookup and conversion of HTTP method names to their respective enum representation.
     */
    private static final Map<String, RequestMethod> LOOKUP;

    static {
        LOOKUP = new HashMap<>();
        for (RequestMethod m : values()) {
            LOOKUP.put(m.name(), m);
        }
    }

    /**
     * Converts a provided string representation of an HTTP method to its corresponding
     * {@link RequestMethod} enum constant. The lookup is case-insensitive.
     *
     * @param method the string representation of the HTTP method to convert.
     *               Must not be null. If the method is not a valid HTTP method,
     *               the result will be null.
     * @return the corresponding {@link RequestMethod} enum constant, or null if
     * no matching constant is found.
     */
    public static RequestMethod fromString(String method) {
        return LOOKUP.get(method.toUpperCase());
    }
}