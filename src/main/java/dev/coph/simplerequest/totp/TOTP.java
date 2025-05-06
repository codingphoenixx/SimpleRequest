package dev.coph.simplerequest.totp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

/**
 * A utility class for generating and validating Time-based One-Time Passwords (TOTP)
 * using the TOTP algorithm as specified in RFC 6238.
 *
 * This class provides static methods to generate or verify TOTP using a secret key
 * and the current time step.
 *
 * The core functionality includes:
 * - Generating a six-digit OTP based on a given secret key.
 * - Validating a provided OTP against the generated OTP for the current or previous time step.
 *
 * The implementation relies on HMAC-SHA1 for OTP generation and assumes a step size of 30 seconds.
 */
public final class TOTP {
	/**
	 * Private constructor for the TOTP class.
	 * This constructor is defined to restrict instantiation of the TOTP class,
	 * as it provides only static methods for TOTP generation and validation.
	 */
	private TOTP() {
	}

	/**
	 * Generates a one-time password (OTP) based on the given key using the TOTP (Time-based One-Time Password) algorithm.
	 *
	 * @param key the secret key used for generating the OTP. The key must be provided in hexadecimal format.
	 * @return a six-digit TOTP string valid for the current time step.
	 * @throws IllegalArgumentException if the step value is invalid during OTP generation.
	 */
	public static String getOTP(String key) {
		return TOTP.getOTP(TOTP.getStep(), key);
	}

	/**
	 * Validates a given one-time password (OTP) against a time-based one-time password (TOTP)
	 * generated using the provided key and the current time step.
	 *
	 * @param key the secret key used for generating the OTP. The key must be in hexadecimal format.
	 * @param otp the one-time password to validate.
	 * @return true if the provided OTP is valid for the current or previous time step, false otherwise.
	 */
	public static boolean validate(final String key, final String otp) {
		return TOTP.validate(TOTP.getStep(), key, otp);
	}

	/**
	 * Validates whether the given one-time password (OTP) corresponds to the current or previous
	 * time step based on the provided key and step value.
	 *
	 * @param step the time step used for OTP generation, typically derived from the current timestamp.
	 *             Must be greater than or equal to zero.
	 * @param key the secret key used for generating the OTP. The key must be provided in hexadecimal format.
	 * @param otp the one-time password to validate.
	 * @return true if the provided OTP matches the OTP for the current or previous time step;
	 *         false otherwise.
	 */
	private static boolean validate(final long step, final String key, final String otp) {
		return TOTP.getOTP(step, key).equals(otp) || (step > 0 && TOTP.getOTP(step - 1, key).equals(otp));
	}

	/**
	 * Computes the current time step for use in Time-based One-Time Password (TOTP) generation.
	 * The time step is determined by dividing the current system time in milliseconds by 30,000,
	 * corresponding to a 30-second interval.
	 *
	 * @return the current time step as a long value, representing the number of 30-second intervals
	 *         elapsed since the Unix epoch.
	 */
	private static long getStep() {
		// 30 seconds StepSize (ID TOTP)
		return System.currentTimeMillis() / 30000;
	}

	/**
	 * Generates a one-time password (OTP) based on a specific time step value and a secret key
	 * using the TOTP (Time-based One-Time Password) algorithm. The OTP is a six-digit code derived
	 * from the HMAC-SHA1 hash of the key and the step.
	 *
	 * @param step the time step used for OTP generation, typically derived from the current timestamp.
	 *             Must be greater than or equal to zero.
	 * @param key the secret key used for generating the OTP. The key must be provided in hexadecimal format.
	 * @return a six-digit TOTP string generated for the provided time step and key.
	 * @throws IllegalArgumentException if the step value is less than zero.
	 */
	private static String getOTP(final long step, final String key) {
		if (step < 0) {
			throw new IllegalArgumentException("Step must be greater than or equal to zero.");
		}
		String steps = Long.toHexString(step).toUpperCase();
		while (steps.length() < 16) {
			steps = "0" + steps;
		}

		final byte[] msg = TOTP.hexStr2Bytes(steps);
		final byte[] k = TOTP.hexStr2Bytes(key);

		final byte[] hash = TOTP.hmac_sha1(k, msg);

		final int offset = hash[hash.length - 1] & 0xf;
		final int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
		final int otp = binary % 1000000;

		String result = Integer.toString(otp);
		while (result.length() < 6) {
			result = "0" + result;
		}
		return result;
	}

	/**
	 * Converts a hexadecimal string into a byte array.
	 *
	 * @param hex the hexadecimal string to convert. Each character in the string must be a valid
	 *            hexadecimal digit (0-9, A-F, case-insensitive).
	 * @return a byte array representing the binary data derived from the hexadecimal string.
	 *         The first byte of the resulting array corresponds to the most significant hex digits.
	 */
	private static byte[] hexStr2Bytes(final String hex) {
		final byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
		final byte[] ret = new byte[bArray.length - 1];

		System.arraycopy(bArray, 1, ret, 0, ret.length);
		return ret;
	}

	/**
	 * Computes the HMAC-SHA1 hash of the given input text using the provided key.
	 *
	 * @param keyBytes the secret key used for hashing, provided as a byte array.
	 *                 This key is used to initialize the HMAC-SHA1 algorithm.
	 * @param text the input data to be hashed, provided as a byte array.
	 *             This data is processed using HMAC-SHA1 with the provided key.
	 * @return the resulting HMAC-SHA1 hash as a byte array.
	 *         This array represents the binary hash value computed from the key and input text.
	 * @throws UndeclaredThrowableException if a GeneralSecurityException occurs during the hash computation.
	 */
	private static byte[] hmac_sha1(final byte[] keyBytes, final byte[] text) {
		try {
			final Mac hmac = Mac.getInstance("HmacSHA1");
			final SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
			hmac.init(macKey);
			return hmac.doFinal(text);
		} catch (final GeneralSecurityException gse) {
			throw new UndeclaredThrowableException(gse);
		}
	}

}
