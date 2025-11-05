package dev.coph.simplerequest.security.jwt;

/**
 * Exception thrown to indicate issues related to JWT processing or validation.
 * This class extends {@code Exception}, providing constructors to specify
 * an error message and optional cause.
 */
public final class JwtException extends Exception {
    /**
     * Constructs a new JwtException with the specified detail message.
     *
     * @param msg the detail message providing additional information about the exception.
     */
    public JwtException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new JwtException with the specified detail message and cause.
     *
     * @param msg   the detail message providing additional information about the exception.
     * @param cause the underlying cause of the exception, which may be null.
     */
    public JwtException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
