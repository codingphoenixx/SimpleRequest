package dev.coph.simplerequest.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

/**
 * Utility class for JSON-related operations.
 * Provides methods for creating and manipulating JSON objects and arrays.
 */
public class JsonUtil {
    /**
     * A {@code DateTimeFormatter} that formats or parses an instant in UTC, such as {@code 2007-12-03T10:15:30.00Z}.
     * <p>
     * This formatter is pre-defined in the {@code DateTimeFormatter} class as {@code DateTimeFormatter.ISO_INSTANT}.
     * It is used for formatting and parsing dates and times in the ISO-8601 instant format, with the zone offset
     * always set to UTC. The format consists of:
     * - The date in the format {@code yyyy-MM-dd}.
     * - The character {@code 'T'} to separate the date and time.
     * - The time in the format {@code HH:mm:ss} optionally followed by a fraction of a second.
     * - The character {@code 'Z'} to indicate UTC.
     * <p>
     * This field is used as a utility constant to ensure a standard way of handling instant formatting or parsing
     * across the methods or functionality provided by the containing class.
     */
    private static final DateTimeFormatter ISO_INSTANT =
            DateTimeFormatter.ISO_INSTANT;

    /**
     * Private constructor for the {@code JsonUtil} class.
     * This constructor prevents instantiation of the utility class, as all methods are
     * static and the class is designed to hold static utility methods for JSON-related operations.
     */
    private JsonUtil() {
    }

    /**
     * Prepares a JSON object with a specific status based on the provided type.
     *
     * @param type the type of status to set in the resulting JSON object.
     *             Possible values are defined in the {@code Type} enum: {@code SUCCESS}, {@code OK}, or {@code ERROR}.
     * @return a {@code JSONObject} containing a "status" key with a value corresponding to the provided type:
     * "success" for {@code SUCCESS}, "ok" for {@code OK}, and "error" for {@code ERROR}.
     */
    public static JSONObject prepare(Type type) {
        var jsonObject = new JSONObject();
        switch (type) {
            case SUCCESS -> jsonObject.put("status", "success");
            case OK -> jsonObject.put("status", "ok");
            case ERROR -> jsonObject.put("status", "error");
        }
        return jsonObject;
    }

    /**
     * Creates a JSON response indicating that the user does not have permission to access the resource.
     * The returned JSON object contains a status set to "error" and an explanatory message.
     *
     * @return a {@code JSONObject} with a "status" key set to "error" and a "message" key
     * explaining the lack of permission.
     */
    public static JSONObject noPermission() {
        var jsonObject = prepare(Type.ERROR);
        jsonObject.put("message", "You don't have permission to access this resource.");
        return jsonObject;
    }

    /**
     * Prepares a JSON object with a specific status and additional message based on the provided type.
     *
     * @param type    the type of status to set in the resulting JSON object.
     *                Possible values are defined in the {@code Type} enum: {@code SUCCESS}, {@code OK}, or {@code ERROR}.
     * @param message the additional message to include in the resulting JSON object under the "message" key.
     * @return a {@code JSONObject} containing a "status" key with a value corresponding to the provided type:
     * "success" for {@code SUCCESS}, "ok" for {@code OK}, and "error" for {@code ERROR},
     * as well as a "message" key with the provided message.
     */
    public static JSONObject prepare(Type type, String message) {
        var jsonObject = prepare(type);
        jsonObject.put("message", message);
        return jsonObject;
    }

    /**
     * Converts a {@code Timestamp} object into a JSON-compatible {@code String}
     * formatted according to the ISO-8601 standard.
     *
     * @param ts the {@code Timestamp} to be converted into a JSON string
     * @return a {@code String} representing the {@code Timestamp} in ISO-8601 format
     */
    public static String timeStampToJson(Timestamp ts) {
        var instant = ts.toInstant();
        return ISO_INSTANT.format(instant);
    }

