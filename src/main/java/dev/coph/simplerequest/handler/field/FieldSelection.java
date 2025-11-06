package dev.coph.simplerequest.handler.field;

import org.eclipse.jetty.server.Request;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for parsing and normalizing field selections from HTTP requests or CSV strings.
 * This class provides static methods to read and process field selections from request headers,
 * normalize field names by trimming and removing duplicates, and parse comma-separated values (CSV)
 * into a set of fields. It is designed to assist in extracting and organizing field names for
 * request handling or similar use cases.
 * <p>
 * This class is non-instantiable.
 */
public final class FieldSelection {
    /**
     * Private constructor to prevent instantiation of the {@code FieldSelection} utility class.
     * This class contains static methods for parsing and normalizing field selections, and is
     * not meant to be instantiated.
     */
    private FieldSelection() {
    }

    /**
     * Reads and parses a specified HTTP header from the provided request into a set of field names.
     * If the header is not found or its value is empty, an empty set is returned.
     *
     * @param req        the HTTP request object containing the headers
     * @param headerName the name of the header to read and parse
     * @return a set of parsed field names from the header, or an empty set if the header is missing or empty
     */
    public static Set<String> read(Request req, String headerName) {
        String header = Optional.ofNullable(req.getHeaders().get(headerName)).orElse("");
        Set<String> fromHeader = parseCsv(header);
        if (!fromHeader.isEmpty()) return fromHeader;
        return Set.of();
    }

    /**
     * Normalizes an array of input strings by trimming whitespace, filtering out null or empty values,
     * and removing duplicates while maintaining the insertion order.
     *
     * @param names the array of strings to be normalized; can be null or empty
     * @return a {@code Set} of normalized, unique strings in the insertion order,
     * or an empty set if the input is null or contains no valid entries
     */
    public static Set<String> normalize(String... names) {
        if (names == null || names.length == 0) return Set.of();
        return Arrays.stream(names)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Parses a comma-separated values (CSV) string into a set of trimmed, unique strings,
     * preserving the insertion order.
     * If the input string is null, blank, or contains no valid entries, an empty set is returned.
     *
     * @param csv the CSV string to be parsed; values are separated by commas
     * @return a set of trimmed, unique strings parsed from the CSV input, or an empty set if the input is null, blank, or contains no valid values
     */
    public static Set<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) return Set.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
