package dev.coph.simplerequest.util;

import dev.coph.simplelogger.Logger;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


/**
 * A utility class for handling image processing operations.
 * This class provides functionality to retrieve an image from an HTTP request.
 */
public class ImageUtil {

    /**
     * Retrieves an image from the given HTTP request.
     * The method tries to read and parse the image from the request's input stream.
     * If an error occurs or the image cannot be read, it returns null.
     *
     * @param request the HTTP request containing the image data
     * @return a BufferedImage object representing the image if successfully read, or null if an error occurs or no image is found
     */
    public static BufferedImage getImageFromRequest(Request request) {
        InputStream inputStream = Content.Source.asInputStream(request);
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            Logger.getInstance().error("Failed to read image.");
            Logger.getInstance().error(e);
            return null;
        }
        if (image == null) {
            System.out.println("Failed to read image.");
            return null;
        }
        return image;
    }
}
