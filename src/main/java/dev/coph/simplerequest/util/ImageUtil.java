package dev.coph.simplerequest.util;

import dev.coph.simplelogger.Logger;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


/**
 * A utility class for handling image processing operations.
 * This class provides functionality to retrieve an image from an HTTP request.
 */
public class ImageUtil {
    /**
     * Private constructor to prevent instantiation of the utility class.
     * <p>
     * The ImageUtil class is intended to provide static methods for image processing
     * and should not be instantiated.
     */
    private ImageUtil() {

    }


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
            Logger.instance().error("Failed to read image.");
            Logger.instance().error(e);
            return null;
        }
        if (image == null) {
            System.out.println("Failed to read image.");
            return null;
        }
        return image;
    }

    /**
     * Prepares an image for JPEG conversion by creating a new image with a specified
     * background color and converting it to the RGB color model.
     *
     * @param image           the original BufferedImage to be converted
     * @param backgroundColor the Color to be used as the background for the new image
     * @return a BufferedImage object in the RGB color model, ready for JPEG conversion
     */
    public static BufferedImage prepareImageToConvertToJPEG(BufferedImage image, Color backgroundColor) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        convertedImage.createGraphics().drawImage(image, 0, 0, backgroundColor, null);
        return convertedImage;
    }

    /**
     * Scales the given image to the specified width and height using a fast scaling algorithm.
     *
     * @param image  the BufferedImage to be scaled
     * @param width  the desired width of the scaled image
     * @param height the desired height of the scaled image
     * @return a new BufferedImage object representing the scaled image
     */
    public static BufferedImage scale(BufferedImage image, int width, int height) {
        Image tmp = image.getScaledInstance(width, height, BufferedImage.SCALE_FAST);
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        scaled.getGraphics().drawImage(tmp, 0, 0, null);
        return scaled;
    }
}
