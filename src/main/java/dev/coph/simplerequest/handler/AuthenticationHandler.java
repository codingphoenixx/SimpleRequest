package dev.coph.simplerequest.handler;

import org.eclipse.jetty.server.Request;

/**
 * Defines the contract for handling authentication and authorization processes.
 * <p>
 * The {@code AuthenticationHandler} interface provides methods to check access
 * permissions and validate if specific actions or resources are allowed for a given
 * request or identifier.
 *
 * @param <T> the type of the object associated with the authentication result
 */
public interface AuthenticationHandler<T> {

    /**
     * Determines whether the specified request has access based on the provided path and access level.
     * <p>
     * This method evaluates whether the endpoint or resource represented by the given
     * {@code path} can be accessed by the requester, considering the details of the request
     * and the required access level. The result of this evaluation is encapsulated in an
     * {@code AuthenticationAnswer} instance.
     *
     * @param path        the {@code RequestDispatcher.MethodHandler} object representing the endpoint or resource to evaluate
     * @param request     the {@code Request} object containing details about the request, such as headers and parameters
     * @param accessLevel the {@code AccessLevel} that specifies the required permission level for the requested operation
     * @return an {@code AuthenticationAnswer<T>} instance containing the result of the access evaluation, including whether access was granted and any relevant context
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
     * Determines if a general access condition is met for the provided request and access level.
     * <p>
     * This method evaluates whether the given {@code Request} meets the criteria for a specified
     * {@code AccessLevel}, and returns an {@code AuthenticationAnswer} indicating the result of
     * the evaluation. The access determination depends on the implementation and may consider
     * factors like authentication, permissions, and resource configuration.
     *
     * @param request     the {@code Request} object containing details about the request, such as headers or parameters
     * @param accessLevel the {@code AccessLevel} specifying the required permission level to grant access
     * @return an {@code AuthenticationAnswer<T>} instance representing whether the access is granted and additional context
     */
    AuthenticationAnswer<T> hasGeneralAccess(Request request, AccessLevel accessLevel);

}
