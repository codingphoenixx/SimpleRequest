package dev.coph.simplerequest.handler;

/**
 * Enumeration representing the state of a request.
 * <p>
 * This enum is used to define the accessibility or status of a request. It can
 * be utilized in various scenarios, such as determining what level of access
 * an endpointdiscovery requires and controlling access to resources or methods based
 * on the current request state.
 * <p>
 * Enum constants:
 * - PUBLIC: Indicates the request is publicly accessible without any authentication.
 * - AUTHENTICATED: Indicates the request requires authentication for access.
 * - DISABLED: Indicates the request is disabled or unavailable.
 */
public enum AccessLevel {
    /**
     * Enum constant representing a publicly accessible request state.
     * <p>
     * This state indicates that the resource, endpointdiscovery, or request is accessible
     * to anyone without requiring authentication. It is typically used to
     * designate open public access where no additional validation or permissions
     * are necessary.
     */
    PUBLIC,
    /**
     * Enum constant representing a state requiring authentication.
     * <p>
     * This state indicates that the request or resource requires
     * the user to be authenticated before access is granted. Typically
     * used for endpoints or operations where sensitive data or restricted
     * functionalities are involved, ensuring only authorized users can proceed.
     */
    AUTHENTICATED,
    /**
     * Enum constant representing a system-only access state.
     * <p>
     * This state indicates that the request or resource is accessible only
     * by the system itself or by internal mechanisms. It is typically used
     * for operations or endpoints that are meant to be restricted from
     * external access, ensuring strict control over specific functionalities
     * or sensitive operations within the system.
     */
    SYSTEM,
    /**
     * Enum constant representing a disabled or unavailable request state.
     * <p>
     * This state indicates that the request, resource, or endpointdiscovery is not currently
     * accessible or functional. It is typically used to denote a condition where
     * the request or operation is intentionally disabled, restricted, or no longer
     * in use. Requests or resources in this state should not be processed.
     */
    DISABLED

}
