package dev.coph.simplerequest.ratelimit;

/**
 * Represents a custom rate-limiting configuration with a specific key, request limit,
 * and time window in milliseconds.
 *
 * This class is designed to encapsulate the properties of a rate-limiting rule and can
 * be constructed directly or by utilizing a CustomRateLimit annotation.
 *
 * @param key The Key, for the ratelimit that should be enforced
 * @param maxRequests The maximum requests of a user that can be triggered in a specific timeframe
 * @param timeWindowMillis The millis of how long the timeframe should be.
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
