package dev.coph.simplerequest.ratelimit;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayDeque;

/**
 * Vollständige RateLimiter-Implementierung mit Unterstützung für alle Algorithmen aus {@link RateLimitAlgorithm}.
 */
@Getter
@Accessors(fluent = true)
public class RateLimit {

    private final int maxRequests;
    private final long timeWindowMillis;
    private final RateLimitAlgorithm algorithm;
    private final ArrayDeque<Long> requestTimestamps;
    private long windowStart;
    private int requestCount;
    private long lastRequestTime = -1L;
    private double tokens;
    private double refillRatePerMs;
    private long lastRefillTimestamp;

    /**
     * Constructs a RateLimit instance with the specified maximum number of requests
     * and time window in milliseconds, using the default rate-limiting algorithm.
     *
     * @param maxRequests      The maximum number of requests allowed within the specified time window.
     * @param timeWindowMillis The duration of the time window in milliseconds during which requests are counted.
     */
    public RateLimit(int maxRequests, long timeWindowMillis) {
        this(maxRequests, timeWindowMillis, RateLimitAlgorithm.USER_FIXED_WINDOW);
    }

    /**
     * Constructs a RateLimit instance with the specified maximum number of requests,
     * time window in milliseconds, and rate-limiting algorithm.
     *
     * @param maxRequests      The maximum number of requests allowed within the specified time window.
     * @param timeWindowMillis The duration of the time window in milliseconds during which requests are counted.
     * @param algorithm        The rate-limiting algorithm to use, which determines the mechanism for
     *                         enforcing rate limits such as FIXED_WINDOW, TOKEN_BUCKET, or SLIDING_WINDOW.
     */
    public RateLimit(int maxRequests, long timeWindowMillis, RateLimitAlgorithm algorithm) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowMillis;
        this.algorithm = algorithm;

        long now = System.currentTimeMillis();

