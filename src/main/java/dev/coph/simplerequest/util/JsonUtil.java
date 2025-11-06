package dev.coph.simplerequest.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

public class JsonUtil {

    private static final DateTimeFormatter ISO_INSTANT =
            DateTimeFormatter.ISO_INSTANT;

    public static JSONObject prepare(Type type) {
        var jsonObject = new JSONObject();
        switch (type) {
            case SUCCESS -> jsonObject.put("status", "success");
            case OK -> jsonObject.put("status", "ok");
            case ERROR -> jsonObject.put("status", "error");
        }
        return jsonObject;
    }

    public static JSONObject noPermission() {
        var jsonObject = prepare(Type.ERROR);
        jsonObject.put("message", "You don't have permission to access this resource.");
        return jsonObject;
    }

    public static JSONObject prepare(Type type, String message) {
        var jsonObject = prepare(type);
        jsonObject.put("message", message);
        return jsonObject;
    }

    public static String timeStampToJson(Timestamp ts) {
        var instant = ts.toInstant();
        return ISO_INSTANT.format(instant);
    }

    public static JSONObject toJsonObject(Map<?, ?> map) {
        if (map == null) return new JSONObject();
        JSONObject obj = new JSONObject();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (e.getKey() == null) continue;
            obj.put(String.valueOf(e.getKey()), wrap(e.getValue()));
        }
        return obj;
    }

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

    public static JSONArray toJsonArrayFromCollection(Collection<?> col) {
        JSONArray arr = new JSONArray();
        if (col == null) return arr;
        for (Object o : col) arr.put(wrap(o));
        return arr;
    }

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

    public enum Type {
        ERROR, SUCCESS, OK
    }
}
