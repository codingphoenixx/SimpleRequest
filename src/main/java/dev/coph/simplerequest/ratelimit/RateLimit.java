package dev.coph.simplerequest.ratelimit;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class RateLimit {
    private final int maxRequests;
    private final long timeWindowMillis;
    private long windowStart;
    private int requestCount;

    public RateLimit(int maxRequests, long timeWindowMillis) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowMillis;
        this.windowStart = System.currentTimeMillis();
        this.requestCount = 0;
    }

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
