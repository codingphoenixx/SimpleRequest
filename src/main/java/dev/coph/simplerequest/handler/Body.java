package dev.coph.simplerequest.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Represents the body of an HTTP request. This class is commonly used to
 * encapsulate the content of a request body as a string.
 *
 * This class is immutable and designed to simplify working with request bodies
 * in HTTP request handling scenarios.
 *
 * Features:
 * - Fluent accessors for retrieving the body content.
 * - Constructed as a final class to ensure immutability.
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class Body {

    /**
     * Represents the content of the HTTP request body.
     *
     * This variable is immutable and encapsulates the raw string data of the
     * request body. It is designed to provide a straightforward mechanism for
     * accessing and working with HTTP request body content in a structured manner.
     */
    private final String content;
}
