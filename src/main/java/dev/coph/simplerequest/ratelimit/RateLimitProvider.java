package dev.coph.simplerequest.ratelimit;

import dev.coph.simplerequest.server.WebServer;
import dev.coph.simplerequest.util.Time;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The RateLimitProvider class is responsible for managing and enforcing
 * request rate limits in a web server environment. It provides mechanisms
 * to apply default rate limits as well as custom rate limits based on the
 * client and requested path.
 *
 * This class integrates with a WebServer instance to configure rate limiting
 * rules and dynamically resolve applicable rate limits for incoming requests.
 * Rate limits can be defined with different algorithms, such as fixed window
 * or custom implementations.
 */
public class RateLimitProvider {
    private final WebServer webServer;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, RateLimit>> rateLimits = new ConcurrentHashMap<>();
    private final long defaultTimeWindow;
    private final int maxRequests;

    /**
     * Constructs a new RateLimitProvider instance for managing request rate limits.
     *
     * @param webServer    The WebServer instance with which this RateLimitProvider is associated.
     * @param time         The Time object representing the duration of the default time window for rate limiting.
     * @param maxRequests  The maximum number of requests allowed within the default time window.
     */
    public RateLimitProvider(WebServer webServer, @NonNull Time time, int maxRequests) {
        this.webServer = webServer;
        this.maxRequests = maxRequests;
        this.defaultTimeWindow = time.toMilliseconds();
    }

    /**
     * Resolves and returns the rate limits associated with a given key and path.
     * This method ensures that default and custom rate limits are initialized
     * and applied based on the requested path.
     *
     * @param key  The unique identifier associated with a user or client making a request.
     * @param path The request path that may have specific rate-limiting rules applied to it.
     * @return A ConcurrentHashMap where the keys represent rate limit categories (e.g., "default"
     *         or custom keys) and the values are the corresponding RateLimit objects.
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

        return limits;
    }

    /**
     * Determines whether a request is allowed based on the rate limits associated with the given key and path.
     * This method evaluates all relevant rate limits and grants or denies permission accordingly.
     *
     * @param key  The unique identifier associated with the user or client making the request.
     * @param path The request path that may have specific rate-limiting rules applied to it.
     * @return {@code true} if the request is allowed based on the applicable rate limits, {@code false} otherwise.
     */
    public boolean allowRequest(String key, String path) {
        ConcurrentHashMap<String, RateLimit> limits = resolveRateLimits(key, path);

        boolean allowed = true;
        for (RateLimit rateLimit : limits.values()) {
            if (!rateLimit.allowRequest()) {
                allowed = false;
            }
        }
        return allowed;
    }

    /**
     * Determines the earliest allowed timestamp for a request based on the rate limits
     * associated with the given key and path. This method evaluates all relevant rate
     * limits and calculates the maximum allowed timestamp to ensure compliance with the
     * rate-limiting rules.
     *
     * @param key  The unique identifier associated with a user or client making the request.
     * @param path The request path that may have specific rate-limiting rules applied to it.
     * @return The earliest timestamp (in milliseconds since epoch) at which the request
     *         is allowed to proceed based on the applicable rate limits.
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