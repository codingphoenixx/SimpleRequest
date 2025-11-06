package dev.coph.simplerequest.security.jwt;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;

/**
 * Utility class for creating and validating JSON Web Tokens (JWTs) using the HS256 algorithm.
 * This class provides methods for generating signed JWT strings and validating their structure,
 * signature, and claims.
 * JWT payload claims can be customized and include standard claims such as "iat", "exp", and "nbf".
 * <p>
 * This class is designed for use with HMAC SHA-256 (HS256) signing and does not support other algorithms.
 */
public final class JWT {

    /**
     * A constant representing the algorithm used for signing the JWT.
     * <p>
     * This value is specifically set to "HS256", signifying the HMAC
     * algorithm with SHA-256 as the hash function. It is used in the
     * construction and validation of JWTs for ensuring message integrity
     * and authenticity.
     */
    public static final String ALG = "HS256";
    private static final String TYP = "JWT";
    private static final Base64.Encoder B64_URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_URL_DECODER =
            Base64.getUrlDecoder();

    private JWT() {
    }

    /**
     * Create a signed JWT string using HS256.
     *
     * @param secret        HMAC secret
     * @param claims        claims map. Standard claims can be provided here.
     *                      Values can be primitives, Maps/Lists, JSONObject/JSONArray.
     * @param issuedAtSecs  iat (seconds since epoch), or null to set automatically
     * @param expiresAtSecs exp (seconds since epoch), nullable
     * @param notBeforeSecs nbf (seconds since epoch), nullable
     * @return compact JWS string
     * @throws JwtException if an error occurs during the signing process.
     */
    public static String createToken(
            byte[] secret,
            Map<String, Object> claims,
            Long issuedAtSecs,
            Long expiresAtSecs,
            Long notBeforeSecs
    ) throws JwtException {
        Objects.requireNonNull(secret, "secret");
        Objects.requireNonNull(claims, "claims");

        JSONObject header = new JSONObject();
        header.put("alg", ALG);
        header.put("typ", TYP);

        JSONObject payload = new JSONObject(claims);
        long now = Instant.now().getEpochSecond();
        if (issuedAtSecs == null) issuedAtSecs = now;
        payload.put("iat", issuedAtSecs);
        if (expiresAtSecs != null) payload.put("exp", expiresAtSecs);
        if (notBeforeSecs != null) payload.put("nbf", notBeforeSecs);

        String headerB64 = base64UrlEncodeUtf8(header.toString());
        String payloadB64 = base64UrlEncodeUtf8(payload.toString());

        String signingInput = headerB64 + "." + payloadB64;
        String signatureB64 = signHS256ToBase64Url(secret, signingInput);

        return signingInput + "." + signatureB64;
    }

    /**
     * Validates a JSON Web Token (JWT) against a secret key and specified clock skew.
     *
     * The method checks the integrity and validity of the JWT by verifying its signature,
     * decoding its components, and ensuring that relevant claims such as "typ", "alg",
     * "nbf", and "exp" are correctly satisfied.
     *
     * @param secret   the secret key used for verifying the token's signature. Must not be null.
     * @param token    the compact JWS (JWT) string to be validated. Must not be null.
     * @param skewSecs the allowed clock skew in seconds for validating the "nbf" (Not Before)
     *                 and "exp" (Expiration) claims.
     * @return a map representing the decoded payload of the JWT, containing its claims as key-value pairs.
     * @throws JwtException if the token fails validation due to an invalid format, signature mismatch,
     *                      expired or not yet valid token, incorrect "typ" or "alg" headers,
     *                      or other processing issues (e.g., invalid JSON content).
     */
    public static Map<String, Object> validateToken(
            byte[] secret,
            String token,
            long skewSecs
    ) throws JwtException {
        Objects.requireNonNull(secret, "secret");
        Objects.requireNonNull(token, "token");

        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new JwtException("Invalid token format");
        }

        String headerB64 = parts[0];
        String payloadB64 = parts[1];
        String sigB64 = parts[2];

