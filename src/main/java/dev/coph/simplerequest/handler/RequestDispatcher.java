package dev.coph.simplerequest.handler;

import dev.coph.simplelogger.Logger;
import dev.coph.simplerequest.authentication.AuthenticationAnswer;
import dev.coph.simplerequest.authentication.AuthenticationHandler;
import dev.coph.simplerequest.body.Body;
import dev.coph.simplerequest.handler.field.FieldResponse;
import dev.coph.simplerequest.handler.field.FieldRoute;
import dev.coph.simplerequest.handler.field.FieldSelection;
import dev.coph.simplerequest.ratelimit.AdditionalCustomRateLimit;
import dev.coph.simplerequest.ratelimit.CustomRateLimit;
import dev.coph.simplerequest.server.WebServer;
import dev.coph.simplerequest.util.IPUtil;
import dev.coph.simplerequest.util.JsonUtil;
import dev.coph.simplerequest.util.ResponseUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code RequestDispatcher} class is responsible for managing HTTP request routing,
 * handling, and processing within a web server environment. It allows the registration
 * of request handlers, dynamic parameter extraction, and the association of incoming
 * HTTP requests with appropriate handler methods. Key features include route organization,
 * parameter resolution, and CORS compliance.
 */
@Getter
@Accessors(fluent = true)
public class RequestDispatcher {
    private static final Logger logger = Logger.of("WebServer");
    private final HashMap<Pattern, AdditionalCustomRateLimit[]> additionalCustomRateLimits = new HashMap<>();
    private final WebServer webServer;
    private final LinkedHashMap<String, RouteEntry> handlers = new LinkedHashMap<>();
    private final List<CompiledFieldRoute> fieldRoutes = new ArrayList<>();

    @Setter
    private boolean filterPrefireRequests = true;

    /**
     * Creates a new instance of RequestDispatcher with the specified WebServer instance.
     *
     * @param webServer the WebServer instance to associate with this RequestDispatcher
     */
    public RequestDispatcher(WebServer webServer) {
        this.webServer = webServer;
    }

