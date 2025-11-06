package dev.coph.simplerequest.handler;

import dev.coph.simplelogger.Logger;
import dev.coph.simplerequest.authentication.AccessLevel;
import dev.coph.simplerequest.authentication.AuthenticationAnswer;
import dev.coph.simplerequest.body.Body;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Represents a handler responsible for invoking a specific method mapped to an HTTP request path.
 * The handler encapsulates method details, the associated instance, and configuration
 * related to authentication and the request body.
 */
@Getter
@Accessors(fluent = true, chain = true)
public class MethodHandler {
    private final String path;
    private final RequestMethod requestMethod;
    private final Object instance;
    private final Method method;
    private final String description;
    /**
     * Represents the access level associated with a method handler.
     *
     * This variable defines the visibility or accessibility requirements for
     * a specific method handler. It determines whether the handler can be used
     * by the public, authenticated users, system processes, or if it is
     * disabled.
     *
     * Possible values are defined by the {@link AccessLevel} enum:
     * - PUBLIC: The handler is available to all users without authentication.
     * - AUTHENTICATED: The handler requires authentication for access.
     * - SYSTEM: The handler is restricted to internal system usage.
     * - DISABLED: The handler is inactive and cannot be accessed.
     *
     * Default value: {@code AccessLevel.PUBLIC}.
     *
     * Accessibility: Protected - can be accessed within its class,
     * subclasses, and within the package.
     */
    protected AccessLevel accessLevel = AccessLevel.PUBLIC;

    /**
     * Constructs a new MethodHandler instance with the provided parameters.
     *
     * @param path          the URI path this handler corresponds to
     * @param requestMethod the HTTP request method associated with this handler
     * @param instance      the object instance containing the method to be invoked
     * @param method        the method to be executed for this handler
     * @param description   a brief description of this handler's purpose or functionality
     */
    public MethodHandler(String path, RequestMethod requestMethod, Object instance, Method method, String description) {
        this.path = path;
        this.requestMethod = requestMethod;
        this.instance = instance;
        this.method = method;
        this.description = description;
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
     */
    public void invoke(Request request, Response response, Callback callback, AuthenticationAnswer authenticationAnswer, Map<String, String> pathVariables) {
        Parameter[] parameterTypes = method.getParameters();
        Object[] parameters = new Object[parameterTypes.length];

        int args = 1;
        for (int i = 0; i < parameterTypes.length; i++) {
            Parameter parameter = parameterTypes[i];
            if (Body.class.isAssignableFrom(parameter.getType())) {
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
        try {
            method.invoke(instance, parameters);
        } catch (InvocationTargetException e) {
            Logger.error("An error occurred while invoking the method " + method.getName() + " of the class " + instance.getClass().getName() + ".");
            Logger.error(e.getCause());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            callback.succeeded();
        } catch (Exception e) {
            Logger.error("An error occurred while invoking the method " + method.getName() + " of the class " + instance.getClass().getName() + ".");
            Logger.error(e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            callback.succeeded();
        }
    }
}
