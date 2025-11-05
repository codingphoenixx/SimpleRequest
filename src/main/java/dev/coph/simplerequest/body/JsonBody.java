package dev.coph.simplerequest.body;

import dev.coph.simplelogger.Logger;
import dev.coph.simplerequest.util.JsonUtil;
import dev.coph.simplerequest.util.ResponseUtil;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.UUID;

public record JsonBody(String json) {

    public static String getString(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        if (!contains(data, key, response, callback, errorMessage)) return null;
        var dataString = data.getString(key);
        if (dataString.isBlank()) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        return dataString;
    }

    public static String getString(JSONObject data, String key) {
        if (!data.has(key)) {
            return null;
        }
        var dataString = data.getString(key);
        if (dataString.isBlank()) {
            return null;
        }
        return dataString;
    }

    public static Boolean getBoolean(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        if (!contains(data, key, response, callback, errorMessage)) return null;
        boolean returnData;
        try {
            returnData = data.getBoolean(key);
        } catch (JSONException e) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        return returnData;
    }

    public static Boolean getBoolean(JSONObject data, String key) {
        if (!data.has(key)) {
            return null;
        }
        try {
            return data.getBoolean(key);
        } catch (JSONException e) {
            return null;
        }
    }

    public static UUID getUUID(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        var dataString = getString(data, key, response, callback, errorMessage);

        if (dataString == null)
            return null;

        try {
            return UUID.fromString(dataString);
        } catch (Exception e) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
    }

    public static UUID getUUID(JSONObject data, String key) {
        var dataString = getString(data, key);

        if (dataString == null)
            return null;

        try {
            return UUID.fromString(dataString);
        } catch (Exception e) {
            return null;
        }
    }

    public static OffsetDateTime getOffsetDateTime(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        var dataString = getString(data, key, response, callback, errorMessage);
        if (dataString == null)
            return null;
        try {
            return OffsetDateTime.parse(dataString);
        } catch (Exception e) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
    }

    public static OffsetDateTime getOffsetDateTime(JSONObject data, String key) {
        var dataString = getString(data, key);
        if (dataString == null)
            return null;
        try {
            return OffsetDateTime.parse(dataString);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T extends Enum<T>> T getEnum(JSONObject data, String key, Class<T> enumClass, Response response, Callback callback, String errorMessage) {
        if (!contains(data, key, response, callback, errorMessage)) return null;

        String dataString = data.getString(key);
        if (dataString.isBlank()) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        var enumValue = getEnumValue(dataString.toUpperCase(), enumClass, null);
        if (enumValue == null) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        return enumValue;
    }

    public static <T extends Enum<T>> T getEnum(JSONObject data, String key, Class<T> enumClass, T defaultValue, Response response, Callback callback, String errorMessage) {
        if (!contains(data, key, response, callback, errorMessage)) return null;

        var dataString = data.getString(key);
        if (dataString.isBlank()) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        var enumValue = getEnumValue(dataString, enumClass, defaultValue);
        if (enumValue == null) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        return enumValue;
    }

    public static <T extends Enum<T>> T getEnum(JSONObject data, String key, Class<T> enumClass) {
        if (!data.has(key)) return null;

        String dataString = data.getString(key);
        if (dataString.isBlank()) {
            return null;
        }
        return getEnumValue(dataString.toUpperCase(), enumClass, null);
    }

    public static <T extends Enum<T>> T getEnum(JSONObject data, String key, Class<T> enumClass, T defaultValue) {
        if (!data.has(key)) return null;

        var dataString = data.getString(key);
        if (dataString.isBlank()) {
            return null;
        }
        return getEnumValue(dataString, enumClass, defaultValue);
    }

    public static Integer getInt(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        if (!data.has(key)) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        return data.getInt(key);
    }

    public static Integer getInt(JSONObject data, String key) {
        if (!data.has(key)) {
            return null;
        }
        return data.getInt(key);
    }

    public static Long getLong(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        if (!data.has(key)) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        return data.getLong(key);
    }

    public static Long getLong(JSONObject data, String key) {
        if (!data.has(key)) {
            return null;
        }
        return data.getLong(key);
    }

    private static void respondBadRequestParameter(String key, Response response, Callback callback, String errorMessage) {
        if (errorMessage == null)
            return;
        ResponseUtil.writeAnswer(response, callback, JsonUtil.prepare(JsonUtil.Type.ERROR, "Missing/Invalid data."));
        Logger.warn(errorMessage + "Missing/Invalid parameter: " + key);
        response.setStatus(HttpStatus.BAD_REQUEST_400);
        callback.succeeded();
    }

    private static boolean contains(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        if (data.has(key)) {
            return true;
        }
        if (errorMessage == null)
            return false;

        Logger.warn(errorMessage + "Missing parameter: " + key);
        response.setStatus(HttpStatus.BAD_REQUEST_400);
        ResponseUtil.writeAnswer(response, callback, JsonUtil.prepare(JsonUtil.Type.ERROR, "Missing/Invalid data."));
        return false;
    }

    private static <T extends Enum<T>> T getEnumValue(String string, Class<T> clazz, T defaultValue) {
        if (string == null) return defaultValue;

        try {
            return Enum.valueOf(clazz, string.trim().toUpperCase());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    public boolean isEmpty() {
        return json == null || json.isEmpty();
    }

    public JSONObject toJSONObject() {
        return new JSONObject(json);
    }

    public JSONArray toJSONArray() {
        return new JSONArray(json);
    }

    public String toString() {
        return json;
    }
}
