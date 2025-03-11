package dev.coph.simplerequest.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Represents the result of an authentication process.
 * <p>
 * This class encapsulates information regarding whether access is granted
 * and provides an accompanying message that describes the result of the
 * authentication check.
 */
public class AuthenticationAnswer<T> {
    /**
     * Indicates whether access is granted as a result of the authentication process.
     *
     * This variable represents the outcome of an authentication check. A value of
     * {@code true} signifies that access is granted, while a value of {@code false}
     * indicates that access is denied.
     */
    private final boolean hasAccess;

    /**
     * Represents a generic object associated with the result of an authentication process.
     *
     * This variable is used to store additional information or a specific entity
     * related to the authentication result. The type of the object is defined
     * by the generic type parameter {@code T}.
     */
    private final T object;

    private final String message;

    /**
     * Constructs a new {@code AuthenticationAnswer} object with the specified
     * authentication result and associated object.
     *
     * @param hasAccess a boolean indicating whether access is granted; {@code true} if access is granted, {@code false} otherwise
     * @param object a generic object associated with the authentication result; can hold additional information or entity specific to the authentication process
     */
    public AuthenticationAnswer(boolean hasAccess, T object) {
        this.hasAccess = hasAccess;
        this.object = object;
        this.message = null;
    }

    /**
     * Constructs a new {@code AuthenticationAnswer} object with the specified
     * authentication result, associated object, and an accompanying message.
     *
     * @param hasAccess a boolean indicating whether access is granted; {@code true} if access is granted, {@code false} otherwise
     * @param object a generic object associated with the authentication result; can hold additional information or entity specific to the authentication process
     * @param message a string that provides a descriptive message about the authentication result
     */
    public AuthenticationAnswer(boolean hasAccess, T object, String message) {
        this.hasAccess = hasAccess;
        this.object = object;
        this.message = message;
    }

    /**
     * Determines whether access is granted based on the authentication process.
     *
     * @return true if access is granted, false otherwise
     */
    public boolean hasAccess() {
        return hasAccess;
    }

    /**
     * Retrieves the object associated with the authentication result.
     *
     * @return the generic object of type T associated with the authentication process
     */
    public T object() {
        return object;
    }

    /**
     * Retrieves the message associated with the authentication result.
     *
     * @return a string containing the message describing the authentication result, or null if no message is available
     */
    public String message() {
        return message;
    }
}
