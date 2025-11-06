package dev.coph.simplerequest.handler.field;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Represents a response builder for managing required and optional fields in a structured format.
 * This class is designed to facilitate the definition and organization of fields necessary for
 * various operations, separating required fields from optional fields, and generating a final
 * output based on the requested fields.
 */
public final class FieldResponse {
    private final Map<String, Supplier<Object>> required = new LinkedHashMap<>();
    private final Map<String, Supplier<Object>> optional = new LinkedHashMap<>();

    /**
     * Default constructor for the {@code FieldResponse} class.
     * Initializes a new instance of the {@code FieldResponse} object.
     */
    public FieldResponse() {
    }

    /**
     * Adds a required field to the response object with the specified name and value supplier.
     * Required fields are essential fields that must be included in the response.
     *
     * @param name     the name of the field to be added as required
     * @param supplier a supplier providing the value for the specified field
     * @return the current instance of {@code FieldResponse} to allow method chaining
     */
    public FieldResponse required(String name, Supplier<Object> supplier) {
        required.put(name, supplier);
        return this;
    }

    /**
     * Adds an optional field to the response object with the specified name and value supplier.
     * Optional fields are additional fields that can be included in the response, depending on the request.
     *
     * @param name     the name of the field to be added as optional
     * @param supplier a supplier providing the value for the specified field
     * @return the current instance of {@code FieldResponse} to allow method chaining
     */
    public FieldResponse optional(String name, Supplier<Object> supplier) {
        optional.put(name, supplier);
        return this;
    }

    /**
     * Constructs a map of field names and their corresponding values based on the sets of requested
     * and required field names. The method ensures all fields specified in the required set are included
     * in the output, while also considering the requested fields that match optional fields.
     *
     * @param requested     a set of field names that are optionally requested for inclusion in the output
     * @param requiredNames a set of field names that must be included in the output regardless of the request
     * @return a map where the key is the field name and the value is the result of calling the corresponding supplier
     */
    public Map<String, Object> build(Set<String> requested, Set<String> requiredNames) {
        LinkedHashSet<String> toEmit = new LinkedHashSet<>(requiredNames);
        for (String r : requested) {
            if (optional.containsKey(r)) toEmit.add(r);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (String name : toEmit) {
            var sup = required.getOrDefault(name, optional.get(name));
            if (sup != null) out.put(name, sup.get());
        }
        return out;
    }

    /**
     * Retrieves the set of all required field names for the response object.
     *
     * @return a set containing the names of the required fields
     */
    public Set<String> requiredNames() {
        return required.keySet();
    }

    /**
     * Retrieves the set of optional field names associated with the response object.
     *
     * @return a set containing the names of the optional fields
     */
    public Set<String> optionalNames() {
        return optional.keySet();
    }
}
