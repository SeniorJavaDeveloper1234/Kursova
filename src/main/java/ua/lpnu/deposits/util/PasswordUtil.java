package ua.lpnu.deposits.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing passwords with SHA-256.
 * Uses Java's built-in {@link MessageDigest} — no external libraries required.
 */
public final class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Returns the SHA-256 hex digest of the given plain-text password.
     *
     * @param password plain-text password (not null)
     * @return 64-character lowercase hex string
     */
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Checks whether {@code plainText} matches the stored {@code hash}.
     *
     * @param plainText      password entered by the user
     * @param storedHash     SHA-256 digest stored in the database
     * @return {@code true} if the hashes match
     */
    public static boolean verify(String plainText, String storedHash) {
        return hash(plainText).equals(storedHash);
    }
}
