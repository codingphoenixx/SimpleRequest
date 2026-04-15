package dev.coph.simplerequest.ratelimit;

import dev.coph.simplerequest.server.WebServer;
import dev.coph.simplerequest.util.Time;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides functionality to enforce rate-limiting policies for different keys (e.g., users, IP addresses).
 * <p>
 * The RateLimitProvider class uses a combination of default rate-limiting parameters
 * (time window and maximum requests) and a dynamic mapping of contexts to {@link RateLimit} objects.
 * Each key (representing a unique context) is associated with a {@link RateLimit} object that governs
 * request rate management within a specified time window, ensuring controlled access to resources or services.
 * <p>
 * This class supports thread-safe request evaluation via the {@link #allowRequest(String, String)} method,
 * which evaluates whether a request adheres to the configured rate-limiting constraints.
 * A default configuration is applied to new contexts dynamically, ensuring flexibility
 * and simplicity in managing a wide range of rate-limiting scenarios.
 */
public class RateLimitProvider {
    private final WebServer webServer;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, RateLimit>> rateLimits = new ConcurrentHashMap<>();
    private final long defaultTimeWindow;
    /**
     * Represents the maximum number of requests allowed within a specified time window
     * for rate-limiting purposes.
     * <p>
     * This value is used throughout the rate-limiting logic to determine whether a particular entity
     * (e.g., a user or an IP address) has exceeded the allowed number of requests within the given
     * time span. It serves as a key configuration parameter in the enforcement of rate-limiting policies.
     * <p>
     * The value of this field is set during the instantiation of the containing class and remains
     * immutable thereafter, ensuring consistent behavior of the rate-limiting mechanism.
     */
    private final int maxRequests;

    /**
     * Constructs a RateLimitProvider instance with the supplied web server, time window, and maximum request limit.
     * This initialization allows configuring the rate-limiting policies.
     *
     * @param webServer   the web server instance to which the rate-limiting policies will be applied
     * @param time        the time window to enforce the rate limit, represented as a Time object
     * @param maxRequests the maximum number of requests allowed within the specified time window
     */
    public RateLimitProvider(WebServer webServer, @NonNull Time time, int maxRequests) {
        this.webServer = webServer;
        this.maxRequests = maxRequests;
        this.defaultTimeWindow = time.toMilliseconds();
    }


    /**
     * Determines whether a request is allowed based on rate-limiting policies associated with a specific key
     * and request path. The method assesses both default and custom rate limits for the provided input.
     * <p>
     * The method first ensures that a default rate limit is established. Then it evaluates any additional
     * custom rate limits associated with the input path. Finally, it checks if the request complies with all
     * enforced rate limits. If any of the rate limits deny the request, the method returns false.
     *
     * @param key  the unique identifier used to group requests and apply specific rate-limiting policies
     * @param path the request path to evaluate for any matching custom rate-limiting rules
     * @return true if the request complies with all rate-limiting policies; false otherwise
     */
    private ConcurrentHashMap<String, RateLimit> resolveRateLimits(String key, String path) {
        ConcurrentHashMap<String, RateLimit> limits = rateLimits.computeIfAbsent(key, k -> new ConcurrentHashMap<>());

        limits.computeIfAbsent("default", k -> new RateLimit(maxRequests, defaultTimeWindow, RateLimitAlgorithm.USER_FIXED_WINDOW));

        for (Map.Entry<Pattern, AdditionalCustomRateLimit[]> entry : webServer.requestDispatcher().additionalCustomRateLimits().entrySet()) {
            Matcher matcher = entry.getKey().matcher(path);
            if (matcher.matches()) {
                for (AdditionalCustomRateLimit acrl : entry.getValue()) {
                    limits.computeIfAbsent(acrl.key(), k -> new RateLimit(acrl.maxRequests(), acrl.timeWindowMillis(), acrl.algorithm()));
                }

            }
        }

        boolean allowed = true;
        for (RateLimit rateLimit : limits.values()) {
            if (!rateLimit.allowRequest()) {
                allowed = false;
            }
        }

        return allowed;
    }

    /**
     * Returns the earliest timestamp (in milliseconds since epoch) at which a new request
     * would be allowed for the given key and path, considering all active rate limits.
     * If a request is currently allowed by all rate limits, this returns the current time.
     *
     * @param key  the unique identifier used to group requests and apply specific rate-limiting policies
     * @param path the request path to evaluate for any matching custom rate-limiting rules
     * @return earliest allowed timestamp in milliseconds since epoch
     */
    public long getEarliestAllowedTimestamp(String key, String path) {
        ConcurrentHashMap<String, RateLimit> limits = resolveRateLimits(key, path);

        long now = System.currentTimeMillis();
        long maxTimestamp = now;
        for (RateLimit rateLimit : limits.values()) {
            long allowedAt = now + rateLimit.getRetryAfterMillis();
            if (allowedAt > maxTimestamp) maxTimestamp = allowedAt;
        }
        return maxTimestamp;
    }
}
