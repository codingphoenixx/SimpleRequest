package dev.coph.simplerequest.util;

import java.io.UnsupportedEncodingException;

/**
 * Utility class for encoding and decoding Base32 data. Base32 is a binary-to-text encoding
 * scheme that represents binary data in an ASCII string format using a restricted set
 * of 32 characters.
 *
 * This class provides methods for encoding byte arrays into Base32 strings, decoding
 * Base32 strings back into byte arrays, and modified decoding to handle specific conversions
 * for characters.
 */
public class Base32 {

    /**
     * The `base32Chars` variable defines the character set used for Base32 encoding. It consists
     * of 32 unique characters: the uppercase English alphabet letters (A-Z) and the digits 2-7.
     *
     * This character set is used to represent binary data in a Base32 string format and follows
     * the standard Base32 encoding rules defined in RFC 4648.
     *
     * Each character in this set corresponds to a 5-bit sequence in the encoded data.
     */
    private static final String base32Chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * Lookup table for decoding Base32 encoded characters.
     * This array maps ASCII character values to their corresponding Base32 value or a sentinel value (0xFF)
     * for unsupported characters.
     *
     * Each valid Base32 character is represented by a value in the range of 0x00 to 0x1F,
     * while unsupported characters are represented by 0xFF.
     *
     * This table is used in decoding operations to efficiently convert a Base32 character
     * to its corresponding 5-bit binary value.
     */
    private static final int[] base32Lookup =
            {0xFF, 0xFF, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
                    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
                    0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                    0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E,
                    0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16,
                    0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
                    0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                    0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E,
                    0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16,
                    0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
            };

    /**
     * Encodes the given byte array to a lower-cased Base32-encoded byte array
     * in US-ASCII character set.
     *
     * The method uses the {@link #encodeOriginal(byte[])} method to generate an
     * intermediate Base32-encoded string, converts it to lowercase, and then
     * encodes it into bytes using the US-ASCII encoding.
     *
     * @param data the byte array to be encoded
     * @return the resulting byte array representing the encoded string in US-ASCII
     * @throws UnsupportedEncodingException if the US-ASCII character encoding is not supported*/
    public static byte[] encodeBytes(byte[] data) throws UnsupportedEncodingException {
        String lower = encodeOriginal(data).toLowerCase();
        return lower.getBytes("US-ASCII");
    }

    /**
     * Encodes the given byte array to a Base32-encoded string in lowercase.
     * The method generates an intermediate Base32 representation using the
     * {@code encodeOriginal} method, converts the result to lowercase, and returns it.
     *
     * @param data the byte array to be encoded
     * @return a string containing the lowercase Base32-encoded representation of the input byte array
     * @throws UnsupportedEncodingException if there is an error related to character encoding
     */
    public static String encode(byte[] data) throws UnsupportedEncodingException {
        return encodeOriginal(data).toLowerCase();
    }


    /**
     * Decodes a Base32-encoded string into its byte array representation.
     * This method modifies the input string by replacing all occurrences of '8' with 'L'
     * and '9' with 'O' before decoding it using the {@code decode} method.
     *
     * @param data the Base32-encoded string to be decoded, potentially containing characters
     *             '8' and '9' to be replaced with 'L' and 'O', respectively
     * @return the decoded byte array representation of the input string
     */
    public static byte[] decodeModified(String data) {
        return decode(data.replace('8', 'L').replace('9', 'O'));
    }

    /**
     * Encodes the given byte array into a Base32-encoded string.
     * This method uses the Base32 encoding scheme, where each 5-bit group of the
     * input byte array is mapped to a character from a predefined Base32 alphabet.
     *
     * @param bytes the byte array to be encoded
     * @return a string containing the Base32-encoded representation of the input byte array
     */
    public static String encodeOriginal(final byte[] bytes) {
        int i = 0, index = 0, digit = 0;
        int currByte, nextByte;
        StringBuffer base32 = new StringBuffer((bytes.length + 7) * 8 / 5);

        while (i < bytes.length) {
            currByte = (bytes[i] >= 0) ? bytes[i] : (bytes[i] + 256); // unsign

            if (index > 3) {
                if ((i + 1) < bytes.length) {
                    nextByte =
                            (bytes[i + 1] >= 0) ? bytes[i + 1] : (bytes[i + 1] + 256);
                } else {
                    nextByte = 0;
                }

                digit = currByte & (0xFF >> index);
                index = (index + 5) % 8;
                digit <<= index;
                digit |= nextByte >> (8 - index);
                i++;
            } else {
                digit = (currByte >> (8 - (index + 5))) & 0x1F;
                index = (index + 5) % 8;
                if (index == 0)
                    i++;
            }
            base32.append(base32Chars.charAt(digit));
        }

        return base32.toString();
    }

    /**
     * Decodes a Base32-encoded string into its corresponding byte array representation.
     * The method processes each character of the input string, converts it using a predefined
     * Base32 lookup table, and assembles the decoded bytes iteratively based on Base32 decoding rules.
     *
     * Invalid characters in the input string (those not part of the Base32 alphabet or those
     * corresponding to an invalid lookup) are ignored during the decoding process.
     *
     * @param base32 the Base32-encoded string to be decoded
     * @return the resulting byte array representation of the decoded string
     */
    public static byte[] decode(final String base32) {
        int i, index, lookup, offset, digit;
        byte[] bytes = new byte[base32.length() * 5 / 8];

        for (i = 0, index = 0, offset = 0; i < base32.length(); i++) {
            lookup = base32.charAt(i) - '0';

            if (lookup < 0 || lookup >= base32Lookup.length) {
                continue;
            }

            digit = base32Lookup[lookup];

            if (digit == 0xFF) {
                continue;
            }

            if (index <= 3) {
                index = (index + 5) % 8;
                if (index == 0) {
                    bytes[offset] |= digit;
                    offset++;
                    if (offset >= bytes.length)
                        break;
                } else {
                    bytes[offset] |= digit << (8 - index);
                }
            } else {
                index = (index + 5) % 8;
                bytes[offset] |= (digit >>> index);
                offset++;

                if (offset >= bytes.length) {
                    break;
                }
                bytes[offset] |= digit << (8 - index);
            }
        }
        return bytes;
    }
}
