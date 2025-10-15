package dev.coph.simplerequest.handler;

import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
public class Body {
    private final Request request;
    private String stringContent;
    private ByteBuffer byteBufferContent;
    private InputStream inputStreamContent;

    /**
     * Constructs an instance of the Body class using the given HTTP request.
     *
     * @param request the HTTP request from which the body content is derived
     */
    public Body(Request request) {
        this.request = request;
    }

    /**
     * Determines if the HTTP request body is empty.
     *
     * @return true if the request body has a length of 0, false otherwise
     */
    public boolean isEmpty() {
        return request.getLength() == 0;
    }

    /**
     * Retrieves the content of the HTTP request body as a string using UTF-8 encoding.
     *
     * @return the content of the HTTP request body as a string
     * @throws IOException if an I/O error occurs while accessing the request body content
     */
    public String asString() throws IOException {
        if (byteBufferContent != null) {
            throw new IOException("Cannot convert body to ByteBuffer. Body is already read as ByteBuffer.");
        }
        if (inputStreamContent != null) {
            throw new IOException("Cannot convert body to ByteBuffer. Body is already read as InputStream.");
        }
        if (stringContent != null) {
            return stringContent;
        }
        stringContent = Content.Source.asString(request, StandardCharsets.UTF_8);
        return stringContent;
    }

    /**
     * Retrieves the content of the HTTP request body as a string using the specified charset.
     * This method allows for decoding the body content with a custom character encoding.
     *
     * @param charset the character set to use for decoding the request body content
     * @return the content of the HTTP request body as a string, decoded with the specified charset
     * @throws IOException if an I/O error occurs while accessing the request body content
     */
    public String asString(Charset charset) throws IOException {
        if (byteBufferContent != null) {
            throw new IOException("Cannot convert body to ByteBuffer. Body is already read as ByteBuffer.");
        }
        if (inputStreamContent != null) {
            throw new IOException("Cannot convert body to ByteBuffer. Body is already read as InputStream.");
        }
        if (stringContent != null) {
            return stringContent;
        }
        stringContent = Content.Source.asString(request, charset);
        return stringContent;
    }

    /**
     * Retrieves the content of the HTTP request body as a {@link ByteBuffer}.
     * This method provides access to the raw binary data of the request body,
     * allowing for efficient reading and processing.
     *
     * @return a {@link ByteBuffer} containing the content of the HTTP request body
     * @throws IOException if an I/O error occurs while accessing the request body content
     */
    public ByteBuffer asByteBuffer() throws IOException {
        if (stringContent != null) {
            throw new IOException("Cannot convert body to ByteBuffer. Body is already read as String.");
        }
        if (inputStreamContent != null) {
            throw new IOException("Cannot convert body to ByteBuffer. Body is already read as InputStream.");
        }
        if (byteBufferContent != null) {
            return byteBufferContent;
        }
        byteBufferContent = Content.Source.asByteBuffer(request);
        return byteBufferContent;
    }

    /**
     * Retrieves the content of the HTTP request body as an {@link InputStream}.
     * This method provides access to the body content in a stream format, allowing
     * for efficient processing of large data or binary content.
     *
     * @return an {@link InputStream} representing the content of the HTTP request body
     * @throws IOException if an I/O error occurs while accessing the request body content
     */
    public InputStream asInputStream() throws IOException {
        if (byteBufferContent != null) {
            throw new IOException("Cannot convert body to ByteBuffer. Body is already read as ByteBuffer.");
        }
        if (stringContent != null) {
            throw new IOException("Cannot convert body to ByteBuffer. Body is already read as String.");
        }
        if (inputStreamContent != null) {
            return inputStreamContent;
        }
        inputStreamContent = Content.Source.asInputStream(request);
        return inputStreamContent;
    }


    /**
     * Converts the content of the HTTP request body into a {@link JSONObject}.
     * If the body content is empty or null, an empty {@link JSONObject} is returned.
     *
     * @return a {@link JSONObject} representation of the HTTP request body content
     * @throws IOException if an I/O error occurs while accessing the request body content
     */
    public JSONObject asJSON() throws IOException {
        String content = asString();
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
        try {
            return asString();
        } catch (IOException e) {
            return null;
        }
    }
}
