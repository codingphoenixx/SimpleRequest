package dev.coph.simplerequest.handler;

/**
 * Represents the source type of a parameter in an HTTP request.
 * <p>
 * This enumeration defines the possible locations from which a parameter
 * can be extracted when processing an HTTP request. It is typically used
 * in routing or request-handling mechanisms to specify how parameters
 * should be retrieved.
 *<br>
 * Enumerated Values:<br>
 * - {@code PATH}: Indicates that the parameter is extracted from the path
 *   of the request URL.<br>
 * - {@code QUERY}: Indicates that the parameter is extracted from the query
 *   string of the request URL.<br>
 * - {@code HEADER}: Indicates that the parameter is extracted from the
 *   headers of the HTTP request.<br>
 * - {@code COOKIE}: Indicates that the parameter is extracted from cookies
 *   included in the HTTP request.<br>
 */
public enum ParamInput {
    /**
     * Indicates that the parameter is extracted from the path of the request URL.
     * <p>
     * This value is typically used in the context of HTTP request routing to specify
     * that a parameter is resolved from the path segment of the URL. For example, 
     * dynamic URL segments defined in route patterns (e.g., "/user/{id}") correspond 
     * to path parameters.
     */
    PATH,
    /**
     * Indicates that the parameter is extracted from the query string of the request URL.
     * <p>
     * This value is typically used to specify that a parameter is retrieved from the
     * URL's query string, which follows the "?" character. Query parameters are often used
     * to pass additional data in HTTP requests, typically in key-value pair format.
     */
    QUERY,
    /**
     * Indicates that the parameter is extracted from the headers of the HTTP request.
     * <p>
     * This value is typically used to specify that a parameter is retrieved from the
     * HTTP request's headers. Headers often contain metadata or operational information,
     * such as authentication tokens, content types, or custom application-defined data.
     */
    HEADER,
    /**
     * Indicates that the parameter is extracted from cookies included in the HTTP request.
     * <p>
     * This value is typically used to specify that a parameter is retrieved from cookies
     * sent by the client within the HTTP request. Cookies are often used to store
     * session data, user preferences, or other client-side information that needs
     * to be accessed by the server.
     */
    COOKIE
}