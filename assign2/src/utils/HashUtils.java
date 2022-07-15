package utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    private static byte[] getHash(String input) throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    private static String hashtoString(byte[] hash)
    {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    public static String generateHash(String info) {
        byte[] bytes = new byte[0];
        try {
            bytes = getHash(info);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Could not find algorithm.");
            e.printStackTrace();
        }
        return hashtoString(bytes);
    }
}
