package fr.supmap.supmapapi.utils;

import java.security.SecureRandom;

public class PasswordUtils {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "abcdefghijklmnopqrstuvwxyz"
            + "0123456789"
            + "!@#$%^&*()-_=+[]{}|;:,.<>?";
    private static final int DEFAULT_LENGTH = 16;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Génère un mot de passe alphanumérique aléatoire.
     *
     * @param length longueur souhaitée du mot de passe
     * @return mot de passe clair
     */
    public static String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(idx));
        }
        return sb.toString();
    }

    /**
     * version avec longueur par défaut (16)
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(DEFAULT_LENGTH);
    }
}