        switch (algorithm) {
            case TOKEN_BUCKET -> {
                this.tokens = maxRequests;
                this.refillRatePerMs = (double) maxRequests / (double) Math.max(1L, timeWindowMillis);
                this.lastRefillTimestamp = now;
                this.requestTimestamps = null;
                this.windowStart = 0L;
            }
            case SLIDING_WINDOW -> {
                this.requestTimestamps = new ArrayDeque<>();
                this.windowStart = 0L;
            }
            case FIXED_WINDOW, MINIMUM_COOLDOWN_FIXED_WINDOW -> {
                this.requestTimestamps = null;
                this.windowStart = alignToWindow(now);
                this.requestCount = 0;
            }
            default -> {
                this.requestTimestamps = null;
                this.windowStart = now;
                this.requestCount = 0;
            }
        }
    }

    private long alignToWindow(long t) {
        if (timeWindowMillis <= 0L) return t;
        return (t / timeWindowMillis) * timeWindowMillis;
    }

    private void pruneOldTimestamps(long now) {
        long threshold = now - timeWindowMillis;
        while (!requestTimestamps.isEmpty() && requestTimestamps.peekFirst() < threshold) {
            requestTimestamps.pollFirst();
        }
    }

    private void refillTokens(long now) {
        long delta = Math.max(0, now - lastRefillTimestamp);
        if (delta > 0) {
            tokens = Math.min(maxRequests, tokens + delta * refillRatePerMs);
            lastRefillTimestamp = now;
        }
    }

    /**
     * Determines whether a request is allowed based on the configured rate-limiting algorithm.
     * This method evaluates the internal state of the rate limiter and decides if a new request
     * can be processed according to the rules of the selected algorithm, such as FIXED_WINDOW,
     * SLIDING_WINDOW, or TOKEN_BUCKET.
     *
     * @return true if the request is allowed under the current rate-limiting configuration; false otherwise
     */
    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();

        return switch (algorithm) {
            case FIXED_WINDOW -> {
                long currentWindowStart = alignToWindow(now);
                if (currentWindowStart != windowStart) {
                    windowStart = currentWindowStart;
                    requestCount = 0;
                }
                if (requestCount < maxRequests) {
                    requestCount++;
                    lastRequestTime = now;
                    yield true;
                }
                yield false;
            }
            case USER_FIXED_WINDOW -> {
                if (now - windowStart >= timeWindowMillis) {
                    windowStart = now;
                    requestCount = 0;
                }
                if (requestCount < maxRequests) {
                    requestCount++;
                    lastRequestTime = now;
                    yield true;
                }
                yield false;
            }
            case MINIMUM_COOLDOWN_FIXED_WINDOW -> {
                long currentWindowStart = alignToWindow(now);
                if (currentWindowStart != windowStart) {
                    windowStart = currentWindowStart;
                    requestCount = 0;
                }
                if (requestCount < maxRequests) {
                    requestCount++;
                    lastRequestTime = now;
                    yield true;
                }
                if (now >= lastRequestTime + timeWindowMillis) {
                    windowStart = alignToWindow(now);
                    requestCount = 1;
                    lastRequestTime = now;
                    yield true;
                }
                yield false;
            }
            case MINIMUM_COOLDOWN_USER_FIXED_WINDOW -> {
                if (now - windowStart >= timeWindowMillis) {
                    windowStart = now;
                    requestCount = 0;
                }
                if (requestCount < maxRequests) {
                    requestCount++;
                    lastRequestTime = now;
                    yield true;
                }
                if (now >= lastRequestTime + timeWindowMillis) {
                    windowStart = now;
                    requestCount = 1;
                    lastRequestTime = now;
                    yield true;
                }
                yield false;
            }
            case SLIDING_WINDOW -> {
                pruneOldTimestamps(now);
                if (requestTimestamps.size() < maxRequests) {
                    requestTimestamps.addLast(now);
                    lastRequestTime = now;
                    yield true;
                }
                yield false;
            }
            case TOKEN_BUCKET -> {
                refillTokens(now);
                if (tokens >= 1.0) {
                    tokens -= 1.0;
                    lastRequestTime = now;
                    yield true;
                }
                yield false;
            }
        };
    }

    /**
     * Calculates the time in milliseconds until the next request is allowed, based on
     * the configured rate-limiting algorithm and the current state of the rate limiter.
     * The method evaluates the current request rate, recent activity, and algorithm-specific
     * parameters such as time windows, token counts, or sliding windows to determine
     * the appropriate wait time before subsequent requests can be made.
     *
     * @return the number of milliseconds to wait before the next request is allowed.
     * A return value of 0 indicates that no wait is necessary and a request can
     * be made immediately.
     */
    public synchronized long getRetryAfterMillis() {
        long now = System.currentTimeMillis();

        return switch (algorithm) {
            case FIXED_WINDOW -> {
                long currentWindowStart = alignToWindow(now);
                int used = (currentWindowStart == windowStart) ? requestCount : 0;
                if (used < maxRequests) {
                    yield 0L;
                }
                long windowEnd = currentWindowStart + timeWindowMillis;
                yield Math.max(0L, windowEnd - now);
            }
            case USER_FIXED_WINDOW -> {
                if (now - windowStart >= timeWindowMillis) {
                    yield 0L;
                }
                if (requestCount < maxRequests) {
                    yield 0L;
                }
                yield Math.max(0L, (windowStart + timeWindowMillis) - now);
            }
            case MINIMUM_COOLDOWN_FIXED_WINDOW, MINIMUM_COOLDOWN_USER_FIXED_WINDOW -> {
                if (requestCount < maxRequests) {
                    yield 0L;
                }
                yield Math.max(0L, (lastRequestTime + timeWindowMillis) - now);
            }
            case SLIDING_WINDOW -> {
                pruneOldTimestamps(now);
                if (requestTimestamps.size() < maxRequests) {
                    yield 0L;
                }
                Long oldest = requestTimestamps.peekFirst();
                if (oldest == null) {
                    yield 0L;
                }
                yield Math.max(0L, (oldest + timeWindowMillis) - now);
            }
            case TOKEN_BUCKET -> {
                refillTokens(now);
                if (tokens >= 1.0) {
                    yield 0L;
                }
                double missing = 1.0 - tokens;
                long ms = (long) Math.ceil(missing / Math.max(1e-12, refillRatePerMs));
                yield Math.max(0L, ms);
            }
        };
    }
}
