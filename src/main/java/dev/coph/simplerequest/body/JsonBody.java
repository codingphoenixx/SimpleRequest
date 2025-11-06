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

/**
 * A utility class for extracting and validating data from JSON objects.
 * Provides methods for retrieving various types of data from JSON objects,
 * including strings, booleans, UUIDs, dates, and enums, with error handling
 * capabilities for invalid or missing data.
 * @param json the JSON object from which to retrieve data
 */
public record JsonBody(String json) {

    /**
     * Retrieves a non-blank string value associated with the specified key from the given JSON object.
     * If the key does not exist in the JSON object, or the value is blank, it sends a bad request
     * response using the provided response, callback, and error message.
     *
     * @param data         the JSONObject from which to retrieve the value
     * @param key          the key whose associated value is to be returned
     * @param response     the Response object used to send a bad request in case of errors
     * @param callback     the Callback to handle the response
     * @param errorMessage the message to log in case of missing or invalid data
     * @return the non-blank string value associated with the key, or null if the data is invalid or missing
     */
    public static String getString(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        if (!contains(data, key, response, callback, errorMessage)) return null;
        var dataString = data.getString(key);
        if (dataString.isBlank()) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        return dataString;
    }

    /**
     * Retrieves a non-blank string value associated with the specified key from the given JSON object.
     * If the key does not exist in the JSON object, or the value is blank, it returns null.
     *
     * @param data the JSONObject from which to retrieve the value
     * @param key  the key whose associated value is to be returned
     * @return the non-blank string value associated with the key, or null if the key does not exist or the value is blank
     */
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

    /**
     * Retrieves a boolean value associated with the specified key from the given JSON object.
     * If the key does not exist or its value cannot be converted to a boolean, it sends a bad request
     * response using the provided response, callback, and error message.
     *
     * @param data         the JSONObject from which to retrieve the value
     * @param key          the key whose associated value is to be returned
     * @param response     the Response object used to send a bad request in case of errors
     * @param callback     the Callback to handle the response
     * @param errorMessage the message to log in case of missing or invalid data
     * @return the boolean value associated with the key, or null if the key is missing or its value is invalid
     */
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

    /**
     * Retrieves a boolean value associated with the specified key from the given JSON object.
     * If the key does not exist or its value cannot be converted to a boolean, it returns null.
     *
     * @param data the JSONObject from which to retrieve the value
     * @param key  the key whose associated value is to be returned
     * @return the boolean value associated with the key, or null if the key is missing or its value is invalid
     */
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

    /**
     * Retrieves a UUID value associated with the specified key from the given JSON object.
     * If the key does not exist in the JSON object, or its value cannot be converted to a UUID,
     * it sends a bad request response using the provided response, callback, and error message.
     *
     * @param data         the JSONObject from which to retrieve the value
     * @param key          the key whose associated value is to be returned
     * @param response     the Response object used to send a bad request in case of errors
     * @param callback     the Callback to handle the response
     * @param errorMessage the message to log in case of missing or invalid data
     * @return the UUID value associated with the key, or null if the key does not exist,
     * the value is not a valid UUID, or an error occurs
     */
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

    /**
     * Retrieves a UUID value associated with the specified key from the given JSON object.
     * If the key does not exist in the JSON object, or the value cannot be converted to a UUID, it returns null.
     *
     * @param data the JSONObject from which to retrieve the value
     * @param key  the key whose associated value is to be returned
     * @return the UUID value associated with the key, or null if the key does not exist, the value is not a valid UUID, or an error occurs
     */
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

    /**
     * Retrieves an {@link OffsetDateTime} value associated with the specified key from the given JSON object.
     * If the key does not exist in the JSON object, its value is null, or the value cannot be parsed as
     * an {@link OffsetDateTime}, it sends a bad request response using the provided response, callback,
     * and error message.
     *
     * @param data         the JSONObject from which to retrieve the value
     * @param key          the key whose associated value is to be returned
     * @param response     the Response object used to send a bad request in case of errors
     * @param callback     the Callback to handle the response
     * @param errorMessage the message to log in case of missing or invalid data
     * @return the {@link OffsetDateTime} value associated with the key, or null if the key does not exist,
     * the value is not a valid {@link OffsetDateTime}, or an error occurs
     */
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

