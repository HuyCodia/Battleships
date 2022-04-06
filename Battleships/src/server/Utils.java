package server;

import java.util.Random;

/**
 * @author Lennard Boetcher
 * Small utils class for the Server
 */
public class Utils {

    /**
     * Generate a random String of Characters (a-Z 0-9) with a given Length
     * @param length of the output String
     * @return String of random Characters
     */
    public static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new Random();
        String out = "";
        for (int i = 0; i < length; i++) {
            out += chars.charAt(r.nextInt(chars.length()));
        }
        return out;
    }

}
