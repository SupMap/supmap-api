package fr.supmap.supmapapi.services;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * The type Password manager.
 */
public class PasswordManager {

    /**
     * Hash password.
     *
     * @param password the password
     * @return the password hash
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    /**
     * Verify password.
     *
     * @param password   the password
     * @param storedHash the stored hash
     * @return if the password is correct
     */
    public static boolean verifyPassword(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }
}
