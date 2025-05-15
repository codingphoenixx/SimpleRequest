package dev.coph.simplerequest.security.captcha;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Generates CAPTCHA images and their respective text for validation purposes.<br>
 * <br>
 * This class provides functionality to create CAPTCHAs with customizable properties
 * such as text length, image dimensions, font style, distortions, and background noise. <br>
 * The generated CAPTCHA can include various visual elements to make it harder for
 * automated systems to interpret, while remaining user-readable.<br>
 * <br>
 * The customization options include:<br>
 * <br>
 * - Text and character set customization (e.g., fixed or random text, allowed characters).<br>
 * - Visual distortion effects (e.g., rotation, shear, wave patterns).<br>
 * - Background modifications (e.g., noise or solid color).<br>
 * - Line overlays for added difficulty.<br>
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class CaptchaGenerator {
    /**
     * Private constructor for the CaptchaGenerator class.<br>
     * <br>
     * This constructor is intentionally private to prevent instantiation of the class.<br>
     * CaptchaGenerator is designed to function as a utility class that provides methods
     * to generate CAPTCHAs. All functionalities are handled through static methods, and
     * no instances of this class are required or allowed.<br>
     */
    private CaptchaGenerator() {
    }

    /**
     * Represents a CAPTCHA composed of an image and the corresponding text.<br>
     * <br>
     * The Captcha class encapsulates a generated CAPTCHA image and its associated
     * text, typically used for validating user input in forms or other authentication
     * mechanisms. This class provides methods to access the CAPTCHA image and text.
     */
    public static class Captcha {
        private final BufferedImage image;
        private final String text;

        /**
         * Initializes a new instance of the Captcha class with the specified image and text.
         *
         * @param image The BufferedImage object representing the generated CAPTCHA image.
         * @param text  The String containing the text associated with the CAPTCHA.
         */
        public Captcha(BufferedImage image, String text) {
            this.image = image;
            this.text = text;
        }

        /**
         * Retrieves the CAPTCHA image.
         *
         * @return The BufferedImage object representing the generated CAPTCHA image.
         */
        public BufferedImage image() {
            return image;
        }

        /**
         * Retrieves the text associated with the CAPTCHA.
         *
         * @return The String containing the text of the CAPTCHA.
         */
        public String text() {
            return text;
        }
    }

    /**
     * A string containing the characters used to generate CAPTCHA text.<br>
     * <br>
     * This variable holds a predefined set of characters from which random selections
     * are made to construct the text for CAPTCHA challenges. Excludes visually
     * ambiguous characters like 'I', 'O', '0', and 'l'.<br>
     */
    private String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";
    /**
     * Represents the height of the CAPTCHA image in pixels.<br>
     * <br>
     * This variable determines the vertical dimension of the CAPTCHA image generated
     * by the CaptchaGenerator class. It is used to specify the height during the
     * CAPTCHA creation process, allowing customization of the output image's size.<br>
     * The height value must be a positive integer.<br>
     */
    private int height = 50;
    /**
     * Specifies the padding value used to determine the spacing around the CAPTCHA's content.<br>
     * <br>
     * The padding defines the amount of space to leave between the edges of the CAPTCHA image
     * and the content (e.g., text, lines, shapes) within it. This can help to ensure proper
     * alignment and visibility of the CAPTCHA content without getting too close to the image borders.<br>
     */
    private int padding = 10;
    /**
     * Defines the padding to be added around each character in the generated CAPTCHA.<br>
     * <br>
     * The charPadding variable determines the amount of space (in pixels) <br>
     * to include around individual characters rendered within the CAPTCHA image. <br>
     * Adjusting this value can affect the spacing between characters
     * and their visual appearance in the generated CAPTCHA.<br>
     */
    private int charPadding = 0;
    /**
     * Defines the color used for rendering the text in the CAPTCHA generation process.<br>
     * <br>
     * The textColor variable determines the color with which the text content
     * will be drawn onto the CAPTCHA image. By default, it is set to a black color.<br>
     * <br>
     * This field can be modified to change the text color according to the styling
     * or design preferences of the CAPTCHA output.<br>
     */
    private Color textColor = Color.BLACK;
    /**
     * Stores a predefined text that will be used as the CAPTCHA text.<br>
     * <br>
     * The fixedText variable allows specifying a constant text to be used
     * in the generated CAPTCHA. When this variable is set to a non-null value, <br>
     * it overrides the randomly generated text typically used for CAPTCHAs.<br>
     * If set to null, the CAPTCHA generator will revert to generating random text.<br>
     */
    private String fixedText = null;
    /**
     * Defines the length of the CAPTCHA text to be generated.<br>
     * <br>
     * This variable specifies the number of characters in the generated CAPTCHA text.<br>
     * It is used to control the complexity of the CAPTCHA, with a higher value
     * indicating a longer and potentially more secure CAPTCHA.<br>
     */
    private int captchaLength = 6;
    /**
     * The name of the font used for generating CAPTCHA text.<br>
     * <br>
     * This variable holds the font name as a String, which determines the
     * typeface applied to the text displayed in the CAPTCHA image. The
     * specified font name must be available on the system where the CAPTCHA
     * is generated to ensure proper rendering of the text.<br>
     */
    private final String fontName = "Arial";
    /**
     * Defines the style of the font used for generating the CAPTCHA text.<br>
     * <br>
     * The fontStyle variable determines styling attributes such as boldness or italics
     * for the font used in the CAPTCHA text rendering. It influences the visual
     * representation of the text, enhancing its obfuscation to prevent automated
     * recognition while maintaining readability for human users.<br>
     * <br>
     * The value for this variable is derived from predefined constant values
     * in the {@link Font} class (e.g., Font.PLAIN, Font.BOLD, Font.ITALIC).<br>
     */
    private final int fontStyle = Font.BOLD;
    /**
     * Specifies the font size used for rendering CAPTCHA text.<br>
     * <br>
     * The fontSize variable determines the size of the text rendered on the CAPTCHA
     * image. A larger font size will produce larger text, while a smaller font size
     * will result in smaller text. The value is measured in points.<br>
     */
    private int fontSize = 40;

    /**
     * Indicates whether rotation distortion is applied to the CAPTCHA text.<br>
     * <br>
     * When set to true, the characters in the generated CAPTCHA text are subjected
     * to a rotation effect, introducing distortion that makes it more challenging
     * for automated systems to recognize the text while remaining legible for humans.<br>
     * This option can be used to enhance the security of the CAPTCHA.<br>
     */
    private boolean rotationDistortion = false;
    /**
     * The maximum degree of rotation that can be applied to characters in the captcha. <br>
     * This value is used to introduce rotational distortion to each character, enhancing the difficulty of
     * automated decoding of the captcha while remaining readable by humans.<br>
     * <br>
     * A value greater than 0 allows rotation within the range [-maxRotationDegree, maxRotationDegree].<br>
     */
    private double maxRotationDegree = 15.0;

    /**
     * Controls whether shear distortion is applied to the CAPTCHA text.<br>
     * <br>
     * This variable determines if the text in the generated CAPTCHA image
     * will undergo shear distortion, which skews the text along either
     * the horizontal or vertical axis to make it more challenging for bots
     * to recognize while still being readable by humans.<br>
     */
    private boolean shearDistortion = false;
    /**
     * Defines the maximum shear factor used for applying skew or distortion to text in
     * the CAPTCHA image.<br>
     * <br>
     * This value determines the extent to which the text can be sheared horizontally and/or
     * vertically during the image generation process in order to enhance obfuscation and
     * make automated text recognition more difficult. The factor is typically a small
     * decimal value, where higher values indicate greater distortion.<br>
     */
    private double maxShearFactor = 0.15;

    /**
     * Indicates whether background noise is enabled in the CAPTCHA generation process.<br>
     * <br>
     * Background noise can be used to increase the complexity of the generated CAPTCHA
     * by introducing visual clutter, making automated decoding more difficult.<br>
     * <br>
     * If set to true, background noise will be applied during CAPTCHA creation.<br>
     * If set to false, no background noise will be added.<br>
     */
    private boolean backgroundNoise = false;
    /**
     * Indicates whether RGB noise should be applied to the background of the CAPTCHA image.<br>
     * <br>
     * If set to true, random RGB noise will be added to the background, making
     * the CAPTCHA more challenging to decipher for automated scripts while still
     * legible to humans. If set to false, the background will remain unaffected
     * by RGB noise.<br>
     */
    private boolean backgroundRGBNoise = false;
    /**
     * Specifies the amount of noise to be applied to the generated CAPTCHA image.<br>
     * <br>
     * This variable represents the density or intensity of background noise used
     * in the CAPTCHA image to enhance its security by making it more challenging
     * for automated systems to interpret the text. A higher value increases the
     * level of noise in the image.<br>
     */
    private int noiseAmount = 500;
    /**
     * Specifies the background color of the CAPTCHA image.<br>
     * <br>
     * This variable determines the fill color applied to the background
     * of the generated CAPTCHA. The background color enhances readability
     * and can be customized as needed. The default value is set to white.<br>
     */
    private Color backgroundColor = Color.WHITE;

    /**
     * Indicates whether line distortion is applied to the CAPTCHA.<br>
     * <br>
     * The `lineDistortion` variable determines if random lines should be drawn
     * on the generated CAPTCHA to increase its complexity and reduce the ability
     * of automated systems to decipher it. When set to `true`, lines will be
     * applied as a part of the distortion process; otherwise, they will not be used.<br>
     */
    private boolean lineDistortion = false;
    /**
     * Determines the number of noise lines to be applied as part of the CAPTCHA generation process.<br>
     * <br>
     * These lines are randomly drawn on the CAPTCHA image to add visual complexity, enhancing
     * security by making automated recognition more difficult. <br>
     * A higher value results in more noise lines being generated, while a lower value reduces it.<br>
     */
    private int numberOfLines = 10;
    /**
     * Determines whether the color of the distortion lines in the CAPTCHA
     * generation process should be randomized.<br>
     * <br>
     * If set to {@code true}, the color of the distortion lines is randomly
     * generated for each CAPTCHA, enhancing visual variability. If set to
     * {@code false}, a fixed color defined by the {@code lineColor} property
     * is used for the distortion lines.<br>
     */
    private boolean randomLineColor = false;
    /**
     * Specifies the color of the lines used in the CAPTCHA generation process.<br>
     * <br>
     * This variable determines the color applied to the distortion or noise lines
     * drawn on the CAPTCHA image, which are used to enhance security by
     * complicating automated recognition. If {@code randomLineColor} is true,
     * this value might be overridden by dynamically generated random colors
     * for each line.<br>
     */
    private Color lineColor = Color.GRAY;

    /**
     * Indicates whether wave distortion is applied to the CAPTCHA text.<br>
     * <br>
     * Wave distortion introduces a sinusoidal effect to the text in the CAPTCHA
     * to make it more difficult for bots to read. If set to {@code true},
     * the CAPTCHA's text will be distorted with waves. If set to {@code false},
     * no wave distortion will be applied.<br>
     */
    private boolean waveDistortion = false;
    /**
     * Represents the frequency of the sine wave used in the wave distortion effect
     * for the CAPTCHA image.<br>
     * <br>
     * This value determines how many oscillations occur per unit length in the wave
     * distortion. A higher value increases the frequency, resulting in a more tightly
     * packed wave pattern, while a lower value decreases the frequency, creating a
     * broader wave pattern.<br>
     * <br>
     * The wave distortion is applied as part of the CAPTCHA generation process to
     * make the image more resistant to automated recognition attempts.<br>
     */
    private final double waveFrequency = 0.1;
    /**
     * Represents the amplitude of the wave distortion applied to the CAPTCHA image.<br>
     * <br>
     * The wave amplitude determines the maximum displacement of the wave from its
     * central axis, directly affecting the intensity of the distortion. A higher
     * value increases the distortion effect, making the CAPTCHA image more challenging
     * to decode while potentially improving its resistance to automated attacks.<br>
     * <br>
     * This value is used in conjunction with the wave frequency to define the overall
     * wave distortion applied to the generated CAPTCHA.<br>
     */
    private final double waveAmplitude = 2.0;

    /**
     * Generates a CAPTCHA which consists of an image and its associated text.<br>
     * The generated CAPTCHA may include features such as random distortion,
     * background noise, and noise lines for improved security. The CAPTCHA image
     * is generated based on various configurable properties such as font, text length,
     * distortion attributes, and colors. The method ensures uniqueness for each CAPTCHA
     * by introducing randomization in its elements.<br>
     * <br>
     *
     * @return A newly generated {@link Captcha} object that includes the CAPTCHA image
     * and the corresponding verification text string.<br>
     */
    public Captcha generateCaptcha() {
        String text;
        if (fixedText != null) {
            text = fixedText;
        } else {
            StringBuilder sb = new StringBuilder();
            Random rand = new Random();
            for (int i = 0; i < captchaLength; i++) {
                sb.append(chars.charAt(rand.nextInt(chars.length())));
            }
            text = sb.toString();
        }

        Font font = new Font(fontName, Font.BOLD, fontSize);

        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG = tempImage.createGraphics();
        tempG.setFont(font);
        FontMetrics fm = tempG.getFontMetrics();

        int totalTextWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            totalTextWidth += fm.charWidth(text.charAt(i));
        }
        totalTextWidth += charPadding * (text.length() - 1);

        int width = totalTextWidth + 2 * padding;

        tempG.dispose();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);

        Random rand = new Random();

        if (backgroundNoise) {
            int actualNoiseAmount = noiseAmount > 0 ? noiseAmount : (width * height / 30);
            for (int i = 0; i < actualNoiseAmount; i++) {
                int xNoise = rand.nextInt(width);
                int yNoise = rand.nextInt(height);
                Color noiseColor;
                if (backgroundRGBNoise) {
                    noiseColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
                } else {
                    noiseColor = rand.nextBoolean() ? Color.BLACK : Color.WHITE;
                }
                image.setRGB(xNoise, yNoise, noiseColor.getRGB());
            }
        }

        g.setFont(font);
        g.setColor(textColor);

        fm = g.getFontMetrics();

        int yBase = (height - (fm.getAscent() + fm.getDescent())) / 2 + fm.getAscent();

        int x = padding;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String letter = String.valueOf(c);

            int charWidth = fm.charWidth(c);

            AffineTransform original = g.getTransform();
            AffineTransform transform = new AffineTransform();

            transform.translate(x + charWidth / 2.0, yBase);

            if (rotationDistortion) {
                double rotation = (rand.nextDouble() * 2 - 1) * Math.toRadians(maxRotationDegree);
                transform.rotate(rotation);
            }

            if (shearDistortion) {
                double shearX = (rand.nextDouble() * 2 - 1) * maxShearFactor;
                double shearY = (rand.nextDouble() * 2 - 1) * maxShearFactor;
                transform.shear(shearX, shearY);
            }

            transform.translate(-charWidth / 2.0, 0);

            g.setTransform(transform);
            g.drawString(letter, 0, 0);
            g.setTransform(original);

            x += charWidth + charPadding;
        }

        if (lineDistortion) {
            for (int i = 0; i < numberOfLines; i++) {
                if (randomLineColor)
                    lineColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
                g.setColor(lineColor);
                int x1 = rand.nextInt(width);
                int y1 = rand.nextInt(height);
                int x2 = rand.nextInt(width);
                int y2 = rand.nextInt(height);
                g.drawLine(x1, y1, x2, y2);
            }
        }

        g.dispose();

        if (waveDistortion) {
            BufferedImage dest = new BufferedImage(width, height, image.getType());

            for (int currentY = 0; currentY < height; currentY++) {
                int xOffset = (int) (waveAmplitude * Math.sin(2 * Math.PI * currentY * waveFrequency));
                for (int currentX = 0; currentX < width; currentX++) {
                    int newX = currentX + xOffset;
                    if (newX >= 0 && newX < width) {
                        dest.setRGB(currentX, currentY, image.getRGB(newX, currentY));
                    } else {
                        dest.setRGB(currentX, currentY, backgroundColor.getRGB());
                    }
                }
            }
            image = dest;
        }

        return new Captcha(image, text);
    }
}
