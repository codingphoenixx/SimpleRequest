package dev.coph.simplerequest.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a request handler for incoming HTTP requests.
 * When annotated, the method will be invoked for a matching HTTP request, based
 * on the path and specific configurations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestHandler {

    /**
     * Defines the path for the request handler.
     *
     * @return the path of the request handler as a string
     */
    String path();


    /**
     * Indicates whether authentication is required for the annotated request handler.
     *
     * @return true if authentication is necessary for the request handler, false otherwise
     */
    boolean needAuth() default false;
}
