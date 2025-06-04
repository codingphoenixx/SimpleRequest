package dev.coph.simplerequest.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.server.Request;

/**
 * Defines the contract for handling authentication and authorization processes.
 *
 * The {@code AuthenticationHandler} interface provides methods to check access
 * permissions and validate if specific actions or resources are allowed for a given
 * request or identifier.
 *
 * @param <T> the type of the object associated with the authentication result
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
    AuthenticationAnswer<T> hasAccess(RequestDispatcher.MethodHandler path, Request request, AccessLevel accessLevel);


    /**
     * Checks whether a specific permission is granted for a given identifier.
     *
     * @param permission the permission string to be checked, representing the action or resource to validate.
     * @param identifier the unique identifier, such as a user ID or role, to which the permission is being applied.
     * @return true if the specified permission is granted for the given identifier; false otherwise.
     */
    AuthenticationAnswer<T> hasPermission(String permission, String identifier);


    /**
     * Checks whether general access is permitted based on the provided HTTP request.
     * This method determines if the incoming request satisfies general access criteria
     * defined by the implementation.
     *
     * @param request the {@code Request} object containing details about the incoming HTTP request,
     *                such as headers, parameters, or user information
     * @return an {@code AuthenticationHandler<T>} instance representing the result of the
     *         general access check, including any relevant context or authorization data
     */
    AuthenticationAnswer<T> hasGeneralAccess(Request request, AccessLevel accessLevel);

}
