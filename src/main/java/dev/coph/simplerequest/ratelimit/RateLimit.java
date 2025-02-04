package dev.coph.simplerequest.ratelimit;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * A class that provides a mechanism for rate-limiting requests based on a maximum number
 * of allowable requests within a specified time window.
 *
 * The RateLimit class is designed to enforce limitations on the frequency of requests
 * made to a service or system. It maintains an internal counter to track the current
 * number of requests in the given time window, as well as the starting time of the
 * current window. Requests are either allowed or denied based on these values and the
 * configured maximum number of requests permissible for the time period.
 *
 * Instances of this class are initialized with immutable values for the maximum number
 * of requests and the duration of the time window. The state of the rate limiter is
 * thread-safe, ensuring correctness in multi-threaded environments.
 */
@Getter
@Accessors(fluent = true)
public class RateLimit {
    /**
     * Represents the maximum number of requests allowed within a specified time window for rate limiting.
     *
     * This variable defines the upper limit of requests that can be made within a defined time period.
     * It is a crucial component in enforcing rate-limiting policies, preventing excessive or abusive
     * usage patterns. The value is immutable, ensuring consistency in the rate-limiting configuration
     * throughout the lifecycle of the object.
     *
     * This field is initialized at the time of object construction and is used in conjunction with
     * the time window duration to evaluate whether incoming requests exceed the allowed threshold.
     */
    private final int maxRequests;

    /**
     * Represents the duration of the time window for rate limiting in milliseconds.
     *
     * This value defines the period within which a specific number of requests
     * (determined by the associated maximum request count) are allowed. It is used
     * to enforce rate-limiting logic, where requests are tracked and validated
     * against the allowed number within the specified time frame.
     *
     * The time window is a key component of the rate-limiting mechanism, helping
     * to regulate the flow of incoming requests and prevent exceeding the configured limits.
     */
    private final long timeWindowMillis;

    /**
     * Represents the starting timestamp of the current time window for rate-limiting purposes.
     *
     * This field stores the time (in milliseconds since the epoch) at which the current
     * rate-limiting time window began. It is used to determine whether the time window
     * has expired, at which point the window resets and the request count is recalculated.
     *
     * The value of this field is updated whenever the time window expires, allowing rate-limiting
     * to consistently enforce restrictions within defined intervals.
     */
    private long windowStart;

    /**
     * Tracks the number of requests made during the current time window for rate-limiting purposes.
     *
     * This field is incremented each time a new request is successfully allowed within the defined
     * time window. It is reset to 0 when the time window elapses, allowing the rate-limiting logic
     * to restart for the new time period. The value is used in conjunction with the maximum allowable
     * requests (`maxRequests`) to determine whether additional requests are permitted.
     *
     * The field ensures accurate tracking of requests to enforce rate-limiting policies consistently.
     */
    private int requestCount;

    /**
     * Constructs a RateLimit object with specified maximum number of requests and time window.
     * This object is used to enforce rate-limiting policies by tracking the number of requests
     * within a given time period and comparing it to the defined maximum.
     *
     * @param maxRequests the maximum number of requests allowed within the specified time window
     * @param timeWindowMillis the duration of the time window in milliseconds
     */
    public RateLimit(int maxRequests, long timeWindowMillis) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowMillis;
        this.windowStart = System.currentTimeMillis();
        this.requestCount = 0;
    }

    /**
     * Determines whether a request is allowed based on the current rate limiting configuration.
     *
     * This method checks if the current time window has elapsed, and if so, resets the request count.
     * If the number of requests within the current time window is less than the maximum allowed,
     * the request count is incremented, and the method returns true, signifying that the request
     * is allowed. If the request count exceeds the maximum allowed, the method returns false.
     *
     * @return true if the request is allowed under the rate limit; false otherwise.
     */
    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        if (now - windowStart > timeWindowMillis) {
            windowStart = now;
            requestCount = 0;
        }
        if (requestCount < maxRequests) {
            requestCount++;
            return true;
        }
        return false;
    }
}
