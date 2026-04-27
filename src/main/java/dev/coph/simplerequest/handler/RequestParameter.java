package dev.coph.simplerequest.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to define metadata for a request parameter in an HTTP handler.
 * <p>
 * This annotation is used to specify details about parameters that are essential for
 * handling HTTP requests. The metadata includes the parameter's name, the source of the
 * parameter within the request (e.g., path, query, header, or cookie), and additional
 * descriptive attributes such as whether the parameter is required, its description,
 * type, and format.
 * <p>
 * Components:<br>
 * - `name`: The name of the request parameter.<br>
 * - `input`: The source of the request parameter, represented by the {@link ParamInput} enum.<br>
 * - `required`: A flag indicating whether the parameter is mandatory. Defaults to `false`.<br>
 * - `description`: A textual description of the purpose or usage of the parameter.<br>
 * - `type`: The data type of the parameter's value.<br>
 * - `format`: The specific format of the parameter, if applicable.<br>
 * <p>
 * This annotation is intended for use in frameworks or libraries where metadata about
 * HTTP request parameters is needed to process and validate requests.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParameter {
    /**
     * Retrieves the name of the request parameter.
     * <p>
     * The parameter name is a key that identifies the specific parameter
     * within an HTTP request. This name may correspond to a path variable,
     * query parameter, header, or cookie, depending on the request context
     * and the source specified in the annotation.
     *
     * @return the name of the request parameter as a String
     */
    String name();

    /**
     * Retrieves the source of the request parameter.
     * <p>
     * The source indicates where the parameter is expected to be located
     * within the HTTP request. Possible sources are defined in the {@link ParamInput} enum
     * and include options such as PATH, QUERY, HEADER, and COOKIE.
     *
     * @return the {@link ParamInput} that specifies the source of the request parameter
     */
    ParamInput input();

    /**
     * Indicates whether the parameter is mandatory.
     * <p>
     * This property is used to specify if the annotated parameter must be present
     * in the HTTP request for proper handling. If set to {@code true}, the absence
     * of the parameter will result in validation or processing errors.
     * Defaults to {@code false}, meaning the parameter is optional by default.
     *
     * @return {@code true} if the parameter is required, {@code false} otherwise
     */
    boolean required() default false;

    /**
     * Retrieves the description of the request parameter.
     * <p>
     * The description provides additional context or details about the purpose
     * and usage of the request parameter. This information can be used to enhance
     * documentation or assist developers in understanding the role of the parameter
     * in the request processing.
     *
     * @return the description of the request parameter as a String
     */
    String description() default "";

    /**
     * Specifies the data type or format of the request parameter.
     * <p>
     * This attribute is used to define additional metadata about the way the parameter is expected to be represented,
     * such as its data type or serialization format. It allows further customization or validation
     * based on the type-specific requirements of the parameter within the HTTP request.
     *
     * @return the data type or format of the request parameter as a String
     */
    String type() default "";

    /**
     * Specifies the format of the request parameter.
     * <p>
     * This attribute defines additional formatting details or 
     * constraints for how the parameter should be represented or parsed. 
     * It can be useful for specifying expected data patterns, encoding 
     * formats, or any other relevant structural information needed for 
     * precise handling of the parameter within the HTTP request.
     *
     * @return the format of the request parameter as a String
     */
    String format() default "";
}