    /**
     * Retrieves an {@link OffsetDateTime} value associated with the specified key from the given JSON object.
     * If the key does not exist in the JSON object, its value is null, or the value cannot be parsed as
     * an {@link OffsetDateTime}, it returns null.
     *
     * @param data the JSONObject from which to retrieve the value
     * @param key  the key whose associated value is to be returned
     * @return the {@link OffsetDateTime} value associated with the key, or null if the key does not exist,
     * the value is null, or the value is not a valid {@link OffsetDateTime}
     */
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

    /**
     * Retrieves an enum value from the given JSON object based on the specified key.
     * If the key does not exist, if the value is blank, or if it cannot be matched
     * to an enum constant, the method handles the error using the provided response
     * and callback, and returns null.
     *
     * @param <T>          the type of the enum
     * @param data         the JSONObject containing the data
     * @param key          the key in the JSONObject to look up the value
     * @param enumClass    the class of the enum to which the value belongs
     * @param response     the response object used for handling errors
     * @param callback     the callback object used for handling errors
     * @param errorMessage the error message to include in error handling
     * @return the corresponding enum value if found and valid, otherwise null
     */
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

    /**
     * Retrieves an enum value from the provided JSON object based on the specified key.
     * If the key does not exist, the method returns null. If the key exists but the
     * value is invalid or cannot be mapped to the provided enum type, the method
     * sends a bad request response and also returns null.
     *
     * @param <T>          The type of the enum.
     * @param data         The JSON object containing the key-value pair to retrieve the enum from.
     * @param key          The key in the JSON object associated with the enum value.
     * @param enumClass    The class of the enum to be retrieved.
     * @param defaultValue The default enum value to return if the key's value is not valid.
     * @param response     The response object used to send error responses.
     * @param callback     The callback to execute in case of an error.
     * @param errorMessage The error message to be used in case of an invalid or missing value.
     * @return The enum value associated with the key or null if the key is missing or the value
     * cannot be parsed as a valid enum constant.
     */
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

    /**
     * Retrieves an enum value from a JSON object based on the specified key.
     *
     * @param <T>       The type of the enum.
     * @param data      The JSON object containing the key-value pair.
     * @param key       The key whose associated value is to be retrieved and converted to an enum.
     * @param enumClass The class of the enum from which the value is to be retrieved.
     * @return The enum value corresponding to the key, or null if the key does not exist, the value is blank,
     * or does not match any enum constant.
     */
    public static <T extends Enum<T>> T getEnum(JSONObject data, String key, Class<T> enumClass) {
        if (!data.has(key)) return null;

        String dataString = data.getString(key);
        if (dataString.isBlank()) {
            return null;
        }
        return getEnumValue(dataString.toUpperCase(), enumClass, null);
    }

    /**
     * Retrieves an enum value from a JSONObject based on the specified key.
     *
     * @param data         the JSONObject containing the key-value pair
     * @param key          the key whose corresponding value will be used to determine the enum
     * @param enumClass    the class type of the enum to be retrieved
     * @param defaultValue the default enum value to return if the key's value doesn't match any enum constant
     * @param <T>          the specific enum type
     * @return the corresponding enum value if the key exists and matches a valid enum constant,
     * the default value if no match is found, or null if the key is not present or its value is blank
     */
    public static <T extends Enum<T>> T getEnum(JSONObject data, String key, Class<T> enumClass, T defaultValue) {
        if (!data.has(key)) return null;

        var dataString = data.getString(key);
        if (dataString.isBlank()) {
            return null;
        }
        return getEnumValue(dataString, enumClass, defaultValue);
    }

    /**
     * Retrieves an integer value associated with the specified key from the given JSON object.
     * If the key does not exist in the JSON object, it triggers a bad request response using the provided callback.
     *
     * @param data         the JSON object containing the data
     * @param key          the key whose associated integer value is to be retrieved
     * @param response     the response object used to send error responses
     * @param callback     the callback to be executed in case of an error
     * @param errorMessage the error message to be included in the response in case the key is missing
     * @return the integer value associated with the specified key, or null if the key is not present in the JSON object
     */
    public static Integer getInt(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        if (!data.has(key)) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        return data.getInt(key);
    }

