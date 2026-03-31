package dev.coph.simplerequest.handler;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * Represents an entry in a routing table, associating a URL pattern with a
 * mapping of HTTP request methods to their respective handlers.
 * <p>
 * This class is used to define and store route definitions, where each route
 * is identified by a matching pattern and contains specific handlers associated
 * with different HTTP methods.
 * <p>
 * Instances of this class are immutable, which ensures thread-safety when used
 * in multi-threaded environments for routing purposes.
 * <p>
 * Components:
 * - A URL {@link Pattern} that defines the matching criteria for the route.
 * - A {@link LinkedHashMap} mapping {@link RequestMethod} to their associated
 *   {@link MethodHandler}, representing the handlers bound to specific HTTP methods.
 * <p>
 * The secondary constructor initializes the {@code methods} map as an empty
 * {@link LinkedHashMap}, allowing the creation of a route entry with only a pattern.
 */
public record RouteEntry(Pattern pattern, LinkedHashMap<RequestMethod, MethodHandler> methods) {
    public RouteEntry(Pattern pattern) {
        this(pattern, new LinkedHashMap<>());
    }
}
