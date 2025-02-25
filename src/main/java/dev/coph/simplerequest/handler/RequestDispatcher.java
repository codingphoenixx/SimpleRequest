package dev.coph.simplerequest.handler;


import dev.coph.simplelogger.Logger;
import dev.coph.simplerequest.server.WebServer;
import dev.coph.simplerequest.util.RequestUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code RequestDispatcher} class is responsible for routing and handling HTTP requests
 * in a web server environment. It provides methods for registering request handlers based
 * on specific patterns and managing incoming requests by delegating them to the appropriate
 * registered handler.
 * <p>
 * The class supports features such as:
 * - Dynamic routing with path variable resolution.
 * - Filtering of preflight (CORS) requests.
 * - Authentication checks for secured endpoints.
 * - Integration with {@link WebServer} for handling HTTP processing.
 */
@Getter
@Accessors(fluent = true)
public class RequestDispatcher {
    /**
     * Represents the web server instance utilized by the RequestDispatcher.
     * It is used to dispatch and manage incoming HTTP requests and responses.
     * This variable is initialized through the constructor and is immutable.
     */
    private final WebServer webServer;

    /**
     * Represents a flag to determine whether preflight requests (e.g., CORS preflight OPTIONS requests)
     * should be filtered and handled automatically.
     * <p>
     * When set to {@code true}, the system processes and responds to preflight requests, enabling
     * functionalities such as setting appropriate CORS headers or bypassing certain processing pipelines.
     * When set to {@code false}, such requests are passed down to the subsequent request handling logic without filtering.
     */
    @Setter
    private boolean filterPrefireRequests = true;

