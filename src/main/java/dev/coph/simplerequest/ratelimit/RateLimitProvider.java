package dev.coph.simplerequest.ratelimit;

import dev.coph.simplerequest.server.WebServer;
import dev.coph.simplerequest.util.Time;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides functionality to enforce rate-limiting policies for different keys (e.g., users, IP addresses).
 * <p>
 * The RateLimitProvider class utilizes a combination of default rate-limiting parameters
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
    /**
     * Stores rate limit configurations associated with specific keys for managing request throttling.
     * <p>
     * This map maintains a collection of {@link RateLimit} objects, where each key represents a unique
     * context for rate limiting (e.g., a user ID or an IP address). Entries in this map are dynamically
     * created or updated as needed when handling rate-limited requests.
     * <p>
     * This field is immutable and initialized during the instantiation of the {@code RateLimitProvider}.
     * It is used primarily by the {@link RateLimitProvider#allowRequest(String, String)} method to enforce
     * rate-limiting policies based on the configurations defined for each context.
     */
    private final HashMap<String, HashMap<String, RateLimit>> rateLimits = new HashMap<>();

    /**
     * Represents the default time window duration used for rate limiting, in milliseconds.
     * <p>
     * This value serves as a default configuration for the duration within which a certain
     * number of requests are allowed. It is used when creating new {@link RateLimit} objects
     * in the absence of a specific time window configuration for a particular entity.
     * <p>
     * The field is immutable and is set during the instantiation of the {@code RateLimitProvider}
     * class, ensuring consistent and predictable behavior for rate-limiting operations
     * managed by the provider.
     */
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
     * The method first ensures that a default rate limit is established, then it evaluates any additional
     * custom rate limits associated with the input path. Finally, it checks if the request complies with all
     * enforced rate limits. If any of the rate limits deny the request, the method returns false.
     *
     * @param key  the unique identifier used to group requests and apply specific rate-limiting policies
     * @param path the request path to evaluate for any matching custom rate-limiting rules
     * @return true if the request complies with all rate-limiting policies; false otherwise
     */
    public boolean allowRequest(String key, String path) {
        HashMap<String, RateLimit> rateLimits = this.rateLimits.computeIfAbsent(key, s -> new HashMap<>());

        if (!rateLimits.containsKey("default")) {
            rateLimits.put("default", new RateLimit(maxRequests, defaultTimeWindow));
        }

        for (Map.Entry<Pattern, AdditionalCustomRateLimit> entry : webServer.requestDispatcher().additionalCustomRateLimits().entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(path.trim());
            if (matcher.matches()) {
                AdditionalCustomRateLimit value = entry.getValue();
                if (!rateLimits.containsKey(value.key()))
                    rateLimits.put(value.key(), new RateLimit(value.maxRequests(), value.timeWindowMillis()));
            }
        }

        boolean allowed = true;
        for (RateLimit rateLimit : rateLimits.values()) {
            if (!rateLimit.allowRequest()) {
                allowed = false;
            }
        }

        return allowed;
    }
}
