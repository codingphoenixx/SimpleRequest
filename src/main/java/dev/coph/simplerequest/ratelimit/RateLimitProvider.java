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
 * This class supports thread-safe request evaluation via the {@link #allowRequest(String, String, String)} method,
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
     * Evaluates whether a request is permitted based on the rate limits resolved for
     * the specified key, request path, and HTTP method. This method checks all relevant
     * rate limits to determine if the request adheres to the defined limits.
     *
     * @param key    The unique identifier associated with the user or client making the request.
     * @param path   The request path that may have specific rate-limiting rules applied to it.
     * @param method The HTTP method (e.g., GET, POST) of the request.
     * @return {@code true} if the request is allowed based on the applicable rate limits;
     * {@code false} otherwise.
     */
    public boolean allowRequest(String key, String path, String method) {
        ConcurrentHashMap<String, RateLimit> limits = resolveRateLimits(key, path, method);

        boolean allowed = true;
        for (RateLimit rateLimit : limits.values()) {
            if (!rateLimit.allowRequest()) {
                allowed = false;
            }
        }
        return allowed;
    }

    /**
     * Resolves and applies rate limits for the given user or client based on the specified key,
     * request path, and HTTP method. It evaluates default rate limits and any additional custom
     * rate limits applicable to the path and method.
     *
     * @param key    The unique identifier associated with the user or client making the request.
     * @param path   The request path that may have specific rate-limiting rules applied to it.
     * @param method The HTTP method (e.g., GET, POST) of the request.
     * @return A map containing the resolved rate limits for the key, where the keys in the map
     * represent different rate limit categories (e.g., default and custom rules), and
     * the values are the corresponding RateLimit objects.
     */
    public ConcurrentHashMap<String, RateLimit> resolveRateLimits(String key, String path, String method) {
        String rateLimitKey = path + ":" + method;

        ConcurrentHashMap<String, RateLimit> userLimits = rateLimits.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        ConcurrentHashMap<String, RateLimit> resolvedLimits = new ConcurrentHashMap<>();

        resolvedLimits.put("default", userLimits.computeIfAbsent("default", k -> new RateLimit(maxRequests, defaultTimeWindow, RateLimitAlgorithm.USER_FIXED_WINDOW)));

        for (Map.Entry<Pattern, AdditionalCustomRateLimit[]> entry : webServer.requestDispatcher().additionalCustomRateLimits().entrySet()) {
            Matcher matcher = entry.getKey().matcher(rateLimitKey);
            if (matcher.matches()) {
                for (AdditionalCustomRateLimit acrl : entry.getValue()) {
                    resolvedLimits.put(acrl.key(), userLimits.computeIfAbsent(acrl.key(), k -> new RateLimit(acrl.maxRequests(), acrl.timeWindowMillis(), acrl.algorithm())));
                }
            }
        }

        return resolvedLimits;
    }

    /**
     * Calculates the earliest possible timestamp at which requests can be allowed
     * based on the given rate limits. The method iterates through the provided rate
     * limits and determines the maximum timestamp that is currently blocking requests.
     *
     * @param limits a {@link ConcurrentHashMap} where the keys represent rate
     *               limit identifiers and the values are {@link RateLimit} objects
     *               containing rate-limiting information.
     * @return the earliest timestamp (in milliseconds) at which requests can be allowed
     * based on the provided rate limits.
     */
    public long earliestAllowedTimestamp(ConcurrentHashMap<String, RateLimit> limits) {
        long now = System.currentTimeMillis();
        long maxTimestamp = now;

        for (RateLimit rateLimit : limits.values()) {
            long allowedAt = now + rateLimit.getRetryAfterMillis();
            if (allowedAt > maxTimestamp) maxTimestamp = allowedAt;
        }

        return maxTimestamp;
    }

    /**
     * Filters and returns a map of rate limits that have been triggered based on their retry-after values.
     * A rate limit is considered triggered if its retry-after value is not equal to zero.
     *
     * @param limits a {@code ConcurrentHashMap} containing the rate limits to evaluate, where the keys
     *               represent rate limit identifiers and the values are {@code RateLimit} objects
     *               containing the associated rate-limiting information.
     * @return a {@code ConcurrentHashMap} containing only the triggered rate limits, where the keys
     * represent the rate limit identifiers and the values are the corresponding triggered
     * {@code RateLimit} objects.
     */
    public ConcurrentHashMap<String, RateLimit> allTriggeredLimits(ConcurrentHashMap<String, RateLimit> limits) {
        ConcurrentHashMap<String, RateLimit> triggered = new ConcurrentHashMap<>(limits);
        for (Map.Entry<String, RateLimit> rateLimit : limits.entrySet()) {
            if (rateLimit.getValue().getRetryAfterMillis() == 0) {
                triggered.remove(rateLimit.getKey());
            }
        }
        return triggered;
    }
}
