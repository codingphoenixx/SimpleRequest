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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code RequestDispatcher} class is responsible for routing and handling HTTP requests
 * in a web server environment. It provides methods for registering request handlers based
 * on specific patterns and managing incoming requests by delegating them to the appropriate
 * registered handler.
 * <p>
 * The class supports features such as <br>
 * - Dynamic routing with path variable resolution.<br>
 * - Filtering of preflight (CORS) requests.<br>
 * - Authentication checks for secured endpoints.<br>
 * - Integration with {@link WebServer} for handling HTTP processing.<br>
 */
@Getter
@Accessors(fluent = true)
public class RequestDispatcher {
    /**
     * A mapping between regular expression patterns and custom rate limit configurations
     * specific to each pattern.
     * <p>
     * This map is used to define and apply additional custom rate limiting rules for HTTP
     * requests
     */
    private final HashMap<Pattern, AdditionalCustomRateLimit[]> additionalCustomRateLimits = new HashMap<>();
    /**
     * Represents the web server instance used by the RequestDispatcher.
     * It is used to dispatch and manage incoming HTTP requests and responses.
     * This variable is initialized through the constructor and is immutable.
     */
    private final WebServer webServer;
    /**
     * A mapping of regular expression patterns to their corresponding method handlers.
     * The patterns are compiled from HTTP request paths registered through the dispatcher.
     * Each mapped {@link MethodHandler} is responsible for handling a specific HTTP request
     * based on the pattern it matches. This map serves as the core routing mechanism used
     * by the {@link RequestDispatcher} to delegate requests to the appropriate handlers.
     */
    private final LinkedHashMap<Pattern, MethodHandler> handlers = new LinkedHashMap<>();
    private final List<FieldRoute> fieldRoutes = new ArrayList<>();

    /**
     * Represents a flag to determine whether preflight requests (e.g., CORS preflight OPTIONS requests)
     * should be filtered and handled automatically.
     * <p>
     * When set to {@code true}, the system processes and responds to preflight requests, enabling
     * functionalities such as setting appropriate CORS headers or bypassing certain processing pipelines.
     * When set to {@code false}, such requests are passed down to the later request handling logic without filtering.
     */
    @Setter
    private boolean filterPrefireRequests = true;

    /**
     * Constructs a new RequestDispatcher with the specified WebServer instance.
     * The RequestDispatcher manages the routing of HTTP requests and delegates them
     * to registered handlers based on their paths.
     *
     * @param webServer the WebServer instance used to process and dispatch HTTP requests
     */
    public RequestDispatcher(WebServer webServer) {
        this.webServer = webServer;
    }

