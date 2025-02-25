package dev.coph.simplerequest.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define additional rate limiting configurations for a specific method or endpoint.
 * This annotation allows specifying maximum request limits and the time window for which these limits
 * are applicable. It is typically used for applying fine-grained control over request rates
 * in scenarios where different methods or endpoints have varying rate-limiting requirements.
 *
 * The rate-limiting logic should interpret the provided values for `maxRequests` and `timeWindowMillis`
 * to enforce the desired restrictions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomRateLimit {
    /**
     * Specifies the key used to identify the rate-limiting configuration.
     * This key helps differentiate between multiple rate-limiting rules
     * or configurations, enabling fine-grained control over request handling.
     *
     * @return the key as a string
     */
    String key();
    /**
     * Specifies the maximum number of requests allowed within a defined time window
     * for the annotated method or endpoint. This value is intended to be used for
     * rate-limiting purposes and should be interpreted in conjunction with the time window.
     *
     * @return the maximum number of requests permitted as an integer
     */
    int maxRequests();

    /**
     * Specifies the duration of the time window, in milliseconds, for which the rate-limiting
     * configuration applies. This value is used in conjunction with the maximum request limit
     * to enforce rate-limiting within a fixed period.
     *
     * @return the duration of the time window in milliseconds as a long
     */
    long timeWindowMillis();
}
