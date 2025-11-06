package dev.coph.simplerequest.handler.field;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class FieldResponse {
    private final Map<String, Supplier<Object>> required = new LinkedHashMap<>();
    private final Map<String, Supplier<Object>> optional = new LinkedHashMap<>();

    public FieldResponse required(String name, Supplier<Object> supplier) {
        required.put(name, supplier);
        return this;
    }

    public FieldResponse optional(String name, Supplier<Object> supplier) {
        optional.put(name, supplier);
        return this;
    }

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

    public Set<String> requiredNames() {
        return required.keySet();
    }

    public Set<String> optionalNames() {
        return optional.keySet();
    }
}
