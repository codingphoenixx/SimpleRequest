package dev.coph.simplerequest.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply multiple {@link CustomRateLimit} configurations to a single method.
 * <p>
 * This annotation acts as a container for defining multiple rate-limiting rules that
 * can be applied to a single method or endpointdiscovery. It enables combining multiple instances
 * of the {@link CustomRateLimit} annotation for scenarios requiring more complex
 * or granular rate-limiting configurations. Each {@link CustomRateLimit} specifies its own
 * key, request limit, time window, and optional rate-limiting algorithm.
 * <p>
 * Usage of this annotation allows developers to enforce different rate-limiting policies
 * on the same method, which can be useful for managing requests coming from different
 * users, roles, or client applications.
 * <p>
 * The rate-limiting logic should process all {@link CustomRateLimit} configurations
 * specified within this annotation, applying each set of restrictions independently.
 * <p>
 * This annotation is retained at runtime and can be applied to method declarations only.
 *
 * @see CustomRateLimit
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomRateLimits {
    /**
     * Retrieves an array of {@link CustomRateLimit} configurations associated with the annotated method.
     * Each {@link CustomRateLimit} within the array defines specific rate-limiting rules to be applied,
     * such as maximum requests, time window duration, and the algorithm to enforce the limits.
     * This method is used to access the rate-limiting configurations when the {@link CustomRateLimits}
     * annotation is applied.
     *
     * @return an array of {@link CustomRateLimit} configurations for the annotated method
     */
    CustomRateLimit[] value();
}
