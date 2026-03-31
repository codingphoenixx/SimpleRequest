package dev.coph.simplerequest.handler;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public record RouteEntry(Pattern pattern, LinkedHashMap<RequestMethod, MethodHandler> methods) {
    public RouteEntry(Pattern pattern) {
        this(pattern, new LinkedHashMap<>());
    }
}
