package dev.coph.simplerequest.ratelimit;

/**
 * Represents a custom rate-limiting configuration for a specific key, number of requests,
 * and time window duration. This class is designed to encapsulate and manage rate-limiting
 * properties that can be applied to methods or endpoints.
 */
public record AdditionalCustomRateLimit(String key, int maxRequests, long timeWindowMillis) {
    /**
     * Constructs an AdditionalCustomRateLimit instance using the properties of a CustomRateLimit annotation.
     *
     * This constructor extracts the key, maximum requests, and time window values from the provided CustomRateLimit
     * annotation and uses them to initialize a corresponding AdditionalCustomRateLimit instance.
     *
     * @param customRateLimit the CustomRateLimit annotation instance from which to extract the rate-limiting properties
     */
    public AdditionalCustomRateLimit(CustomRateLimit customRateLimit) {
        this(customRateLimit.key(), customRateLimit.maxRequests(), customRateLimit.timeWindowMillis());
    }

    /**
     * Constructs an AdditionalCustomRateLimit instance with specified parameters.
     *
     * The AdditionalCustomRateLimit is designed to represent a custom rate limiting rule,
     * including its identifying key, maximum number of requests allowed within a
     * specified time window, and the time window duration in milliseconds.
     *
     * @param key the unique identifier for the custom rate-limiting rule
     * @param maxRequests the maximum number of requests allowed within the time window
     * @param timeWindowMillis the duration of the time window in milliseconds
     */
    public AdditionalCustomRateLimit {
    }
}
