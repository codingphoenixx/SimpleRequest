package dev.coph.simplerequest.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.server.Request;

/**
 * The AuthenticationHandler interface defines methods for managing and verifying access control
 * and permissions in an application.
 * <p>
 * It provides mechanisms to check if a user has access to a certain route or method
 * (based on an associated RequestDispatcher.MethodHandler) and whether specific
 * permissions are granted to perform an operation.
 */
public interface AuthenticationHandler<T> {

    /**
     * Determines whether access is permitted to a specific method handler
     * based on the provided request.
     *
     * @param path    the {@code RequestDispatcher.MethodHandler} representing the method or route
     *                that is being accessed.
     * @param request the {@code Request} object containing details about the incoming HTTP request,
     *                such as headers, parameters, and user information.
     * @return {@code true} if access is granted to the specified method handler;
     * {@code false} otherwise.
     */
    AuthenticationAnswer<T> hasAccess(RequestDispatcher.MethodHandler path, Request request);


    /**
     * Checks whether a specific permission is granted for a given identifier.
     *
     * @param permission the permission string to be checked, representing the action or resource to validate.
     * @param identifier the unique identifier, such as a user ID or role, to which the permission is being applied.
     * @return true if the specified permission is granted for the given identifier; false otherwise.
     */
    AuthenticationAnswer<T> hasPermission(String permission, String identifier);


}
