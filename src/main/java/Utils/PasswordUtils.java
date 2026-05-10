package Utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    private static final int BCRYPT_COST = 12;

    private PasswordUtils() {
    }

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }

        String compatibleHash = hashedPassword;

        if (compatibleHash.startsWith("$2y$")) {
            compatibleHash = "$2a$" + compatibleHash.substring(4);
        }

        return BCrypt.checkpw(plainPassword, compatibleHash);
    }
}