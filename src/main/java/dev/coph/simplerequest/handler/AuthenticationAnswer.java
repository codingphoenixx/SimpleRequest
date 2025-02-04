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
@AllArgsConstructor
public class AuthenticationAnswer {
    private boolean hasAccess;
    private String message;
}