    /**
     * Registers all methods annotated with {@code @FieldRequestHandler} or {@code @RequestHandler}
     * within the provided object instance to the request dispatcher. The method processes and
     * registers route handlers defined in the instance for different HTTP paths, methods, and
     * other related configurations such as rate limits.
     *
     * @param instance the object instance containing methods annotated with {@code @FieldRequestHandler}
     *                 or {@code @RequestHandler}, which should be registered for handling routes.
     */
    public void register(Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            if (method.isAnnotationPresent(FieldRequestHandler.class)) {
                FieldRequestHandler fieldRequestHandler =
                        method.getAnnotation(FieldRequestHandler.class);
                method.setAccessible(true);
                FieldRoute route = new FieldRoute(instance, method, fieldRequestHandler.path(), fieldRequestHandler.method(), fieldRequestHandler.headerName(), FieldSelection.normalize(fieldRequestHandler.required()), FieldSelection.normalize(fieldRequestHandler.optional()), FieldSelection.normalize(fieldRequestHandler.defaults()));
                Pattern compiled = createPattern(route.path());
                String[] paramNames = extractParamNames(route.path());
                ParamResolver[] resolvers = buildResolvers(method, paramNames);
                fieldRoutes.add(new CompiledFieldRoute(route, compiled, paramNames, resolvers));
                logger.debug("The method " + method.getName() + " from " + instance.getClass().getSimpleName() + " is annotated with " + fieldRequestHandler.path() + " and registered as a FieldRequestHandler.");
                continue;
            }
            if (method.isAnnotationPresent(RequestHandler.class)) {
                RequestHandler annotation = method.getAnnotation(RequestHandler.class);
                String path = annotation.path();
                RequestMethod requestMethod = annotation.method();

                RouteEntry routeEntry = handlers.computeIfAbsent(path, p -> new RouteEntry(createPattern(p)));

                CustomRateLimit[] customRateLimits = method.getAnnotationsByType(CustomRateLimit.class);
                if (customRateLimits.length > 0) {
                    logger.debug("The method " + method.getName() + " from " + instance.getClass().getSimpleName() + " is annotated with " + customRateLimits.length + " @CustomRateLimit(s).");
                    AdditionalCustomRateLimit[] currentAdditionalCustomRateLimits = new AdditionalCustomRateLimit[customRateLimits.length];
                    for (int i = 0; i < customRateLimits.length; i++) {
                        currentAdditionalCustomRateLimits[i] = new AdditionalCustomRateLimit(customRateLimits[i]);
                    }
                    Pattern pattern = routeEntry.pattern();
                    String name = requestMethod.name();
                    Pattern rateLimitPattern = Pattern.compile(pattern.pattern().replaceAll("\\$$", "") + ":" + name.toUpperCase() + "$");
                    logger.debug("Compiling and adding RateLimit with pattern: " + rateLimitPattern);
                    additionalCustomRateLimits.put(rateLimitPattern, currentAdditionalCustomRateLimits);
                }

                MethodHandler methodHandler = new MethodHandler(path, requestMethod, instance, method, annotation.description());
                methodHandler.accessLevel = annotation.accesslevel();

                if (routeEntry.methods().containsKey(requestMethod)) {
                    logger.warn("Duplicate handler for " + requestMethod + " " + path + " - overwriting.");
                }
                routeEntry.methods().put(requestMethod, methodHandler);

                resortHandlers();
            }
        }
    }

    /**
     * Extracts parameter names enclosed within curly brackets from the given path string.
     * The method identifies dynamic placeholders in the path, which are enclosed in
     * curly brackets (e.g., {paramName}), and returns an array of these parameter names.
     *
     * @param path the input path string that may contain parameter placeholders
     * @return an array of parameter names extracted from the path, or an empty array if no parameters are found
     */
    private String[] extractParamNames(String path) {
        List<String> names = new ArrayList<>();
        for (String part : path.split("/")) {
            if (part.startsWith("{") && part.endsWith("}")) {
                names.add(part.substring(1, part.length() - 1));
            }
        }
        return names.toArray(new String[0]);
    }

    /**
     * Constructs an array of {@code ParamResolver} instances to match method parameter types
     * with appropriate resolvers. The method iterates over the parameter types of the given
     * method and maps each type to a corresponding {@code ParamResolver}, such as handling
     * {@code Request}, {@code Response}, or custom path variables.
     *
     * @param m the method whose parameters need to be resolved
     * @param pathParamNames an array of parameter names extracted from the path string
     *                        for mapping to specific path variable resolvers
     * @return an array of {@code ParamResolver} instances, each responsible for resolving
     *         its associated method parameter during request handling
     */
    private ParamResolver[] buildResolvers(Method m, String[] pathParamNames) {
        Class<?>[] pts = m.getParameterTypes();
        ParamResolver[] resolvers = new ParamResolver[pts.length];
        int pathIdx = 0;
        for (int i = 0; i < pts.length; i++) {
            Class<?> p = pts[i];
            if (Request.class.isAssignableFrom(p)) {
                resolvers[i] = ParamResolver.REQUEST;
            } else if (Response.class.isAssignableFrom(p)) {
                resolvers[i] = ParamResolver.RESPONSE;
            } else if (Callback.class.isAssignableFrom(p)) {
                resolvers[i] = ParamResolver.CALLBACK;
            } else if (Body.class.isAssignableFrom(p)) {
                resolvers[i] = ParamResolver.BODY;
            } else if (String.class.isAssignableFrom(p)) {
                int idx = pathIdx < pathParamNames.length ? pathIdx : i;
                resolvers[i] = new ParamResolver.PathVar(idx);
                pathIdx++;
            } else {
                resolvers[i] = ParamResolver.NULL;
            }
        }
        return resolvers;
    }

    /**
     * Reorders the `handlers` map by sorting its entries based on specific criteria
     * related to their corresponding route keys. The updated ordering ensures that
     * less dynamic routes are prioritized over more dynamic ones.
     * <p>
     * The sorting criteria are as follows:
     * 1. Routes with fewer dynamic segments (e.g., {param}) are prioritized.
     * 2. Among routes with an equal number of dynamic segments, those with a
     *    greater number of total segments are prioritized.
     * 3. For routes with the same number of dynamic and total segments, the
     *    natural lexicographical order of their keys is used.
     * <p>
     * This method clears the `handlers` map and repopulates it with the sorted entries
     * while preserving the insertion order.
     */
    private void resortHandlers() {
        LinkedHashMap<String, RouteEntry> temp = new LinkedHashMap<>(handlers);
        handlers.clear();
        temp.entrySet().stream().sorted((a, b) -> {
            String pa = a.getKey();
            String pb = b.getKey();
            int dynA = countDynamicSegments(pa);
            int dynB = countDynamicSegments(pb);
            if (dynA != dynB) return Integer.compare(dynA, dynB);
            int segA = countSegments(pa);
            int segB = countSegments(pb);
            if (segA != segB) return Integer.compare(segB, segA);
            return pa.compareTo(pb);
        }).forEachOrdered(e -> handlers.put(e.getKey(), e.getValue()));
    }

    /**
     * Counts the number of dynamic segments in the given path string.
     * A dynamic segment is defined as a placeholder enclosed within curly braces
     * (e.g., {segmentName}).
     *
     * @param path the input path string to analyze for dynamic segments
     * @return the number of dynamic segments present in the path
     */
    private int countDynamicSegments(String path) {
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        int count = 0;
        for (String part : path.split("/")) {
            if (!part.isEmpty() && part.charAt(0) == '{' && part.charAt(part.length() - 1) == '}')
                count++;
        }
        return count;
    }

    /**
     * Counts the number of non-empty segments in the provided path string.
     * A segment is defined as a substring separated by the forward slash ('/') character.
     * Trailing slashes in the input path are ignored during the calculation.
     *
     * @param path the input path string to analyze for segment count
     * @return the number of non-empty segments in the path
     */
    private int countSegments(String path) {
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        int count = 0;
        for (String part : path.split("/"))
            if (!part.isEmpty()) count++;
        return count;
    }

    /**
     * Creates a {@code Pattern} instance to match a given path string. The generated pattern
     * translates dynamic segments enclosed in curly braces (e.g., {paramName}) into regular
     * expressions, allowing for parameterized matching of URLs. Each segment of the path
     * separated by slashes is processed to generate an appropriate regex component.
     *
     * @param path the input path string for which to create a matching {@code Pattern}.
     *             Dynamic segments should be enclosed within curly braces (e.g., {paramName}).
     * @return a {@code Pattern} object that can be used to match the input path against
     *         incoming requests.
     */
    private Pattern createPattern(String path) {
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);

        StringBuilder regex = new StringBuilder("^");
        for (String part : path.split("/")) {
            if (!part.isEmpty()) {
                regex.append("\\/");
                if (part.startsWith("{") && part.endsWith("}")) {
                    regex.append("([\\w-]+)");
                } else {
                    regex.append(Pattern.quote(part));
                }
            }
        }

        if (regex.charAt(regex.length() - 1) != '/')
            regex.append("\\/");
        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    /**
     * Processes an incoming HTTP request by matching the given path against defined field routes
     * and request handlers, performs pre-processing, executes the appropriate handler, and generates
     * the corresponding HTTP response.
     *
     * @param path The path of the incoming request, which may include variables or placeholders.
     * @param request The HTTP request object containing details such as headers, body, and method.
     * @param response The HTTP response object used to send back data and status to the client.
     * @param callback A callback to signal the completion of processing and allow for asynchronous handling.
     */
    public void handle(String path, Request request, Response response, Callback callback) {
        if (path.charAt(path.length() - 1) != '/')
            path += "/";

        boolean wasPreFireRequest = addDefaultHeaders(request, response, callback);

        if (wasPreFireRequest && filterPrefireRequests)
            return;

        String trimmedPath = path;

        for (CompiledFieldRoute cfr : fieldRoutes) {
            Matcher matcher = cfr.pattern.matcher(trimmedPath);
            if (matcher.matches()) {
                FieldRoute r = cfr.route;
                if (!r.requestMethod().equals(RequestMethod.ANY) && !r.requestMethod().name().equals(request.getMethod().toUpperCase())) {
                    response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
                    callback.succeeded();
                    return;
                }

                String[] groups = new String[matcher.groupCount()];
                for (int i = 0; i < groups.length; i++)
                    groups[i] = matcher.group(i + 1);

                try {
                    handleFieldRoute(cfr, request, response, callback, groups);
                } catch (Exception e) {
                    logger.error("An error occurred while invoking the method " + r.method().getName() + " of the class " + r.instance().getClass().getName() + ".", e);
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    callback.succeeded();
                }
                return;
            }
        }

        for (Map.Entry<String, RouteEntry> entry : handlers.entrySet()) {
            RouteEntry routeEntry = entry.getValue();
            Matcher matcher = routeEntry.pattern().matcher(trimmedPath);
            if (matcher.matches()) {
                String incomingMethod = request.getMethod().toUpperCase();
                RequestMethod incomingRequestMethod = RequestMethod.fromString(incomingMethod);

                MethodHandler handler = routeEntry.methods().get(incomingRequestMethod);
                if (handler == null)
                    handler = routeEntry.methods().get(RequestMethod.ANY);

                if (handler == null) {
                    response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
                    StringJoiner allowed = new StringJoiner(", ");
                    routeEntry.methods().keySet().forEach(m -> allowed.add(m.name()));
                    response.getHeaders().add(HttpHeader.ALLOW, allowed.toString());
                    callback.succeeded();
                    return;
                }

                AuthenticationAnswer authenticationAnswer = null;
                switch (handler.accessLevel()) {
                    case DISABLED -> {
                        logger.debug("A incoming request tried to call a disabled request handler.");
                        response.setStatus(HttpStatus.UNAUTHORIZED_401);
                        callback.succeeded();
                        return;
                    }
                    case AUTHENTICATED -> {
                        AuthenticationHandler authenticationHandler = webServer.authenticationHandler();

                        if (authenticationHandler == null) {
                            logger.error("There is a request that needs authentication, but no AuthenticationHandler exists.");
                            response.setStatus(HttpStatus.UNAUTHORIZED_401);
                            callback.succeeded();
                            return;
                        }
                        authenticationAnswer = authenticationHandler.hasGeneralAccess(request, handler.accessLevel());

                        if (authenticationAnswer == null) {
                            logger.error("AuthenticationAnswer is null. Declined request.");
                            response.setStatus(HttpStatus.UNAUTHORIZED_401);
                            callback.succeeded();
                            return;
                        }

                        if (!authenticationAnswer.hasAccess()) {
                            response.setStatus(HttpStatus.UNAUTHORIZED_401);
                            ResponseUtil.writeAnswer(response, callback, authenticationAnswer.message());
                            callback.succeeded();
                            return;
                        }
                    }
                }

                Map<String, String> pathVariables = matcher.groupCount() > 0 ? new HashMap<>(matcher.groupCount()) : Collections.emptyMap();
                for (int i = 1; i <= matcher.groupCount(); i++)
                    pathVariables.put("arg" + i, matcher.group(i));

                try {
                    handler.invoke(request, response, callback, authenticationAnswer, pathVariables);
                } catch (Exception e) {
                    logger.error("An error occurred while invoking the method " + handler.method().getName() + " of the class " + handler.instance().getClass().getName() + ".", e);
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    callback.succeeded();
                    return;
                }

                if (!response.getHeaders().contains(HttpHeader.CONTENT_TYPE))
                    response.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json;charset=utf-8");
                callback.succeeded();
                return;
            }
        }
        response.setStatus(HttpStatus.NOT_FOUND_404);
        callback.succeeded();
    }

    /**
     * Creates and configures a new instance of {@code ContextHandler} with a root path ("/").
     * The returned context handler is responsible for delegating the processing of incoming HTTP requests
     * to a specific handler method of the {@code RequestDispatcher} class.
     * <p>
     * The handler performs the following tasks:
     * 1. Extracts the path information from the request's URI.
     * 2. If a valid path is available, it invokes the {@code RequestDispatcher#handle} method, passing
     *    the path and key request/response objects for further processing.
     * 3. If the path is null, it sets the response with a 404 status and signals success in the callback.
     *
     * @return a {@code ContextHandler} instance configured to manage requests for the root path ("/").
     */
    public ContextHandler createContextHandler() {
        return new ContextHandler(new Handler.Abstract() {
            @Override
            public boolean handle(Request request, Response response, Callback callback) {
                String pathInfo = request.getHttpURI().getPath();
                if (pathInfo != null) {
                    RequestDispatcher.this.handle(pathInfo, request, response, callback);
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND_404);
                    callback.succeeded();
                }
                return false;
            }
        }, "/");
    }

    /**
     * Adds default CORS headers to the provided response object based on the request and server configuration.
     * This method ensures compliance with the configured CORS rules, such as allowed origins, methods, and headers.
     * If the incoming request is an HTTP OPTIONS preflight request, the response is updated accordingly,
     * and the callback is invoked to signal success.
     *
     * @param request the HTTP request object that contains the client’s request details, including headers and method
     * @param response the HTTP response object to which the CORS headers will be added
     * @param callback a callback used to signal the completion of the operation, typically for asynchronous handling
     * @return {@code true} if the method processes a preflight OPTIONS request and completes handling;
     *         {@code false} otherwise
     */
    private boolean addDefaultHeaders(Request request, Response response, Callback callback) {
        if (!webServer.allowedOrigins().contains("*")) {
            response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_METHODS, String.join(",", webServer.allowedMethods()));
        response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_HEADERS, String.join(",", webServer.allowedHeaders()));

        String origin = request.getHeaders().get("Origin");
        if (webServer.allowedOrigins().contains("*")) {
            response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        } else if (origin != null) {
            if ((webServer.corsAllowLocalhost() && IPUtil.isLocal(request)) || webServer.allowedOrigins().contains(origin.toLowerCase())) {
                response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            }
        }

        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpStatus.ACCEPTED_202);
            callback.succeeded();
            return true;
        }
        return false;
    }

    /**
     * Handles routing of field requests based on the compiled field route and request parameters.
     * <p>
     * This method processes the incoming request, resolves parameters, invokes the appropriate
     * method, and writes the response, which can either be a JSON-compatible type or a `FieldResponse`.
     *
     * @param cfr The compiled field route containing routing metadata and method information.
     * @param request The incoming HTTP request that contains the field selection and other context.
     * @param response The HTTP response to be populated based on the processed result.
     * @param callback The callback to indicate the success or failure of the request processing.
     * @param groups The security or context groups used during resolution of parameters.
     * @throws Exception If an error occurs during the method invocation or response building.
     */
    private void handleFieldRoute(CompiledFieldRoute cfr, Request request, Response response, Callback callback, String[] groups) throws Exception {
        FieldRoute route = cfr.route;
        Set<String> requested = FieldSelection.read(request, route.headerName());
        if (requested.isEmpty()) requested = route.defaults();

        LinkedHashSet<String> safe = new LinkedHashSet<>(route.required());

        for (String f : requested) {
            if (route.optional().contains(f))
                safe.add(f);
        }

        ParamResolver[] resolvers = cfr.resolvers;
        Object[] args = new Object[resolvers.length];
        for (int i = 0; i < resolvers.length; i++) {
            args[i] = resolvers[i].resolve(request, response, callback, groups);
        }

        Object result = route.method().invoke(route.instance(), args);

        if (!(result instanceof FieldResponse fr)) {
            if (result instanceof Map<?, ?> map) {
                writeJson(response, callback, map);
                return;
            }
            if (result instanceof String s) {
                response.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json");
                ResponseUtil.writeAnswer(response, callback, s);
                callback.succeeded();
                return;
            }
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            ResponseUtil.writeAnswer(response, callback, "FieldRequestHandler must return FieldResponse or JSON-compatible type");
            callback.succeeded();
            return;
        }

        Map<String, Object> payload = fr.build(safe, route.required());
        response.getHeaders().add("X-Fields-Resolved", String.join(",", payload.keySet()));
        writeJson(response, callback, payload);
    }

    /**
     * Writes a JSON representation of the given map to the response object and marks the callback as succeeded.
     *
     * @param resp     the HTTP response object to which JSON content will be written
     * @param callback the callback to indicate the completion of the operation
     * @param map      the map data to be converted into JSON and written to the response
     */
    private void writeJson(Response resp, Callback callback, Map<?, ?> map) {
        resp.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json");
        ResponseUtil.writeAnswer(resp, callback, JsonUtil.toJsonObject(map));
        callback.succeeded();
    }

    /**
     * Represents a compiled version of a field route, encapsulating the associated route information,
     * compiled pattern, parameter names, and parameter resolvers.
     * <p>
     * This record is designed to facilitate the processing of field routes by associating
     * required metadata, including a regex pattern for matching, and resolvers for handling
     * the route parameters.
     */
    record CompiledFieldRoute(FieldRoute route, Pattern pattern, String[] paramNames, ParamResolver[] resolvers) { }

    /**
     * Interface representing a parameter resolver that extracts and provides specific parameters
     * based on the request, response, callback, and route groups in a web context.
     * <p>
     * The interface contains predefined resolvers for common components including request,
     * response, callback, and request body. Additionally, it defines a custom resolver
     * implementation for resolving path variables by their position.
     * <p>
     * Predefined Resolvers:
     * - REQUEST: Resolves to the incoming request object.
     * - RESPONSE: Resolves to the outgoing response object.
     * - CALLBACK: Resolves to the callback object.
     * - BODY: Resolves to the request body as a new Body object.
     * - NULL: Always resolves to null.
     * <p>
     * Custom Resolver:
     * - PathVar: Resolves to a specific path variable by its index in the route groups.
     */
    interface ParamResolver {
        /**
         * Predefined parameter resolver that resolves to the incoming request object.
         * <p>
         * This resolver is part of the {@code ParamResolver} interface and is used to return the request
         * object directly, as passed to the handler. It serves as a helper to simplify access to the
         * request within the context of web application routing and handling.
         * <p>
         * Operational Details:
         * - The {@code resolve} method implementation for this resolver takes the request, response,
         *   callback, and route group parameters but only utilizes the request parameter for resolution.
         * - The resulting value is the unchanged input {@code Request} instance.
         * <p>
         * Example Use Case:
         * - Often used when the handler implementation requires direct access to the incoming request
         *   object for purposes such as query parameter extraction, header processing, or payload handling.
         */
        ParamResolver REQUEST = (req, res, cb, g) -> req;
        /**
         * Predefined parameter resolver that resolves to the outgoing response object.
         * <p>
         * This resolver is part of the {@code ParamResolver} interface and is used to return the response
         * object directly, as passed to the handler. It serves as a helper to simplify access to the
         * response within the context of web application routing and handling.
         * <p>
         * Operational Details:
         * - The {@code resolve} method implementation for this resolver takes the request, response,
         *   callback, and route group parameters but only utilizes the response parameter for resolution.
         * - The resulting value is the unchanged input {@code Response} instance.
         * <p>
         * Use Case:
         * - Typically used when the handler implementation requires direct access to the outgoing response
         *   object for purposes such as setting headers, writing content, or managing the HTTP status code.
         */
        ParamResolver RESPONSE = (req, res, cb, g) -> res;
        /**
         * A predefined implementation of the {@code ParamResolver} interface that directly returns
         * the provided {@code Callback} instance without any modification or additional processing.
         * <p>
         * This resolver can be used in scenarios where no parameter resolution or transformation
         * is needed, and the given {@code Callback} should be passed through as-is.
         */
        ParamResolver CALLBACK = (req, res, cb, g) -> cb;
        /**
         * A predefined {@link ParamResolver} for resolving the body of an HTTP request.
         * This resolver processes the provided {@code Request} instance to create a new
         * {@link Body} object, encapsulating the content of the request body.
         * <p>
         * The BODY resolver is typically used to extract and access the raw or processed
         * content of an HTTP request body in various formats, such as a string, byte
         * buffer, input stream, JSON, or even as an image representation.
         * <p>
         * Features:
         * - Simplifies access to the request body within request-handling workflows.
         * - Supports multiple content representation formats by utilizing the {@link Body} class.
         * <p>
         * Parameters:
         * - {@code req}: The {@link Request} object containing the incoming HTTP request.
         * - {@code res}: The {@link Response} object representing the outgoing response (unused in this resolver).
         * - {@code cb}: The {@link Callback} instance to handle callback-specific logic (unused in this resolver).
         * - {@code g}: An array of group strings (unused in this resolver).
         * <p>
         * Returns:
         * A {@link Body} object instantiated using the provided {@code req} parameter.
         */
        ParamResolver BODY = (req, res, cb, g) -> new Body(req);
        /**
         * A predefined constant representing a {@code ParamResolver} instance that always
         * resolves to {@code null}. This implementation effectively functions as a
         * no-op or placeholder resolver that performs no processing and produces no result.
         * <p>
         * Example use case includes scenarios where a resolver behavior is intentionally
         * omitted or delegated without impacting the primary flow of execution.
         */
        ParamResolver NULL = (req, res, cb, g) -> null;

        /**
         * Resolves the appropriate object based on the provided request, response, callback,
         * and groups. This method is used to handle the resolution logic for the parameters
         * involved in a specific operation.
         *
         * @param request  the incoming request containing necessary information
         * @param response the response object that will be populated or modified
         * @param callback the callback mechanism used to process additional logic
         * @param groups   an array of group identifiers that may influence resolution
         * @return the resolved object based on the provided inputs
         */
        Object resolve(Request request, Response response, Callback callback, String[] groups);

        /**
         * A record that represents a resolver for extracting path variables from a web routing context.
         * <p>
         * PathVar is an implementation of the ParamResolver interface that resolves a specific
         * path variable based on its positional index within the provided route groups. It is
         * typically used to retrieve parts of a URL matched by a pattern.
         * <p>
         * Features:
         * - Resolves a single path variable identified by its index in the groups array.
         * - Provides flexibility in routing by allowing extraction of URL components for
         *   use in request handlers or controllers.
         * <p>
         * Usage Flow:
         * 1. Specify the index of the path variable to resolve using the constructor.
         * 2. During resolution, if the index is valid for the given groups array, the matching
         *    path variable string will be returned.
         * 3. If the index is out of bounds, null is returned.
         * <p>
         * Constructor:
         * - {@code PathVar(int index)}: Constructs a new PathVar resolver with the specified
         *   index identifying the desired path variable.
         * <p>
         * Record Components:
         * - {@code index}: The zero-based index of the path variable to resolve within the groups array.
         * <p>
         * Method Details:
         * - {@code resolve(Request request, Response response, Callback callback, String[] groups)}:
         *   Resolves the path variable at the specified index in the groups array. If the index is
         *   out of bounds, the method returns null.
         * <p>
         * Parameters:
         * - {@code request}: The HTTP request object (not directly used by this resolver).
         * - {@code response}: The HTTP response object (not directly used by this resolver).
         * - {@code callback}: The callback object (not directly used by this resolver).
         * - {@code groups}: An array of strings, typically representing matched route groups.
         * <p>
         * Returns:
         * - The resolved path variable as a string, or null if the index is out of bounds.
         */
        record PathVar(int index) implements ParamResolver {
            @Override
            public Object resolve(Request request, Response response, Callback callback, String[] groups) {
                return index < groups.length ? groups[index] : null;
            }
        }
    }
}