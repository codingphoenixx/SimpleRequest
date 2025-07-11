package dev.coph.simplerequest.util;

import dev.coph.simplelogger.Logger;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Utility class for handling request-related operations.
 * This class provides methods to manage and process HTTP responses.
 */
public class ResponseUtil {

    /**
     * Private constructor to prevent instantiation of the utility class.
     * <p>
     * The ResponseUtil class is designed to provide static utility methods for handling
     * request-related operations and should not be instantiated.
     */
    private ResponseUtil() {
    }

    /**
     * Writes the given answer as a response using the specified callback.
     * This method attempts to encode the answer into bytes and write it to the response.
     * In case of an exception, it logs the error and returns false.
     *
     * @param response the Response object to which the answer will be written
     * @param callback the Callback to be executed upon completion of the write operation
     * @param answer   the String containing the answer to be written
     * @return true if the answer is successfully written, false if an error occurs
     */
    public static boolean writeAnswer(Response response, Callback callback, String answer) {
        try {
            if (answer != null && !answer.isEmpty())
                response.write(true, ByteBuffer.wrap(answer.getBytes()), callback);
        } catch (Exception e) {
            Logger.getInstance().error("Error writing answer.");
            Logger.getInstance().error(e);
            return false;
        }
        return true;
    }

    /**
     * Writes the given JSON answer as a response using the specified callback.
     * This method converts the JSONObject into a string and delegates the actual
     * writing operation to another overloaded method.
     *
     * @param response the Response object to which the answer will be written
     * @param callback the Callback to be executed upon completion of the write operation
     * @param answer   the JSONObject containing the answer to be written
     * @return true if the answer is successfully written, false if an error occurs
     */
    public static boolean writeAnswer(Response response, Callback callback, JSONObject answer) {
        if (answer != null)
            return writeAnswer(response, callback, answer.toString());
        return writeAnswer(response, callback, "");
    }
    /**
     * Writes the given JSON answer as a response using the specified callback.
     * This method converts the JSONArray into a string and delegates the actual
     * writing operation to another overloaded method.
     *
     * @param response the Response object to which the answer will be written
     * @param callback the Callback to be executed upon completion of the write operation
     * @param answer   the JSONArray containing the answer to be written
     * @return true if the answer is successfully written, false if an error occurs
     */
    public static boolean writeAnswer(Response response, Callback callback, JSONArray answer) {
        if (answer != null)
            return writeAnswer(response, callback, answer.toString());
        return writeAnswer(response, callback, "");
    }

    /**
     * Writes the given image as a response using the specified callback.
     * This method encodes the image into bytes using the specified format
     * and writes it to the response. In case of an exception, it logs the
     * error and returns false.
     *
     * @param response    the Response object to which the image will be written
     * @param callback    the Callback to be executed upon completion of the write operation
     * @param answerImage the BufferedImage containing the image to be written
     * @param format      the format in which the image should be encoded (e.g., "png", "jpg")
     * @return true if the image is successfully written, false if an error occurs
     */
    public static boolean writeSuccessfulAnswer(Response response, Callback callback, BufferedImage answerImage, String format) {
        if (format == null)
            return false;
        if (answerImage == null)
            return false;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(answerImage, format, byteArrayOutputStream);

            response.getHeaders().add(HttpHeader.CONTENT_TYPE, "image/" + format);
            response.write(true, ByteBuffer.wrap(byteArrayOutputStream.toByteArray()), callback);
            callback.succeeded();
        } catch (Exception e) {
            Logger.getInstance().error("Error writing answer.");
            Logger.getInstance().error(e);
            return false;
        }
        return true;
    }


    /**
     * Writes the given answer as a response using the specified callback.
     * This method attempts to encode the answer into bytes and write it to the response.
     * In case of an exception, it logs the error and returns false.
     *
     * @param response the Response object to which the answer will be written
     * @param callback the Callback to be executed upon completion of the write operation
     * @param answer   the String containing the answer to be written
     * @return true if the answer is successfully written, false if an error occurs
     */
    public static boolean writeSuccessfulAnswer(Response response, Callback callback, String answer) {
        try {
            if (answer != null && !answer.isEmpty())
                response.write(true, ByteBuffer.wrap(answer.getBytes()), callback);
            callback.succeeded();
        } catch (Exception e) {
            Logger.getInstance().error("Error writing answer.");
            Logger.getInstance().error(e);
            return false;
        }
        return true;
    }

    /**
     * Writes the given JSON answer as a response using the specified callback.
     * This method converts the JSONObject into a string and delegates the actual
     * writing operation to another overloaded method.
     *
     * @param response the Response object to which the answer will be written
     * @param callback the Callback to be executed upon completion of the write operation
     * @param answer   the JSONObject containing the answer to be written
     * @return true if the answer is successfully written, false if an error occurs
     */
    public static boolean writeSuccessfulAnswer(Response response, Callback callback, JSONObject answer) {
        if (answer != null)
            return writeSuccessfulAnswer(response, callback, answer.toString());
        return writeSuccessfulAnswer(response, callback, "");
    }
    /**
     * Writes the given JSON answer as a response using the specified callback.
     * This method converts the JSONArray into a string and delegates the actual
     * writing operation to another overloaded method.
     *
     * @param response the Response object to which the answer will be written
     * @param callback the Callback to be executed upon completion of the write operation
     * @param answer   the JSONArray containing the answer to be written
     * @return true if the answer is successfully written, false if an error occurs
     */
    public static boolean writeSuccessfulAnswer(Response response, Callback callback, JSONArray answer) {
        if (answer != null)
            return writeSuccessfulAnswer(response, callback, answer.toString());
        return writeSuccessfulAnswer(response, callback, "");
    }

}
