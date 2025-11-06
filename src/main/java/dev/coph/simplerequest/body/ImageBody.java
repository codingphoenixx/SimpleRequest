package dev.coph.simplerequest.body;

import lombok.experimental.Accessors;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an image body by encapsulating its content as both an input stream
 * and a buffered image. This class facilitates the manipulation and retrieval
 * of image data in two commonly used formats.
 * <p>
 * Features:
 * - Provides access to the raw input stream of the image data.
 * - Automatically converts the input stream into a {@link BufferedImage} for convenient image manipulation.
 * <p>
 * This class is immutable once an instance is created.
 */
@Accessors(fluent = true)
public class ImageBody {
    private final InputStream inputStream;
    private final BufferedImage bufferedImage;

    /**
     * Constructs an instance of the {@code ImageBody} class by initializing it
     * with an input stream of image data. The provided {@link InputStream} is
     * also used to create a {@link BufferedImage} for subsequent image processing.
     *
     * @param inputStream the input stream containing the raw image data
     * @throws IOException if an error occurs while reading the input stream or converting it
     *                     to a {@link BufferedImage}
     */
    public ImageBody(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        this.bufferedImage = ImageIO.read(inputStream);
    }

    /**
     * Returns the input stream containing the raw image data.
     *
     * @return the input stream associated with the image data
     */
    public InputStream inputStream() {
        return this.inputStream;
    }

    /**
     * Returns the buffered image representation of the image data.
     *
     * @return the {@link BufferedImage} associated with the image data
     */
    public BufferedImage bufferedImage() {
        return this.bufferedImage;
    }
}
