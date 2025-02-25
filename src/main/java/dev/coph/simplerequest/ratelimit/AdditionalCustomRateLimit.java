package dev.coph.simplerequest.ratelimit;

public record AdditionalCustomRateLimit(String key, int maxRequests, long timeWindowMillis) {
    public AdditionalCustomRateLimit(CustomRateLimit customRateLimit) {
        this(customRateLimit.key(), customRateLimit.maxRequests(), customRateLimit.timeWindowMillis());
    }

    public AdditionalCustomRateLimit {
    }
}
