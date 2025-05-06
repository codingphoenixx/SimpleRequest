package dev.coph.simplerequest.security.captcha;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class CaptchaGenerator {
    public static class Captcha {
        private final BufferedImage image;
        private final String text;

        public Captcha(BufferedImage image, String text) {
            this.image = image;
            this.text = text;
        }

        public BufferedImage image() {
            return image;
        }

        public String text() {
            return text;
        }
    }

    private String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";
    private int height = 50;
    private int padding = 10;
    private int charPadding = 0;
    private Color textColor = Color.BLACK;
    private String fixedText = null;
    private int captchaLength = 6;
    private final String fontName = "Arial";
    private final int fontStyle = Font.BOLD;
    private int fontSize = 40;


    private boolean rotationDistortion = false;
    private double maxRotationDegree = 15.0;

    private boolean shearDistortion = false;
    private double maxShearFactor = 0.15;

    private boolean backgroundNoise = false;
    private boolean backgroundRGBNoise = false;
    private int noiseAmount = 500;
    private Color backgroundColor = Color.WHITE;

    private boolean lineDistortion = false;
    private int numberOfLines = 10;
    private boolean randomLineColor = false;
    private Color lineColor = Color.GRAY;

    private boolean waveDistortion = false;
    private final double waveFrequency = 0.1;
    private final double waveAmplitude = 2.0;

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