    /**
     * Registers all methods annotated with {@code @RequestHandler} from the provided instance.
     * These methods are stored as handlers that can be invoked based on incoming HTTP request paths.
     *
     * @param instance the object containing methods annotated with {@code @RequestHandler}
     */
    public void register(Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            if (method.isAnnotationPresent(FieldRequestHandler.class)) {
                FieldRequestHandler fieldRequestHandler = method.getAnnotation(FieldRequestHandler.class);
                method.setAccessible(true);
                fieldRoutes.add(new FieldRoute(
                        instance,
                        method,
                        fieldRequestHandler.path(),
                        fieldRequestHandler.method(),
                        fieldRequestHandler.headerName(),
                        FieldSelection.normalize(fieldRequestHandler.required()),
                        FieldSelection.normalize(fieldRequestHandler.optional()),
                        FieldSelection.normalize(fieldRequestHandler.defaults())
                ));
                Logger.debug("The method " + method.getName() + " from " + instance.getClass().getSimpleName() + " is annotated with " + fieldRequestHandler.path() + " and registered as a FieldRequestHandler.");
                continue;
            }
            if (method.isAnnotationPresent(RequestHandler.class)) {
                RequestHandler annotation = method.getAnnotation(RequestHandler.class);
                String path = annotation.path();
                RequestMethod requestMethod = annotation.method();
                Pattern pattern = createPattern(path);

                CustomRateLimit[] customRateLimits = method.getAnnotationsByType(CustomRateLimit.class);

                if (customRateLimits.length > 0) {
                    Logger.debug("The method " + method.getName() + " from " + instance.getClass().getSimpleName() + " is annotated with " + customRateLimits.length + " @CustomRateLimit(s).");

                    AdditionalCustomRateLimit[] currentAdditionalCustomRateLimits = new AdditionalCustomRateLimit[customRateLimits.length];
                    for (int i = 0; i < customRateLimits.length; i++) {
                        currentAdditionalCustomRateLimits[i] = new AdditionalCustomRateLimit(customRateLimits[i]);
                    }
                    additionalCustomRateLimits.put(pattern, currentAdditionalCustomRateLimits);
                }


                MethodHandler methodHandler = new MethodHandler(path, requestMethod, instance, method, annotation.description());
                methodHandler.accessLevel = annotation.accesslevel();
                handlers.put(pattern, methodHandler);
                resortHandlers();
            }

        }
    }

    private void resortHandlers() {
        LinkedHashMap<Pattern, MethodHandler> temp = new LinkedHashMap<>(handlers);
        handlers.clear();
        temp.entrySet().stream().sorted(
                (a, b) -> {
                    String pa = a.getValue().path();
                    String pb = b.getValue().path();
                    int dynA = countDynamicSegments(pa);
                    int dynB = countDynamicSegments(pb);
                    if (dynA != dynB) return Integer.compare(dynA, dynB);
                    int segA = countSegments(pa);
                    int segB = countSegments(pb);
                    if (segA != segB) return Integer.compare(segB, segA);
                    return pa.compareTo(pb);
                }
        ).forEachOrdered(e -> handlers.put(e.getKey(), e.getValue()));
    }

    private int countDynamicSegments(String path) {
        String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int count = 0;
        for (String part : p.split("/")) {
            if (part.isEmpty()) continue;
            if (part.startsWith("{") && part.endsWith("}")) count++;
        }
        return count;
    }

    private int countSegments(String path) {
        String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int count = 0;
        for (String part : p.split("/")) if (!part.isEmpty()) count++;
        return count;
    }

    /**
     * Creates a regular expression pattern from the provided path string.
     * The path is processed to generate a regex that matches dynamic segments
     * enclosed in curly braces (e.g., {id}) and static segments as-is.
     * It ensures the resulting regex is properly escaped and suitable for path matching.
     *
     * @param path the input path string, typically containing static and/or dynamic segments
     * @return a compiled Pattern object representing the regex derived from the input path
     */
    private Pattern createPattern(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

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

        if (regex.charAt(regex.length() - 1) != '/') {
            regex.append("\\/");
        }
        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    /**
     * Handles HTTP requests by matching the given path against registered handlers
     * and invoking the corresponding handler logic. Supports authentication checks,
     * default header additions, and dynamic path variable resolution.
     *
     * @param path     the HTTP request path to be processed
     * @param request  the HTTP request object containing the request data
     * @param response the HTTP response object to be populated and sent back
     * @param callback the callback to notify the completion of request processing
     */
    public void handle(String path, Request request, Response response, Callback callback) {
        if (path.charAt(path.length() - 1) != '/')
            path += "/";

        var wasPreFireRequest = addDefaultHeaders(request, response, callback);

        if (wasPreFireRequest && filterPrefireRequests)
            return;

        for (FieldRoute r : fieldRoutes) {
            Pattern pattern = createPattern(r.path());
            Matcher matcher = pattern.matcher(path.trim());
            if (matcher.matches()) {
                if (!r.requestMethod().equals(RequestMethod.ANY) && !r.requestMethod().name().equals(request.getMethod().toUpperCase())) {
                    response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
                    callback.succeeded();
                    return;
                }

                Map<String, String> pathVariables = new HashMap<>();
                for (int i = 1; i <= matcher.groupCount(); i++)
                    pathVariables.put("arg" + i, matcher.group(i));


                try {
                    handleFieldRoute(r, request, response, callback, pathVariables);
                } catch (Exception e) {
                    Logger.error("An error occurred while invoking the method " + r.method().getName() + " of the class " + r.method().getClass().getName() + ".", e);
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    callback.succeeded();
                    return;
                }
                return;
            }
        }

        var entries = handlers.entrySet();
        for (Map.Entry<Pattern, MethodHandler> entry : entries) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(path.trim());
            if (matcher.matches()) {
                MethodHandler handler = entry.getValue();


                if (!handler.requestMethod().equals(RequestMethod.ANY) && !handler.requestMethod().name().equals(request.getMethod().toUpperCase())) {
                    response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
                    callback.succeeded();
                    return;
                }

                AuthenticationAnswer authenticationAnswer = null;
                switch (handler.accessLevel()) {
                    case DISABLED -> {
                        response.setStatus(HttpStatus.UNAUTHORIZED_401);
                        callback.succeeded();
                        return;
                    }
                    case AUTHENTICATED -> {
                        AuthenticationHandler authenticationHandler = webServer.authenticationHandler();

                        if (authenticationHandler == null) {
                            Logger.error("There is an request need to be authenticated, but there is no AuthenticationHandler. Declined request.");
                            response.setStatus(HttpStatus.UNAUTHORIZED_401);
                            callback.succeeded();
                            return;
                        }
                        authenticationAnswer = authenticationHandler.hasGeneralAccess(request, handler.accessLevel());

                        if (authenticationAnswer == null) {
                            Logger.error("There is an request need to be authenticated, but the AuthenticationAnswer is null. Declined request.");
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

                Map<String, String> pathVariables = new HashMap<>();

                for (int i = 1; i <= matcher.groupCount(); i++)
                    pathVariables.put("arg" + i, matcher.group(i));

                try {
                    handler.invoke(request, response, callback, authenticationAnswer, pathVariables);
                } catch (Exception e) {
                    Logger.error("An error occurred while invoking the method " + handler.method().getName() + " of the class " + handler.instance().getClass().getName() + ".", e);
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
     * Creates and returns a ContextHandler instance. The ContextHandler is responsible
     * for handling requests with a specified path prefix and delegating them to the
     * appropriate request handler logic within the RequestDispatcher class.
     * <p>
     * The newly created ContextHandler:
     * - Handles incoming HTTP requests using a Handler.
     * - Evaluates requests based on their path and invokes the corresponding handler logic.
     * - Returns a 404 NOT FOUND response if the path information is null or does not match any handler.
     *
     * @return a ContextHandler instance
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
     * Adds default headers to the response based on the provided request and response objects.
     * This method also handles preflight requests and sets appropriate HTTP headers for CORS (Cross-Origin Resource Sharing).
     * If the request method is "OPTIONS", it adjusts the response to indicate acceptance.
     *
     * @param request  the HTTP request being processed; used to retrieve headers and other request metadata.
     * @param response the HTTP response being constructed; modified to include default and necessary headers.
     * @param callback the callback to be notified when the operation is completed successfully.
     * @return true if the request was a preflight request and has been handled; false otherwise.
     */
    private boolean addDefaultHeaders(Request request, Response response, Callback callback) {
        if (!webServer.allowedOrigins().contains("*")) {
            response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        } else {
            Logger.debug("The request is a star request and credentials are not allowed.");
        }

        response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_METHODS, String.join(",", webServer.allowedMethods()));
        response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_HEADERS, String.join(",", webServer.allowedHeaders()));

        String origin = request.getHeaders().get("Origin");
        if (webServer.allowedOrigins().contains("*")) {
            response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        } else {
            if (origin != null) {
                if (webServer.allowedOrigins().contains(origin.toLowerCase())) {
                    response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                } else {
                    Logger.debug("The origin " + origin + " is not allowed.");
                }
            }
        }

        if (Objects.equals(request.getMethod(), "OPTIONS")) {
            response.setStatus(HttpStatus.ACCEPTED_202);
            callback.succeeded();
            return true;
        }
        return false;
    }


    private void handleFieldRoute(FieldRoute route, Request request, Response response, Callback callback,
                                  Map<String, String> pathVariables) throws Exception {
        Set<String> requested = FieldSelection.read(request, route.headerName());
        if (requested.isEmpty()) requested = route.defaults();

        LinkedHashSet<String> safe = new LinkedHashSet<>(route.required());
        for (String f : requested) {
            if (route.optional().contains(f)) safe.add(f);
        }

        Object result;
        Method m = route.method();
        Class<?>[] pts = m.getParameterTypes();
        Object[] args = new Object[pts.length];
        for (int i = 0; i < pts.length; i++) {
            Class<?> p = pts[i];
            if (p.isAssignableFrom(Request.class)) args[i] = request;
            else if (p.isAssignableFrom(Response.class)) args[i] = response;
            else if (p.isAssignableFrom(Callback.class)) args[i] = callback;
            else if (p.isAssignableFrom(Body.class)) args[i] = new Body(request);
            else if (String.class.isAssignableFrom(p)) {
                String paramName = m.getParameters()[i].getName();
                Object val = pathVariables.get(paramName);
                if (val == null) {
                    val = pathVariables.get("arg" + (i +1));
                }
                args[i] = val;
            } else args[i] = null;

        }
        result = m.invoke(route.instance(), args);

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

    private void writeJson(Response resp, Callback callback, Map<?, ?> map) throws Exception {
        resp.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json");
        ResponseUtil.writeAnswer(resp, callback, JsonUtil.toJsonObject(map));
        callback.succeeded();
    }
}