    /**
     * Retrieves an integer value associated with the specified key from the given JSONObject.
     *
     * @param data the JSONObject containing the key-value pairs
     * @param key  the key whose associated integer value is to be retrieved
     * @return the integer value associated with the specified key, or null if the key does not exist
     */
    public static Integer getInt(JSONObject data, String key) {
        if (!data.has(key)) {
            return null;
        }
        return data.getInt(key);
    }

    /**
     * Retrieves the value associated with the specified key from the provided JSONObject as a Long.
     * If the key does not exist in the JSONObject, it triggers a bad request response and returns null.
     *
     * @param data         the JSONObject containing the data
     * @param key          the key whose associated value is to be retrieved
     * @param response     the response object used to send any error responses
     * @param callback     the callback to handle the response in case of an error
     * @param errorMessage the error message to send if the key does not exist in the JSONObject
     * @return the Long value associated with the specified key, or null if the key is not present
     */
    public static Long getLong(JSONObject data, String key, Response response, Callback callback, String errorMessage) {
        if (!data.has(key)) {
            respondBadRequestParameter(key, response, callback, errorMessage);
            return null;
        }
        return data.getLong(key);
    }

    /**
     * Retrieves the value of the specified key from the provided JSONObject as a Long.
     * If the key is not present in the JSONObject, null is returned.
     *
     * @param data the JSONObject from which to retrieve the value
     * @param key  the key whose associated value is to be returned
     * @return the Long value associated with the specified key, or null if the key does not exist
     */
    public static Long getLong(JSONObject data, String key) {
        if (!data.has(key)) {
            return null;
        }
        return data.getLong(key);
    }

    /**
     * Sends a bad request response indicating a missing or invalid parameter.
     *
     * @param key          the name of the parameter that is missing or invalid
     * @param response     the response object used to send the error response
     * @param callback     the callback to handle the completion of the response
     * @param errorMessage the error message to be logged alongside the parameter information
     */
    private static void respondBadRequestParameter(String key, Response response, Callback callback, String errorMessage) {
        if (errorMessage == null)
            return;
        ResponseUtil.writeAnswer(response, callback, JsonUtil.prepare(JsonUtil.Type.ERROR, "Missing/Invalid data."));
        Logger.warn(errorMessage + "Missing/Invalid parameter: " + key);
        response.setStatus(HttpStatus.BAD_REQUEST_400);
        callback.succeeded();
    }

    /**
     * Checks if the specified key exists in the given JSON object. If the key is missing,
     * an error message is logged, the response status is set to BAD REQUEST, and an error
     * response is written using the provided callback.
     *
     * @param data         the JSON object to be checked for the presence of the key
     * @param key          the key to check in the JSON object
     * @param response     the HTTP response object to set the status if the key is missing
     * @param callback     the callback used to write the error response if the key is missing
     * @param errorMessage the error message to log when the key is missing; if null, no message is logged
     * @return true if the specified key exists in the JSON object; false otherwise
     */
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

    /**
     * Retrieves the enum value matching the input string within the provided enum class.
     * If the string is null or if no matching enum value is found, the default value is returned.
     *
     * @param <T>          the type of the enum
     * @param string       the input string representing the name of the enum value
     * @param clazz        the Class object of the enum type
     * @param defaultValue the default enum value to return if no match is found or if the string is null
     * @return the enum value corresponding to the input string, or the default value if no match is found
     */
    private static <T extends Enum<T>> T getEnumValue(String string, Class<T> clazz, T defaultValue) {
        if (string == null) return defaultValue;

        try {
            return Enum.valueOf(clazz, string.trim().toUpperCase());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    /**
     * Checks if the JSON object is empty or null.
     *
     * @return true if the JSON object is null or contains no key-value pairs, false otherwise
     */
    public boolean isEmpty() {
        return json == null || json.isEmpty();
    }

    /**
     * Converts the underlying data into a JSON object representation.
     *
     * @return a JSONObject representing the current data
     */
    public JSONObject toJSONObject() {
        return new JSONObject(json);
    }

    /**
     * Converts the internal JSON representation into a JSONArray object.
     *
     * @return a JSONArray containing the elements of the internal JSON data
     */
    public JSONArray toJSONArray() {
        return new JSONArray(json);
    }

    /**
     * Returns a string representation of the object.
     * This implementation returns the JSON representation of the object as a string.
     *
     * @return the JSON string representation of this object
     */
    @Override
    public String toString() {
        return json;
    }
}
