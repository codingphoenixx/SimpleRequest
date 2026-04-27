package dev.coph.simplerequest.handler;

import dev.coph.simplerequest.authentication.AccessLevel;

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
     * Defines the HTTP request method that the annotated handler should respond to.
     * If not explicitly specified, the default value is {@link RequestMethod#ANY}.
     *
     * @return the HTTP request method the handler is configured to respond to
     */
    RequestMethod method();

    /**
     * Defines the path for the request handler.
     *
     * @return the path of the request handler as a string
     */
    String path();

    /**
     * Specifies the access level required to invoke the annotated request handler.
     * This is used to determine the accessibility of the endpoint-discovery.
     *
     * @return the required access level for the request handler, defaulting to {@code AccessLevel.PUBLIC}
     */
    AccessLevel accesslevel() default AccessLevel.PUBLIC;

    /**
     * Provides an optional description for the request handler.
     * This description can be used for documentation or as metadata
     * to explain the purpose and behavior of the annotated method.
     *
     * @return a descriptive text about the request handler as a string, defaulting to an empty string
     */
    String description() default "";

    /**
     * Specifies an array of parameters for the request handler. Each parameter 
     * is described using a {@link RequestParameter} annotation, which defines 
     * details such as name, location (e.g., path, query, header, or cookie), 
     * whether the parameter is required, and other optional metadata.
     *
     * @return an array of {@link RequestParameter} annotations representing 
     *         the parameters that the annotated request handler expects to handle.
     *         Defaults to an empty array if no parameters are specified.
     */
    RequestParameter[] parameters() default {};

    /**
     * Indicates whether the annotated request handler is deprecated.
     * When set to {@code true}, the handler is considered deprecated
     * and its use is discouraged.
     *
     * @return {@code true} if the request handler is deprecated, {@code false} otherwise
     */
    boolean deprecated() default false;


}
