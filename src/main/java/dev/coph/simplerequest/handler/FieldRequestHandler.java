package dev.coph.simplerequest.handler;

import dev.coph.simplerequest.authentication.AccessLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FieldRequestHandler {
    RequestMethod method();

    String path();

    AccessLevel accesslevel() default AccessLevel.PUBLIC;

    String description() default "";

    String[] required() default {};

    String[] optional() default {};

    String[] defaults() default {};

    String headerName() default "X-Fields";
}
