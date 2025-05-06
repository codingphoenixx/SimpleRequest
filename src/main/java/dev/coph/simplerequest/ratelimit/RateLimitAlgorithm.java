package dev.coph.simplerequest.ratelimit;

/**
 * Lists the various algorithms used for rate-limiting mechanisms.<br>
 *<br>
 * This enumeration defines different approaches to manage and restrict<br>
 * the rate at which requests are processed. These algorithms are used<br>
 * to enforce rate limits in distributed systems, APIs, or any system<br>
 * requiring controlled consumption of resources. Each algorithm provides<br>
 * a unique way of maintaining request count and evaluating rate limits.<br>
 *<br>
 * The algorithms include:<br>
 * - FIXED_WINDOW: The fixed window algorithm divides time into fixed intervals
 *   and tracks the number of requests within each interval.<br>
 * - USER_FIXED_WINDOW: A variation of the fixed window algorithm where rate
 *   limits start with the first request from the user.<br>
 * - SLIDING_WINDOW: You have a pool of tokens that are replenished after the window
 *   applied each one.<br>
 * - TOKEN_BUCKET: Utilizes tokens as a dynamic mechanism to regulate the flow
 *   of allowed requests, where tokens are replenished over time.<br>
 */
public enum RateLimitAlgorithm {

    /**
     * Represents the fixed window algorithm for rate-limiting.
     *
     * The fixed window algorithm segments time into intervals of a predetermined
     * length (windows) and tracks the number of requests made within each window. Once
     * the limit of requests for the current window is reached, subsequent requests
     * are denied until the start of the next window.
     *
     * This approach is straightforward to implement and provides deterministic
     * request limits for each time interval but may lead to uneven distribution of
     * allowed requests if bursts occur near the boundary of two consecutive windows.
     */
    FIXED_WINDOW,
    /**
     *  A variation of the fixed window rate-limiting strategy that resets the "start" of
     *  the window to the time of the last request made by a user, regardless of the original start time.
     *
     *  This approach is useful for scenarios where a user should not be able to try until to cooldown
     *  is finished and hope that the first request in the new window will be processed.
     */
    MINIMUM_COOLDOWN_FIXED_WINDOW,
    /**
     * Represents a user-specific variation of the fixed window rate-limiting algorithm.
     *
     * The user-specific window algorithm segments time into intervals of a predetermined
     * length (windows) and tracks the number of requests made within each window. Once
     * the limit of requests for the current window is reached, subsequent requests
     * are denied until the start of the next window.
     *
     * This approach is straightforward to implement and provides deterministic
     * request limits for each time interval but may lead to uneven distribution of
     * allowed requests if the windows are triggered once and a burst occurs near the boundary of two consecutive windows.
     */
    USER_FIXED_WINDOW,
    /**
     *  A variation of the user-specific window rate-limiting strategy that resets the "start" of
     *  the window to the time of the last request made by a user, regardless of the original start time.
     *
     *  This approach is useful for scenarios where a user should not be able to try until to cooldown
     *  is finished and hope that the first request in the new window will be processed.
     */
    MINIMUM_COOLDOWN_USER_FIXED_WINDOW,
    /**
     * Represents the sliding window algorithm for rate-limiting.
     *
     * The sliding window algorithm dynamically maintains request counts over a
     * sliding time window, allowing for more evenly distributed request handling
     * compared to traditional fixed window algorithms. It evaluates requests based
     * on their timestamps and computes the rate limit using a continuously updated
     * sliding window.
     *
     * This approach provides greater accuracy in adhering to the defined request
     * limits, especially in scenarios where incoming requests arrive in bursts,
     * as it avoids the boundary issues commonly associated with fixed window schemes.
     */
    SLIDING_WINDOW,
    /**
     * A rate-limiting strategy based on the token bucket algorithm.
     *
     * The TOKEN_BUCKET strategy regulates the flow of requests by maintaining a bucket
     * of tokens, where each token corresponds to permission for a single request.
     * Tokens are replenished at a fixed rate over time, and they are consumed
     * whenever a request is processed. If no tokens are available, the request is
     * rejected until tokens become available again.
     *
     * This approach allows for bursty traffic while ensuring that the average request
     * rate does not exceed the defined limit, making it suitable for scenarios where
     * short bursts of higher traffic levels are acceptable but sustained high traffic
     * levels are not.
     */
    TOKEN_BUCKET,

}
