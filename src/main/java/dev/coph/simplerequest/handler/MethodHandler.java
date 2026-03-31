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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * The {@code MethodHandler} class is responsible for handling the invocation
 * of methods annotated with specific endpoints in a web server context.
 * It processes requests and prepares the appropriate parameters based
 * on the method's signature before invoking the target method.
 * <p>
 * This class is designed to handle instance methods, their metadata, and
 * execution logic, including parameter resolution and error handling.
 */
@Getter
@Accessors(fluent = true, chain = true)
public class MethodHandler {
    private static final Logger logger = Logger.of("WebServer");

    private static final byte T_BODY = 1;
    private static final byte T_REQUEST = 2;
    private static final byte T_AUTH = 3;
    private static final byte T_RESPONSE = 4;
    private static final byte T_CALLBACK = 5;
    private static final byte T_SELF = 6;
    private static final byte T_PATH_VAR = 7;
    private static final byte T_NULL = 0;

    private final String path;
    private final RequestMethod requestMethod;
    private final Object instance;
    private final Method method;
    private final String description;
    private final byte[] paramSlots;
    private final int[] pathVarIndices;
    private final MethodHandle methodHandle;

    /**
     * Represents the access level required to invoke methods within the MethodHandler.
     * This field determines the accessibility of methods and resources handled
     * by this instance, based on the defined {@link AccessLevel}.
     * <p>
     * Possible values include:
     * - {@link AccessLevel#PUBLIC}: The method is accessible without authentication.
     * - {@link AccessLevel#AUTHENTICATED}: Requires authentication to access the method.
     * - {@link AccessLevel#SYSTEM}: Restricted to system-level access or internal operations.
     * - {@link AccessLevel#DISABLED}: Indicates the method is disabled or unavailable.
     * <p>
     * This field is set during the construction or configuration of the MethodHandler
     * and is used to enforce access control policies.
     */
    protected AccessLevel accessLevel = AccessLevel.PUBLIC;

    /**
     * Constructs an instance of the MethodHandler class, which handles method details
     * such as accessible path, HTTP request type, associated instance, and related logic.
     *
     * @param path the URL path associated with the method
     * @param requestMethod the HTTP request method (e.g., GET, POST) tied to the handler
     * @param instance the object instance that owns the specified method
     * @param method the method to be invoked by this handler
     * @param description a description of the method's purpose or behavior
     */
    public MethodHandler(String path, RequestMethod requestMethod, Object instance, Method method, String description) {
        this.path = path;
        this.requestMethod = requestMethod;
        this.instance = instance;
        this.method = method;
        this.description = description;

        method.setAccessible(true);
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().unreflect(method).bindTo(instance);
        } catch (IllegalAccessException e) {
            mh = null;
        }
        this.methodHandle = mh;

        Parameter[] params = method.getParameters();
        paramSlots = new byte[params.length];
        pathVarIndices = new int[params.length];
        int pathIdx = 1;
        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();
            if (Body.class.isAssignableFrom(type)) {
                paramSlots[i] = T_BODY;
            } else if (Request.class.isAssignableFrom(type)) {
                paramSlots[i] = T_REQUEST;
            } else if (AuthenticationAnswer.class.isAssignableFrom(type)) {
                paramSlots[i] = T_AUTH;
            } else if (Response.class.isAssignableFrom(type)) {
                paramSlots[i] = T_RESPONSE;
            } else if (Callback.class.isAssignableFrom(type)) {
                paramSlots[i] = T_CALLBACK;
            } else if (MethodHandler.class.isAssignableFrom(type)) {
                paramSlots[i] = T_SELF;
            } else if (String.class.isAssignableFrom(type)) {
                paramSlots[i] = T_PATH_VAR;
                pathVarIndices[i] = pathIdx++;
            } else {
                paramSlots[i] = T_NULL;
            }
        }
    }

    /**
     * Invokes a method with appropriate parameters derived from the provided inputs.
     * This method handles the preparation of method arguments based on predefined
     * parameter slots and subsequently executes the target method.
     * If an error occurs during the invocation, it logs the error and sets the response
     * status to indicate an internal server error.
     *
     * @param request the HTTP request object containing information about the client's request
     * @param response the HTTP response object used to send a response back to the client
     * @param callback an object implementing the callback interface to handle asynchronous operations
     * @param authenticationAnswer the result of an authentication process, indicating access control status
     * @param pathVariables a map of path variables extracted from the URL, used for dynamic parameter resolution
     */
    public void invoke(Request request, Response response, Callback callback, AuthenticationAnswer authenticationAnswer, Map<String, String> pathVariables) {
        Object[] parameters = new Object[paramSlots.length];
        for (int i = 0; i < paramSlots.length; i++) {
            switch (paramSlots[i]) {
                case T_BODY -> parameters[i] = new Body(request);
                case T_REQUEST -> parameters[i] = request;
                case T_AUTH -> parameters[i] = authenticationAnswer;
                case T_RESPONSE -> parameters[i] = response;
                case T_CALLBACK -> parameters[i] = callback;
                case T_SELF -> parameters[i] = this;
                case T_PATH_VAR -> parameters[i] = pathVariables.get("arg" + pathVarIndices[i]);
                default -> parameters[i] = null;
            }
        }
        try {
            if (methodHandle != null) {
                methodHandle.invokeWithArguments(parameters);
            } else {
                method.invoke(instance, parameters);
            }
        } catch (Throwable e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            logger.error("An error occurred while invoking the method " + method.getName() + " of the class " + instance.getClass().getName() + ".", cause);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            callback.succeeded();
        }
    }
}