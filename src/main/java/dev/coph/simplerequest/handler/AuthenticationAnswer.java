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
@Getter
@Accessors(fluent = true)
public class AuthenticationAnswer {
    /**
     * Indicates whether access is granted as a result of the authentication process.
     *
     * This variable represents the outcome of an authentication check. A value of
     * {@code true} signifies that access is granted, while a value of {@code false}
     * indicates that access is denied.
     */
    private boolean hasAccess;
    /**
     * Represents a descriptive message associated with the authentication result.
     *
     * This variable provides additional context or information related to
     * the success or failure of the authentication process. It may
     * include error details, success messages, or other relevant explanations.
     */
    private String message;

    /**
     * Constructs an AuthenticationAnswer instance.
     *
     * @param hasAccess a boolean indicating whether access is granted
     * @param message a string containing the message associated with the authentication result
     */
    public AuthenticationAnswer(boolean hasAccess, String message) {
        this.hasAccess = hasAccess;
        this.message = message;
    }
}
