package dev.coph.simplerequest.handler;

/**
 * Enum representing HTTP request methods.
 *
 * This enum is used to define the various types of HTTP request methods
 * that are commonly utilized in web communication and API interactions.
 *<br><br>
 * Each constant represents a specific HTTP request method:<br>
 * - GET: Retrieves data from a server.<br>
 * - POST: Sends data to be processed to a server.<br>
 * - PUT: Updates or replaces data on a server.<br>
 * - DELETE: Removes data from a server.<br>
 * - PATCH: Partially updates data on a server.<br>
 * - OPTIONS: Fetches information about the communication options available.<br>
 * - ANY: Matches any type of HTTP request method.<br>
 */
public enum RequestMethode {
    /**
     * Enum constant representing the HTTP GET request method.
     *
     * Used to retrieve data from the server without causing any side effects.
     * The GET method is commonly used to request data, such as fetching resources
     * or retrieving information.
     */
    GET,
    /**
     * Enum constant representing the HTTP POST request method.
     *
     * The POST method is used to send data to the server to create or update a resource.
     * It is commonly used for operations that require data to be processed by the server,
     * such as submitting forms or uploading files. The data sent via a POST request
     * is typically included in the body of the request.
     */
    POST,
    /**
     * Enum constant representing the HTTP PUT request method.
     *
     * The PUT method is used to update or replace an existing resource on the server.
     * Typically, a PUT request includes the full details of the resource to be created
     * or updated in the body of the request. If the resource does not exist, the server
     * may create a new resource, depending on its implementation.
     */
    PUT,
    /**
     * Enum constant representing the HTTP DELETE request method.
     *
     * The DELETE method is used to request the removal of a specific resource
     * from the server. It is commonly utilized to delete resources identified
     * by a given URI. The operation is expected to be idempotent, meaning multiple
     * identical DELETE requests should have the same effect as a single request.
     */
    DELETE,
    /**
     * Enum constant representing the HTTP PATCH request method.
     *
     * The PATCH method is used to partially update an existing resource on the server.
     * Unlike the PUT method, which replaces an entire resource, the PATCH method applies
     * partial modifications to an existing resource using the data provided in the request.
     * It is commonly used for updates where only specific fields or properties of a resource
     * need to be changed without altering the entire resource.
     */
    PATCH,
    /**
     * Enum constant representing the HTTP OPTIONS request method.
     *
     * The OPTIONS method is used to describe the communication options available
     * for a specific resource or server. It allows the client to determine
     * the supported HTTP methods and other capabilities of a server or resource.
     * This method does not typically involve any modification or retrieval of
     * resource data. It is primarily used for discovering server capabilities
     * and constraints.
     */
    OPTIONS,
    /**
     * Represents the HTTP QUERY request method in the context of server routing or request handling.
     * This constant is primarily used to specify or match HTTP requests that use the QUERY method.
     * While not a standard HTTP method, it might be defined for custom purposes or specific application logic.
     */
    QUERY,
    /**
     * Represents the HTTP HEAD method.
     * The HEAD method is used to request the headers of a resource, similar
     * to a GET request, but without the response body. This is typically
     * used to check for the existence or status of a resource.
     */
    HEAD,
    /**
     * Represents a catch-all HTTP request method.
     * Used to match requests of any HTTP method, typically when the specific
     * method (e.g., GET, POST, PUT, etc.) is not predefined or when handling
     * requests in a generic manner.
     */
    ANY;


}
