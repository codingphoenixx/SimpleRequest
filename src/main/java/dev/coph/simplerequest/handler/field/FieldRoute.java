package dev.coph.simplerequest.handler.field;

import dev.coph.simplerequest.handler.RequestMethod;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;
import java.util.Set;

@Accessors(fluent = true)
public record FieldRoute(Object instance, Method method, String path, RequestMethod requestMethod, String headerName,
                         Set<String> required, Set<String> optional, Set<String> defaults) {

}
