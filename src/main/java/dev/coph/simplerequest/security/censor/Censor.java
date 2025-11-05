package dev.coph.simplerequest.security.censor;


/**
 * Utility class for text manipulation, primarily for censoring
 * and truncating strings.
 *
 * This class provides static methods to perform operations such
 * as truncating a string into a specific format, completely
 * redacting a string using a specified character, or partially
 * redacting a string while leaving a specific number of characters
 * untouched.
 *
 * The class cannot be instantiated as it is designed to provide
 * static methods only.
 */
public class Censor {

    /**
     * Private constructor to prevent instantiation of the Censor class.
     *
     * The Censor class is a utility class containing only static methods
     * for text manipulation. As such, it is not meant to be instantiated.
     */
    private Censor() {
    }

    /**
     * Truncates the input string to include only a specified
     * number of characters from the beginning and end of the string,
     * separated by an ellipsis ("...").
     *
     * This method provides a default truncation length of 5 characters.
     *
     * @param string the input string to be truncated
     * @return the truncated string in the format of
     * the first n characters, followed by "...", and then the last n characters
     */
    public static String truculence(String string) {
        return truculence(string, 5);
    }

    /**
     * Truncates the input string to include only a specified number
     * of characters from the beginning and end of the string, separated
     * by an ellipsis ("...").
     *
     * This method returns a formatted string where the first and last
     * specified number of characters are preserved, with the middle portion
     * replaced by "...".
     *
     * @param string the input string to be truncated
     * @param length the number of characters to preserve from both the
     *               beginning and end of the string
     * @return the truncated string in the format of the first n characters,
     *         followed by "...", and then the last n characters
     */
    public static String truculence(String string, int length) {
        return string.substring(0, length) + "..." + string.substring(string.length() - length);
    }

    /**
     * Redacts the input string entirely by replacing each character in the string
     * with the default censor character, which is a hash ("#").
     *
     * This method is a simple overload of {@link #redaction(String, String)}
     * and uses the default censor character.
     *
     * @param string the input string to be entirely redacted
     * @return the redacted string, with each character replaced by the default censor character
     */
    public static String redaction(String string) {
        return redaction(string, "#");
    }

    /**
     * Redacts the input string entirely by replacing each character in the string
     * with the specified censor character.
     *
     * Each character of the input string is replaced by the repeated value
     * of the provided `censorChar`.
     *
     * @param string the input string to be redacted
     * @param censorChar the character used to replace each character in the input string
     * @return the redacted string, with each character replaced by the specified censor character
     */
    public static String redaction(String string, String censorChar) {
        return censorChar.repeat(string.length());
    }

    /**
     * Partially redacts the input string by replacing all but a specified
     * number of trailing characters with the default censor character ("#").
     *
     * This method ensures that the last `remaining` characters of the
     * input string are preserved, and all preceding characters are replaced
     * by the default censor character.
     *
     * @param string the input string to be redacted
     * @param remaining the number of characters to preserve at the end of the string
     * @return the redacted string, where all but the last `remaining` characters
     *         are replaced by the default censor character
     */
    public static String redactionLite(String string, int remaining) {
        return redactionLite(string, "#", remaining);
    }

    /**
     * Partially redacts the input string by replacing all but a specified
     * number of trailing characters with a specified censor character.
     *
     * This method ensures that the last `remaining` characters of the
     * input string are preserved, and all preceding characters are replaced
     * by the specified `censorChar`.
     *
     * @param string the input string to be redacted
     * @param censorChar the character to use for redacting the portion of the string
     * @param remaining the number of characters to preserve at the end of the string
     * @return the redacted string, where all but the last `remaining` characters
     *         are replaced by the `censorChar`
     */
    public static String redactionLite(String string, String censorChar, int remaining) {
        return censorChar.repeat((string.length() < remaining ? 0 : string.length() - remaining)) + string.substring(string.length() - remaining);
    }
}
