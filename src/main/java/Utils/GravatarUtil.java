package Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GravatarUtil {
    public static String getGravatarUrl(String email, int size) { // cette méthode génère l'URL de l'avatar Gravatar en fonction de l'adresse e-mail et de la taille spécifiée
        if (email == null) {
            email = "";
        }
        String hash = md5Hex(email.trim().toLowerCase());
        return "https://www.gravatar.com/avatar/" + hash + "?s=" + size + "&d=identicon";
    }

    private static String md5Hex(String input) { // cette méthode convertit une chaîne de caractères en son équivalent MD5 hexadécimal
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}