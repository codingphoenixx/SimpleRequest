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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Getter
@Accessors(fluent = true)
public class RequestDispatcher {
    private static final Logger logger = Logger.of("WebServer");
    private final HashMap<Pattern, AdditionalCustomRateLimit[]> additionalCustomRateLimits = new HashMap<>();
    private final WebServer webServer;
    private final LinkedHashMap<String, RouteEntry> handlers = new LinkedHashMap<>();
    private final List<FieldRoute> fieldRoutes = new ArrayList<>();

    @Setter
    private boolean filterPrefireRequests = true;

    public RequestDispatcher(WebServer webServer) {
        this.webServer = webServer;
    }

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
                    additionalCustomRateLimits.put(routeEntry.pattern(), currentAdditionalCustomRateLimits);
                }

                MethodHandler methodHandler = new MethodHandler(path, requestMethod, instance, method, annotation.description());
                methodHandler.accessLevel = annotation.accesslevel();

                if (routeEntry.methods().containsKey(requestMethod)) {
                    logger.warn("Duplicate handler for " + requestMethod + " " + path + " – overwriting.");
                }
                routeEntry.methods().put(requestMethod, methodHandler);

                resortHandlers();
            }
        }
    }

    private void resortHandlers() {
        LinkedHashMap<String, RouteEntry> temp = new LinkedHashMap<>(handlers);
        handlers.clear();
        temp.entrySet().stream().sorted(
                (a, b) -> {
                    String pa = a.getKey();
                    String pb = b.getKey();
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
                    logger.error("An error occurred while invoking the method " + r.method().getName() + " of the class " + r.method().getClass().getName() + ".", e);
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    callback.succeeded();
                    return;
                }
                return;
            }
        }

        for (Map.Entry<String, RouteEntry> entry : handlers.entrySet()) {
            RouteEntry routeEntry = entry.getValue();
            Matcher matcher = routeEntry.pattern().matcher(path.trim());
            if (matcher.matches()) {
                String incomingMethod = request.getMethod().toUpperCase();
                RequestMethod incomingRequestMethod = RequestMethod.fromString(incomingMethod);

                MethodHandler handler = routeEntry.methods().get(incomingRequestMethod);
                if (handler == null) {
                    handler = routeEntry.methods().get(RequestMethod.ANY);
                }

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
                            logger.error("There is an request need to be authenticated, but there is no AuthenticationHandler. Declined request.");
                            response.setStatus(HttpStatus.UNAUTHORIZED_401);
                            callback.succeeded();
                            return;
                        }
                        authenticationAnswer = authenticationHandler.hasGeneralAccess(request, handler.accessLevel());

                        if (authenticationAnswer == null) {
                            logger.error("There is an request need to be authenticated, but the AuthenticationAnswer is null. Declined request.");
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

    private boolean addDefaultHeaders(Request request, Response response, Callback callback) {
        if (!webServer.allowedOrigins().contains("*")) {
            response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        } else {
            logger.debug("The request is a star request and credentials are not allowed.");
        }

        response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_METHODS, String.join(",", webServer.allowedMethods()));
        response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_HEADERS, String.join(",", webServer.allowedHeaders()));

        String origin = request.getHeaders().get("Origin");
        if (webServer.allowedOrigins().contains("*")) {
            response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        } else {
            if (origin != null) {
                if (webServer.corsAllowLocalhost() && IPUtil.isLocal(request)) {
                    response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                } else if (webServer.allowedOrigins().contains(origin.toLowerCase())) {
                    response.getHeaders().add(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                } else {
                    logger.debug("The origin " + origin + " is not allowed.");
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
                    val = pathVariables.get("arg" + (i + 1));
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