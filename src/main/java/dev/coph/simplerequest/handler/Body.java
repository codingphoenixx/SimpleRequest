package dev.coph.simplerequest.handler;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.json.JSONObject;

/**
 * Represents the body of an HTTP request. This class is commonly used to
 * encapsulate the content of a request body as a string.
 * <p>
 * This class is immutable and designed to simplify working with request bodies
 * in HTTP request handling scenarios.
 * <p>
 * Features:
 * - Fluent accessors for retrieving the body content.
 * - Constructed as a final class to ensure immutability.
 */
@Getter
@Accessors(fluent = true)
public class Body {

    /**
     * Constructs a new {@code Body} object with the specified content.
     *
     * @param content the content of the HTTP request body
     */
    public Body(String content) {
        this.content = content;
    }

    /**
     * Represents the content of the HTTP request body.
     * <p>
     * This variable is immutable and encapsulates the raw string data of the
     * request body. It is designed to provide a straightforward mechanism for
     * accessing and working with HTTP request body content in a structured manner.
     */
    private final String content;

    /**
     * Converts the content of the HTTP request body to a {@link JSONObject}.
     * If the body content is null or empty, an empty {@link JSONObject} is returned.
     *
     * @return a {@link JSONObject} representation of the HTTP request body content,
     *         or an empty {@link JSONObject} if the body content is null or empty
     */
    public JSONObject asJSON() {
        if (content == null || content.isEmpty())
            return new JSONObject();
        return new JSONObject(content);
    }

    /**
     * Returns a string representation of the HTTP request body content.
     * This method provides direct access to the raw content contained
     * within the body.
     *
     * @return the content of the HTTP request body as a string
     */
    @Override
    public String toString() {
        return content;
    }
}
