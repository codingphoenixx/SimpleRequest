package dev.coph.simplerequest.handler;

import dev.coph.simplerequest.authentication.AccessLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to handle field-based HTTP requests with specific configurations.
 * <p>
 * This annotation is designed to facilitate the definition of HTTP request handlers
 * by specifying details such as the request method, access level, required and optional
 * parameters, default values, and additional metadata.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FieldRequestHandler {

    /**
     * Retrieves the HTTP request method associated with the annotated handler.
     * <p>
     * The method corresponds to the type of HTTP request (such as GET, POST, PUT, DELETE, etc.)
     * that this handler is configured to handle.
     *
     * @return the {@link RequestMethod} representing the HTTP method associated with this handler.
     */
    RequestMethod method();

    /**
     * Specifies the path or endpoint for the annotated HTTP request handler.
     * <p>
     * This path represents the URI to which the annotated method will respond.
     *
     * @return the URI path associated with the handler
     */
    String path();

    /**
     * Defines the access level required to invoke the annotated method.
     * <p>
     * The access level determines the visibility and permissions for the associated handler.
     * It specifies whether the annotated handler is accessible publicly, requires authentication,
     * is restricted to internal use, or is disabled.
     *
     * @return the {@link AccessLevel} indicating the required access level for the handler.
     */
    AccessLevel accesslevel() default AccessLevel.PUBLIC;

    /**
     * Provides a brief description of the annotated field-based HTTP request handler.
     * <p>
     * The description can offer additional context or explanation regarding
     * the purpose, functionality, or behavior of the associated handler.
     *
     * @return a string containing the description of the handler
     */
    String description() default "";

    /**
     * Specifies the list of required fields for the annotated handler.
     * <p>
     * This attribute defines a set of field names that are mandatory
     * and must be provided in the HTTP request. The annotated method
     * will validate these fields to ensure they are present and properly defined.
     *
     * @return an array of strings representing the names of the required fields
     */
    String[] required() default {};

    /**
     * Specifies the optional fields for the annotated handler.
     * <p>
     * This attribute defines a set of field names that are optional and
     * can be provided in the HTTP request. These fields are not mandatory
     * and may or may not be included in the request without affecting
     * the validity of the request.
     *
     * @return an array of strings representing the names of the optional fields
     */
    String[] optional() default {};

    /**
     * Specifies the default values for the annotated handler.
     * <p>
     * This attribute defines a list of default field values to be used when
     * the corresponding request fields are not provided. These defaults help
     * in maintaining consistent behavior or fallback values for the handler.
     *
     * @return an array of strings representing the default values for the fields
     */
    String[] defaults() default {};

    /**
     * Specifies the name of the HTTP header used to carry additional field information
     * for the annotated handler.
     * <p>
     * This header name identifies the custom HTTP header expected in the request,
     * which can be used to relay specific field data for processing.
     *
     * @return the default name of the header as a string, or a customized name if specified
     */
    String headerName() default "X-Fields";
}
