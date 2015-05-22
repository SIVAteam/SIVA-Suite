package hu.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * This class provides utility functions for security related tasks, such as
 * hash functions and generation of random data.
 */
public class SecurityUtils {
    private static final String CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private SecurityUtils() {
    }

    /**
     * Generate a hash-value from a {@link String}.
     * 
     * @param input
     *            data to hash.
     * @return a hash-value generated from the given input as a {@link String}
     *         in hexadecimal representation.
     * @throws NoSuchAlgorithmException
     * 
     */
    public static String hash(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ignored) {
        }
        md.update(input.getBytes());

        byte[] byteHash = md.digest();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < byteHash.length; i++) {
            sb.append(Integer.toString((byteHash[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }

        return sb.toString();
    }

    /**
     * Generate a random, alphanumeric {@link String}.
     * 
     * @param length
     *            of the random {@link String} to generate.
     * @return a random {@link String} with the given length.
     * 
     */
    public static String randomString(int length) {
        Random random = new Random();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}