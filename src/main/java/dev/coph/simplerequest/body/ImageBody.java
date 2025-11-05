package dev.coph.simplerequest.body;

import lombok.Getter;
import lombok.experimental.Accessors;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Getter
@Accessors(fluent = true)
public class ImageBody {
    private final InputStream inputStream;
    private final BufferedImage bufferedImage;

    public ImageBody(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        this.bufferedImage = ImageIO.read(inputStream);
    }


}
