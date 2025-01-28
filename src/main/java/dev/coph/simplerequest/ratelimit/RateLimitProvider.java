package dev.coph.simplerequest.ratelimit;

import dev.coph.simplerequest.util.Time;
import lombok.NonNull;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RateLimitProvider {
    private final HashMap<String, RateLimit> rateLimits = new HashMap<>();
    private final long defaultTimeWindow;
    private final int maxRequests;

    public RateLimitProvider(@NonNull Time time, int maxRequests) {
        this.maxRequests = maxRequests;
        this.defaultTimeWindow = time.toMilliseconds();
    }

    public boolean allowRequest(String key){
        RateLimit rateLimit = rateLimits.computeIfAbsent(key, s -> new RateLimit(maxRequests, defaultTimeWindow));
        return rateLimit.allowRequest();
    }
}
