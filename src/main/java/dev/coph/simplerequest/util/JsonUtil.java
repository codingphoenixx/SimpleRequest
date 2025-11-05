package dev.coph.simplerequest.util;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

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

    public enum Type {
        ERROR, SUCCESS, OK
    }
}