    /**
     * A mapping of regular expression patterns to their corresponding method handlers.
     * The patterns are compiled from HTTP request paths registered through the dispatcher.
     * Each mapped {@link MethodHandler} is responsible for handling a specific HTTP request
     * based on the pattern it matches. This map serves as the core routing mechanism used
     * by the {@link RequestDispatcher} to delegate requests to the appropriate handlers.
     */
    private final Map<Pattern, MethodHandler> handlers = new HashMap<>();

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
            if (method.isAnnotationPresent(RequestHandler.class)) {
                RequestHandler annotation = method.getAnnotation(RequestHandler.class);
                String path = annotation.path();
                Pattern pattern = createPattern(path);
                MethodHandler methodHandler = new MethodHandler(path, annotation.receiveBody(), instance, method);

                methodHandler.needAuth = annotation.needAuth();
                handlers.put(pattern, methodHandler);
            }
        }
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
     * @throws Exception if an error occurs during request handling or method invocation
     */
    public void handle(String path, Request request, Response response, Callback callback) throws Exception {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        var wasPreFireRequest = addDefaultHeaders(request, response, callback);
        if (wasPreFireRequest && filterPrefireRequests) {
            Logger.getInstance().debug("The Request filtered out because it was a prefire request.");
            return;
        }
        for (Map.Entry<Pattern, MethodHandler> entry : handlers.entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(path.trim());
            if (matcher.matches()) {
                MethodHandler handler = entry.getValue();

                AuthenticationAnswer authenticationAnswer = null;
                if (handler.needAuth) {
                    AuthenticationHandler authenticationHandler = webServer.authenticationHandler();

                    if (authenticationHandler == null) {
                        Logger.getInstance().error("There is an request need to be authenticated, but there is no AuthenticationHandler. Declined request.");
                        response.setStatus(HttpStatus.UNAUTHORIZED_401);
                        callback.succeeded();
                        return;
                    }
                    authenticationAnswer = authenticationHandler.hasAccess(handler, request);

                    if (authenticationAnswer == null) {
                        Logger.getInstance().error("There is an request need to be authenticated, but the AuthenticationAnswer is null. Declined request.");
                        response.setStatus(HttpStatus.UNAUTHORIZED_401);
                        callback.succeeded();
                        return;
                    }

                    if (!authenticationAnswer.hasAccess()) {
                        response.setStatus(HttpStatus.UNAUTHORIZED_401);
                        RequestUtil.writeAnswer(response, callback, authenticationAnswer.message());
                        callback.succeeded();
                        return;
                    }
                }

                Map<String, String> pathVariables = new HashMap<>();

                for (int i = 1; i <= matcher.groupCount(); i++) {
                    pathVariables.put("arg" + i, matcher.group(i));
                }
                handler.invoke(request, response, callback, authenticationAnswer, pathVariables);
                if (!response.getHeaders().contains(HttpHeader.CONTENT_TYPE)) {
                    response.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json;charset=utf-8");
                }
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
            public boolean handle(Request request, Response response, Callback callback) throws Exception {
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
        response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_METHODS, "GET,PUT,POST,OPTIONS");
        response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept, Authorization");
        String origin = request.getHeaders().get("Origin");
        if (origin != null && webServer.allowedOrigins().contains(origin.toLowerCase())) {
            response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        }
        if (Objects.equals(request.getMethod(), "OPTIONS")) {
            response.setStatus(HttpStatus.ACCEPTED_202);
            callback.succeeded();
            return true;
        }
        return false;
    }


    /**
     * Represents a handler responsible for invoking a specific method mapped to an HTTP request path.
     * The handler encapsulates method details, the associated instance, and configuration
     * related to authentication and the request body.
     */
    @Getter
    @Accessors(fluent = true, chain = true)
    public static class MethodHandler {
        private final String path;
        private boolean needAuth = false;
        private final boolean receiveBody;
        private final Object instance;
        private final Method method;

        /**
         * Constructs a MethodHandler instance that associates a method with a specific HTTP request path,
         * indicating whether the HTTP body content should be received, and maintaining the instance and method to invoke.
         *
         * @param path        the HTTP path with which this method handler is associated
         * @param receiveBody whether the HTTP request body should be received by this method handler
         * @param instance    the instance on which the method will be invoked
         * @param method      the method to be invoked in response to HTTP requests
         */
        public MethodHandler(String path, boolean receiveBody, Object instance, Method method) {
            this.path = path;
            this.receiveBody = receiveBody;
            this.instance = instance;
            this.method = method;
        }


        /**
         * Invokes the specified method associated with the instance of this handler,
         * dynamically resolving and mapping the required parameters. This method processes
         * the input parameters and invokes the encapsulated method based on the defined
         * request and configuration.
         *
         * @param request              the HTTP request to process
         * @param response             the HTTP response to send
         * @param callback             the callback function to notify upon operation completion
         * @param authenticationAnswer an object representing the result of the authentication process
         * @param pathVariables        a map of path variable names to their corresponding values
         * @throws Exception if an error occurs during method invocation
         */
        public void invoke(Request request, Response response, Callback callback, AuthenticationAnswer authenticationAnswer, Map<String, String> pathVariables) throws Exception {
            Parameter[] parameterTypes = method.getParameters();
            Object[] parameters = new Object[parameterTypes.length];

            int args = 1;
            for (int i = 0; i < parameterTypes.length; i++) {
                Parameter parameter = parameterTypes[i];
                if (receiveBody && Body.class.isAssignableFrom(parameter.getType())) {
                    parameters[i] = new Body(request);
                } else if (Request.class.isAssignableFrom(parameter.getType())) {
                    parameters[i] = request;
                } else if (AuthenticationAnswer.class.isAssignableFrom(parameter.getType())) {
                    parameters[i] = authenticationAnswer;
                } else if (Response.class.isAssignableFrom(parameter.getType())) {
                    parameters[i] = response;
                } else if (Callback.class.isAssignableFrom(parameter.getType())) {
                    parameters[i] = callback;
                } else if (MethodHandler.class.isAssignableFrom(parameter.getType())) {
                    parameters[i] = this;
                } else if (String.class.isAssignableFrom(parameter.getType())) {
                    String paramName = method.getParameters()[args].getName();
                    parameters[i] = pathVariables.get(paramName);
                    args++;
                }
            }

            method.invoke(instance, parameters);
        }
    }
}
