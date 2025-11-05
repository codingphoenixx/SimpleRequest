package dev.coph.simplerequest.handler.field;

import org.eclipse.jetty.server.Request;

import java.util.*;
import java.util.stream.Collectors;

public final class FieldSelection {
    private FieldSelection() {
    }

    public static Set<String> read(Request req, String headerName) {
        String header = Optional.ofNullable(req.getHeaders().get(headerName)).orElse("");
        Set<String> fromHeader = parseCsv(header);
        if (!fromHeader.isEmpty()) return fromHeader;
        return Set.of();
    }

    public static Set<String> normalize(String... names) {
        if (names == null || names.length == 0) return Set.of();
        return Arrays.stream(names)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) return Set.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
