package dev.coph.simplerequest.ratelimit;

/**
 * Represents a custom rate-limiting configuration with a specific key, request limit,
 * time window in milliseconds and algorithm.
 * <p>
 * This class is designed to encapsulate the properties of a rate-limiting rule and can
 * be constructed directly or by using a CustomRateLimit annotation.
 *
 * @param key              The Key for the rate limit that should be enforced
 * @param maxRequests      The maximum requests of a user that can be triggered in a specific timeframe
 * @param timeWindowMillis The millis of how long the timeframe should be
 * @param algorithm        The algorithm used to enforce the rate limit
 */
public record AdditionalCustomRateLimit(String key, int maxRequests, long timeWindowMillis,
                                        RateLimitAlgorithm algorithm) {

    /**
     * Backwards-compatible constructor without algorithm; defaults to USER_FIXED_WINDOW.
     *
     * @param key              the unique identifier for the custom rate-limiting rule
     * @param maxRequests      the maximum number of requests allowed within the time window
     * @param timeWindowMillis the duration of the time window in milliseconds
     */
    public AdditionalCustomRateLimit(String key, int maxRequests, long timeWindowMillis) {
        this(key, maxRequests, timeWindowMillis, RateLimitAlgorithm.USER_FIXED_WINDOW);
    }

    /**
     * Constructs an AdditionalCustomRateLimit instance using the properties of a CustomRateLimit annotation.
     * <p>
     * This constructor extracts the key, maximum requests, time window and algorithm values from the provided CustomRateLimit
     * annotation and uses them to initialize a corresponding AdditionalCustomRateLimit instance.
     *
     * @param customRateLimit the CustomRateLimit annotation instance from which to extract the rate-limiting properties
     */
    public AdditionalCustomRateLimit(CustomRateLimit customRateLimit) {
        this(customRateLimit.key(), customRateLimit.maxRequests(), customRateLimit.timeWindowMillis(), customRateLimit.algorithm());
    }

    /**
     * Canonical constructor.
     *
     * @param key              the unique identifier for the custom rate-limiting rule
     * @param maxRequests      the maximum number of requests allowed within the time window
     * @param timeWindowMillis the duration of the time window in milliseconds
     * @param algorithm        the algorithm to use
     */
    public AdditionalCustomRateLimit {
    }
}
