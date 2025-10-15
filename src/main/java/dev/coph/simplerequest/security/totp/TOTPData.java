package dev.coph.simplerequest.security.totp;


import dev.coph.simplerequest.util.Base32;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Provides a data model for a Time-based One-Time Password (TOTP) configuration.
 * This class encapsulates the necessary fields and functions to work with TOTP
 * codes, including an issuer, user, and a shared secret used for generating OTPs.
 * It also provides utility methods for encoding the shared secret in HEX and Base32
 * formats and forms URLs and serials needed for OTP provisioning.
 * <p>
 * Instances of this class are immutable after creation.
 *
 * @param issuer Represents the identifier for the organization or service issuing the Time-Based One-Time Password (TOTP).
 *               <p>
 *               This variable typically holds a human-readable string referencing the entity responsible
 *               for generating and managing TOTP authentication. The issuer helps differentiate between
 *               multiple accounts or services using two-factor authentication when displayed in TOTP clients.
 *               <p>
 *               The issuer is immutable and is initialized during the creation of the {@code TOTPData} instance.
 * @param user   Represents the username or account identifier for which the TOTP data is associated.
 *               This field is immutable once set and identifies the user within the TOTP context.
 *               <p>
 *               Typically used in conjunction with the 'issuer' to generate a TOTP URL or other authentication-related data.
 *               This value is expected to be unique per user or account within a given issuer's scope.
 * @param secret A byte array storing the secret key used for generating Time-based One-Time Passwords (TOTP).
 *               This secret is used in conjunction with the TOTP algorithm to derive one-time passwords
 *               or validate existing ones.
 *               <p>
 *               The secret should be securely generated and stored, as it forms the basis of TOTP authentication.
 *               It is typically represented as a cryptographically secure random key, which can be encoded in
 *               hexadecimal, Base32, or URL-safe string formats for representation or transmission.
 */
public record TOTPData(String issuer, String user, byte[] secret) {
    /**
     * A static array of characters representing the hexadecimal digits (0-9 and A-F).
     * This array is used internally to facilitate conversion of binary data to
     * hexadecimal string representations within the TOTPData class.
     */
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * A shared random number generator instance used for generating cryptographic
     * values such as the secret key within the TOTPData class.
     * <p>
     * This instance is utilized to produce an array of random bytes through
     * methods like `Random.nextBytes(byte[])`. The generated values help ensure
     * unpredictability and uniqueness of the TOTP secret keys.
     * <p>
     * As a `static final` field, this ensures a single shared Random instance
     * within the TOTPData class, optimizing resource use and thread safety when
     * generating random data.
     */
    private static final Random rnd = new Random();

    /**
     * Constructs a new TOTPData instance with the specified issuer, user, and secret key.
     *
     * @param issuer the identifier for the organization or service to which this TOTP data belongs.
     * @param user   the username or account identifier associated with this TOTP data.
     * @param secret the secret key used for generating time-based one-time passwords (TOTP),
     *               provided as a byte array.
     */
    public TOTPData {
    }

    /**
     * Constructs a new TOTPData instance with the specified secret key.
     *
     * @param secret the secret key used for generating time-based one-time passwords (TOTP),
     *               provided as a byte array.
     */
    public TOTPData(byte[] secret) {
        this(null, null, secret);
    }

    /**
     * Creates and returns a new instance of TOTPData. The TOTPData instance
     * is initialized with a newly generated secret key.
     *
     * @return a newly created TOTPData object with a generated secret key.
     */
    public static TOTPData create() {
        return new TOTPData(TOTPData.createSecret());
    }

    /**
     * Creates a new instance of TOTPData with the provided issuer and user,
     * and generates a new secret key for the instance.
     *
     * @param issuer the identifier for the organization or service to which this TOTP data belongs.
     * @param user   the username or account identifier associated with this TOTP data.
     * @return a new TOTPData object with the specified issuer, user, and a newly generated secret key.
     */
    public static TOTPData create(String issuer, String user) {
        return new TOTPData(issuer, user, TOTPData.createSecret());
    }

    /**
     * Generates a new secret key for use in TOTP (Time-based One-Time Password) authentication.
     * This method creates a random sequence of bytes with a fixed length of 20.
     *
     * @return a byte array containing the generated secret key.
     */
    public static byte[] createSecret() {
        byte[] secret = new byte[20];
        TOTPData.rnd.nextBytes(secret);
        return secret;
    }

    /**
     * Returns the identifier for the organization or service associated with this TOTP data.
     *
     * @return the issuer name as a string.
     */
    @Override
    public String issuer() {
        return this.issuer;
    }

    /**
     * Returns the username or account identifier associated with this TOTP data.
     *
     * @return the user name as a string.
     */
    @Override
    public String user() {
        return this.user;
    }

    /**
     * Retrieves the secret key associated with this TOTPData instance.
     *
     * @return the secret key as a byte array.
     */
    @Override
    public byte[] secret() {
        return this.secret;
    }

    /**
     * Converts the secret key of this TOTPData instance into a hexadecimal string representation.
     *
     * @return a string containing the hexadecimal representation of the secret key.
     */
    public String getSecretAsHex() {
        char[] hexChars = new char[this.secret.length * 2];
        for (int j = 0; j < this.secret.length; j++) {
            int v = this.secret[j] & 0xFF;
            hexChars[j * 2] = TOTPData.hexArray[v >>> 4];
            hexChars[(j * 2) + 1] = TOTPData.hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Converts the secret key of this TOTPData instance into a Base32-encoded string.
     * If an encoding error occurs, an empty string is returned.
     *
     * @return a string containing the Base32-encoded representation of the secret key,
     * or an empty string if encoding fails.
     */
    public String getSecretAsBase32() {
        try {
            return Base32.encode(this.secret);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Generates and returns a URL string representing the TOTP configuration for the given issuer, user, and secret key.
     * The URL follows the "otpauth://" format and can be used to configure TOTP applications.
     *
     * @return a string representation of the TOTP configuration URL.
     */
    public String getUrl() {
        String secretString = this.getSecretAsBase32();
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", this.issuer, this.user, secretString, this.issuer);
    }

    /**
     * Generates and returns a TOTP serial string in the format "otpauth://totp/{issuer}:{user}".
     * This string is used as part of the TOTP authentication process, uniquely identifying
     * the issuer and user within the TOTP configuration.
     *
     * @return a formatted string containing the TOTP serial such as "otpauth://totp/{issuer}:{user}".
     */
    public String getSerial() {
        return String.format("otpauth://totp/%s:%s", this.issuer, this.user);
    }
}
