package dev.coph.simplerequest.handler;


import dev.coph.simplelogger.Logger;
import dev.coph.simplerequest.server.WebServer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestDispatcher {
    private final WebServer webServer;


    private final Map<Pattern, MethodHandler> handlers = new HashMap<>();

    public RequestDispatcher(WebServer webServer) {
        this.webServer = webServer;
    }

    public void register(Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            if (method.isAnnotationPresent(RequestHandler.class)) {
                RequestHandler annotation = method.getAnnotation(RequestHandler.class);
                String path = annotation.path();
                Pattern pattern = createPattern(path);
                handlers.put(pattern, new MethodHandler(path, instance, method));
            }
            if (method.isAnnotationPresent(AuthenticatedRequestHandler.class)) {
                AuthenticatedRequestHandler annotation = method.getAnnotation(AuthenticatedRequestHandler.class);
                String path = annotation.path();
                Pattern pattern = createPattern(path);
                MethodHandler methodHandler = new MethodHandler(path, instance, method);
                methodHandler.needAuth = true;
                methodHandler.permission = annotation.permission();
                handlers.put(pattern, methodHandler);
            }
        }
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
                    regex.append("(\\w+)");
                } else {
                    regex.append(Pattern.quote(part));
                }
            }
        }

        if (regex.charAt(regex.length() - 1) != '/') {
            regex.append("\\/");
        }
        regex.append("$");

        System.out.println(regex.toString());
        return Pattern.compile(regex.toString());
    }

    public void handle(String path, Request request, Response response, Callback callback) throws Exception {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        for (Map.Entry<Pattern, MethodHandler> entry : handlers.entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(path.trim());
            if (matcher.matches()) {
                MethodHandler handler = entry.getValue();

                if (handler.needAuth)
                    if (webServer.authenticationHandler() != null) {
                        if (!webServer.authenticationHandler().hasAccess(handler, request)) {
                            response.setStatus(HttpStatus.UNAUTHORIZED_401);
                            callback.succeeded();
                            return;
                        }
                    } else {
                        Logger.getInstance().error("There is an request need to be authenticated, but there is no AuthenticationHandler. Declined request.");
                        response.setStatus(HttpStatus.UNAUTHORIZED_401);
                        callback.succeeded();
                        return;
                    }

                Map<String, String> pathVariables = new HashMap<>();

                for (int i = 1; i <= matcher.groupCount(); i++) {
                    pathVariables.put("arg" + i, matcher.group(i));
                }
                handler.invoke(request, response, callback, pathVariables);
                response.setStatus(HttpStatus.OK_200);
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

    @Getter
    @Accessors(fluent = true, chain = true)
    public static class MethodHandler {
        private final String path;
        private String permission;
        private boolean needAuth = false;


        private final Object instance;
        private final Method method;

        public MethodHandler(String path, Object instance, Method method) {
            this.path = path;
            this.instance = instance;
            this.method = method;
        }

        public void invoke(Request request, Response response, Callback callback, Map<String, String> pathVariables) throws Exception {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];

            int args = 1;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (Request.class.isAssignableFrom(parameterTypes[i])) {
                    parameters[i] = request;
                } else if (Response.class.isAssignableFrom(parameterTypes[i])) {
                    parameters[i] = response;
                } else if (Callback.class.isAssignableFrom(parameterTypes[i])) {
                    parameters[i] = callback;
                } else if (String.class.isAssignableFrom(parameterTypes[i])) {
                    String paramName = method.getParameters()[args].getName();
                    parameters[i] = pathVariables.get(paramName);
                    args++;
                }
            }

            method.invoke(instance, parameters);
        }
    }
}