        String signingInput = headerB64 + "." + payloadB64;
        String expectedSigB64 = signHS256ToBase64Url(secret, signingInput);

        if (!constantTimeEq(sigB64, expectedSigB64)) {
            throw new JwtException("Invalid signature");
        }

        String headerJson = utf8DecodeBase64Url(headerB64);
        String payloadJson = utf8DecodeBase64Url(payloadB64);

        JSONObject header;
        JSONObject payload;
        try {
            header = new JSONObject(headerJson);
            payload = new JSONObject(payloadJson);
        } catch (org.json.JSONException e) {
            throw new JwtException("Invalid JSON", e);
        }

        String typ = header.optString("typ", null);
        if (!TYP.equals(typ)) {
            throw new JwtException("Invalid typ");
        }
        String alg = header.optString("alg", null);
        if (!ALG.equals(alg)) {
            throw new JwtException("Invalid alg");
        }

        long now = Instant.now().getEpochSecond();
        Long nbf = optLong(payload, "nbf");
        Long exp = optLong(payload, "exp");
        if (nbf != null && now + skewSecs < nbf) {
            throw new JwtException("Token not yet valid");
        }
        if (exp != null && now - skewSecs >= exp) {
            throw new JwtException("Token expired");
        }

        return jsonObjectToMap(payload);
    }

    private static String signHS256ToBase64Url(byte[] secret, String data)
            throws JwtException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return B64_URL_ENCODER.encodeToString(sig);
        } catch (GeneralSecurityException e) {
            throw new JwtException("HMAC failure", e);
        }
    }

    private static String base64UrlEncodeUtf8(String s) {
        return B64_URL_ENCODER.encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String utf8DecodeBase64Url(String b64) throws JwtException {
        try {
            byte[] bytes = B64_URL_DECODER.decode(b64);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Invalid base64url", e);
        }
    }

    private static boolean constantTimeEq(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.ISO_8859_1);
        byte[] y = b.getBytes(StandardCharsets.ISO_8859_1);
        if (x.length != y.length) return false;
        int r = 0;
        for (int i = 0; i < x.length; i++) {
            r |= (x[i] ^ y[i]);
        }
        return r == 0;
    }

    private static Long optLong(JSONObject obj, String key) {
        if (!obj.has(key) || obj.isNull(key)) return null;
        try {
            Object v = obj.get(key);
            if (v instanceof Number) {
                return ((Number) v).longValue();
            }
            if (v instanceof String) {
                return Long.parseLong((String) v);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    private static Map<String, Object> jsonObjectToMap(JSONObject json) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : json.keySet()) {
            Object v = json.get(key);
            map.put(key, jsonToJava(v));
        }
        return map;
    }

    private static Object jsonToJava(Object v) {
        if (v == JSONObject.NULL) return null;
        if (v instanceof JSONObject) {
            return jsonObjectToMap((JSONObject) v);
        }
        if (v instanceof JSONArray arr) {
            List<Object> list = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                list.add(jsonToJava(arr.get(i)));
            }
            return list;
        }
        return v;
    }


    /**
     * Creates and returns a new instance of the Builder class for constructing
     * a JWT with configurable claims, timestamps, and signing options.
     *
     * @return a new instance of the Builder for constructing and signing a JWT.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Provides a builder for constructing JSON Web Tokens (JWT) with configurable claims,
     * timestamps, and signing options. Use the Builder to set standard registered claims
     * (e.g., issuer, subject, audience) or custom claims, along with timestamps such as
     * issued at, expiration, and not-before.
     */
    public static final class Builder {
        /**
         * Constructs a new instance of the Builder class. This class is used to
         * create and configure a JSON Web Token (JWT) with various claims, timestamps, and
         * signing options. The Builder provides a fluent API for setting standard and
         * custom claims and generating a signed JWT.
         */
        public Builder() {}
        private final Map<String, Object> claims = new LinkedHashMap<>();
        private Long iat, exp, nbf;

        /**
         * Sets the issuer claim ("iss") for the JSON Web Token (JWT). The "iss" claim identifies the
         * principal that issued the JWT.
         *
         * @param iss the value to set for the issuer claim. Typically, this represents the name or
         *            identifier of the entity issuing the token.
         * @return the current instance of the Builder to allow method chaining.
         */
        public Builder issuer(String iss) {
            claims.put("iss", iss);
            return this;
        }

        /**
         * Sets the subject claim ("sub") for the JSON Web Token (JWT). The "sub" claim identifies
         * the principal that is the subject of the JWT.
         *
         * @param sub the value to set for the subject claim. Typically, this represents a unique
         *            identifier for the subject, such as a user ID.
         * @return the current instance of the Builder to allow method chaining.
         */
        public Builder subject(String sub) {
            claims.put("sub", sub);
            return this;
        }

        /**
         * Sets the audience claim ("aud") for the JSON Web Token (JWT). The "aud" claim identifies
         * the recipients that the token is intended for.
         *
         * @param aud the value to set for the audience claim. Typically, this represents one or more
         *            recipients or systems that the token is targeting.
         * @return the current instance of the Builder to allow method chaining.
         */
        public Builder audience(String aud) {
            claims.put("aud", aud);
            return this;
        }

        /**
         * Sets the JWT ID claim ("jti") for the JSON Web Token (JWT).
         * The "jti" claim provides a unique identifier for the JWT,
         * which can be used to prevent replay attacks or identify individual tokens.
         *
         * @param jti the value to set for the JWT ID claim. Typically, this is a unique string identifier.
         * @return the current instance of the Builder to allow method chaining.
         */
        public Builder jwtId(String jti) {
            claims.put("jti", jti);
            return this;
        }

        /**
         * Adds or updates a custom claim in the JWT claims set. Claims are key-value pairs
         * that can include standard or application-specific data to be included in the token.
         *
         * @param k the key of the claim. This is typically a string that represents the claim name.
         * @param v the value of the claim. This can be a primitive, a collection, or a complex object.
         * @return the current instance of the Builder to allow method chaining.
         */
        public Builder claim(String k, Object v) {
            claims.put(k, v);
            return this;
        }

        /**
         * Sets the "issued at" claim ("iat") for the JSON Web Token (JWT). The "iat" claim represents the time
         * at which the token was issued, expressed as the number of seconds since the epoch (Unix timestamp).
         *
         * @param seconds the timestamp, in seconds since the epoch, indicating when the token was issued.
         * @return the current instance of the Builder to allow method chaining.
         */
        public Builder issuedAt(long seconds) {
            this.iat = seconds;
            return this;
        }

        /**
         * Sets the expiration time ("exp") for the JSON Web Token (JWT).
         * The expiration time specifies the timestamp, in seconds since the epoch,
         * at which the token will expire and should no longer be accepted.
         *
         * @param seconds the timestamp, in seconds since the epoch, representing when the token expires.
         * @return the current instance of the Builder to allow method chaining.
         */
        public Builder expiresAt(long seconds) {
            this.exp = seconds;
            return this;
        }

        /**
         * Sets the "not before" claim ("nbf") for the JSON Web Token (JWT).
         * The "nbf" claim specifies the time before which the token must not be accepted for processing.
         * The value has been expressed as seconds since the epoch (Unix timestamp).
         *
         * @param seconds the timestamp, in seconds since the epoch, indicating the time before which
         *                the token is not considered valid.
         * @return the current instance of the Builder to allow method chaining.
         */
        public Builder notBefore(long seconds) {
            this.nbf = seconds;
            return this;
        }

        /**
         * Signs a JSON Web Token (JWT) using the specified secret and configured claims,
         * timestamps, and signing options.
         *
         * @param secret the secret key used for signing the token. Must not be null.
         * @return the signed JWT as a compact JWS string.
         * @throws JwtException if an error occurs during the signing process.
         */
        public String sign(byte[] secret) throws JwtException {
            return createToken(secret, claims, iat, exp, nbf);
        }
    }
}
