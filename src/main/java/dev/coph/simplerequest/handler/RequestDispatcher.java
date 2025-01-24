package dev.coph.simplerequest.handler;


import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestDispatcher {

    private final Map<Pattern, MethodHandler> handlers = new HashMap<>();

    public void register(Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            if (method.isAnnotationPresent(RequestHandler.class)) {
                RequestHandler annotation = method.getAnnotation(RequestHandler.class);
                String path = annotation.path();
                Pattern pattern = createPattern(path);
                handlers.put(pattern, new MethodHandler(instance, method));
            }
        }
    }

    private Pattern createPattern(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0,path.length() - 1);
        }

        StringBuilder regex = new StringBuilder("^");
        for (String part : path.split("/")) {
            if (!part.isEmpty()) {
                regex.append("\\/");

                if (part.startsWith("{") && part.endsWith("}")) {
                    String varName = part.substring(1, part.length() - 1);
                    regex.append("([A-Za-z0-9]+)");
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

    public void handle(String path, Request request, Response response, Callback callback) throws Exception {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        for (Map.Entry<Pattern, MethodHandler> entry : handlers.entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(path.trim());
            if (matcher.matches()) {
                System.out.println("MATCH");
                System.out.println(matcher.groupCount());
                MethodHandler handler = entry.getValue();
                Map<String, String> pathVariables = new HashMap<>();

                for (int i = 1; i <= matcher.groupCount(); i++) {
                    pathVariables.put("arg" + i, matcher.group(i));
                    System.out.println(i + ": " + matcher.group(i));
                }
                handler.invoke(request, response, callback, pathVariables);
                return;
            }
        }
        response.setStatus(404);
    }

    private static class MethodHandler {
        private final Object instance;
        private final Method method;

        public MethodHandler(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
        }

        public void invoke(Request request, Response response, Callback callback, Map<String, String> pathVariables) throws Exception {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];

            int args= 1;
            for (int i = 0; i < parameterTypes.length; i++) {
                System.out.println(parameterTypes[i].getName());
                System.out.println(Request.class.isAssignableFrom(parameterTypes[i]));
                System.out.println(Response.class.isAssignableFrom(parameterTypes[i]));
                System.out.println(Callback.class.isAssignableFrom(parameterTypes[i]));
                System.out.println(String.class.isAssignableFrom(parameterTypes[i]));
                if (Request.class.isAssignableFrom(parameterTypes[i])) {
                    parameters[i] = request;
                } else if (Response.class.isAssignableFrom(parameterTypes[i])) {
                    parameters[i] = response;
                } else if (Callback.class.isAssignableFrom(parameterTypes[i])) {
                    parameters[i] = callback;
                } else if (String.class.isAssignableFrom(parameterTypes[i])) {
                    String paramName = method.getParameters()[args].getName();
                    System.out.println(paramName);
                    parameters[i] = pathVariables.get(paramName);
                    args++;
                }
            }

            System.out.println(Arrays.toString(parameters));
            method.invoke(instance, parameters);
        }
    }
}