    /**
     * Converts a specified map into a {@code JSONObject}.
     * Keys in the map are converted to strings, while values are serialized into JSON-compatible objects.
     *
     * @param map the map to be converted into a {@code JSONObject};
     *            if {@code map} is null, an empty {@code JSONObject} is returned
     * @return a {@code JSONObject} representation of the provided map with keys transformed to strings
     * and values appropriately wrapped for JSON compatibility
     */
    public static JSONObject toJsonObject(Map<?, ?> map) {
        if (map == null) return new JSONObject();
        JSONObject obj = new JSONObject();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (e.getKey() == null) continue;
            obj.put(String.valueOf(e.getKey()), wrap(e.getValue()));
        }
        return obj;
    }

    /**
     * Wraps the provided object into a JSON-compatible representation.
     * This method ensures that the object is converted into a format suitable
     * for inclusion in a JSON structure (e.g., {@code JSONObject}, {@code JSONArray}, or primitives).
     *
     * @param v the object to be wrapped; can be a primitive, a collection, a map,
     *          an array, an enum, or any other object
     * @return a JSON-compatible representation of the provided object:
     * - {@code JSONObject.NULL} for {@code null}
     * - the object itself if it is an instance of {@code Number}, {@code Boolean},
     * {@code JSONObject}, or {@code JSONArray}
     * - a {@code JSONObject} if the object is a map
     * - a {@code JSONArray} if the object is a collection or array
     * - the name of the enum constant if the object is an enum
     * - the string representation of the object otherwise
     */
    private static Object wrap(Object v) {
        if (v == null) return JSONObject.NULL;
        if (v instanceof Number || v instanceof Boolean) return v;
        if (v instanceof JSONObject || v instanceof JSONArray) return v;
        if (v instanceof Map<?, ?> m) return toJsonObject(m);
        if (v instanceof Collection<?> c) return toJsonArrayFromCollection(c);
        if (v.getClass().isArray()) return toJsonArray(v);
        if (v instanceof Enum<?> e) return e.name();
        return String.valueOf(v);
    }

    /**
     * Converts a given collection into a {@code JSONArray}.
     * Each element in the collection is wrapped into a JSON-compatible representation
     * using the {@code wrap(Object)} method before being added to the resulting {@code JSONArray}.
     *
     * @param col the collection to be converted into a {@code JSONArray};
     *            if {@code col} is null, an empty {@code JSONArray} is returned
     * @return a {@code JSONArray} containing JSON-compatible representations of the elements
     * in the provided collection
     */
    public static JSONArray toJsonArrayFromCollection(Collection<?> col) {
        JSONArray arr = new JSONArray();
        if (col == null) return arr;
        for (Object o : col) arr.put(wrap(o));
        return arr;
    }

    /**
     * Converts the given object, assumed to be an array, into a {@code JSONArray}.
     * If the provided object is null, an empty {@code JSONArray} is returned.
     * For non-array objects, the object is wrapped using a helper method
     * and returned as a single element in the result {@code JSONArray}.
     *
     * @param array the object to be converted; can be an array of any type.
     *              If the object is not an array or is null, appropriate handling
     *              will be performed to wrap the object into a {@code JSONArray}.
     * @return a {@code JSONArray} representation of the input array object.
     * Returns an empty {@code JSONArray} if the provided object is null
     * or not an array.
     */
    public static JSONArray toJsonArray(Object array) {
        if (array == null) return new JSONArray();
        if (!array.getClass().isArray()) return new JSONArray().put(wrap(array));
        int len = Array.getLength(array);
        JSONArray arr = new JSONArray();
        for (int i = 0; i < len; i++) {
            arr.put(wrap(Array.get(array, i)));
        }
        return arr;
    }

    /**
     * Enum representing the type of status or response used in JSON-related operations.
     * <p>
     * This enum is primarily utilized within the {@code JsonUtil} class to define
     * specific statuses that can be represented in JSON objects.
     * <p>
     * The available types are:
     * - {@code ERROR}: Represents an error status.
     * - {@code SUCCESS}: Represents a success status.
     * - {@code OK}: Represents an okay or neutral status.
     */
    public enum Type {
        /**
         * Represents an error status in JSON-related operations.
         * <p>
         * This constant is part of the {@code Type} enum, which is used to define
         * specific statuses or responses that can be represented in JSON objects.
         * The {@code ERROR} status typically indicates that an operation has failed
         * or encountered an issue.
         */
        ERROR,
        /**
         * Represents a success status in JSON-related operations.
         * <p>
         * This constant is part of the {@code Type} enum, which is used to define
         * specific statuses or responses that can be represented in JSON objects.
         * The {@code SUCCESS} status typically indicates that an operation has been
         * completed successfully without any errors or issues.
         */
        SUCCESS,
        /**
         * Represents an okay or neutral status in JSON-related operations.
         * <p>
         * This constant is part of the {@code Type} enum, which is used to define
         * specific statuses or responses that can be represented in JSON objects.
         * The {@code OK} status typically indicates that an operation has proceeded
         * as expected without errors but may not explicitly signify success.
         */
        OK
    }
}
