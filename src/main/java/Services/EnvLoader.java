package Services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EnvLoader {
    private static Properties properties = null;

    static {
        loadEnv();
    }

    private static void loadEnv() {
        properties = new Properties();
        try {
            // Essayer de charger depuis .env
            FileInputStream fis = new FileInputStream(".env");
            properties.load(fis);
            fis.close();
            System.out.println("✅ Fichier .env chargé avec succès");
        } catch (IOException e) {
            System.out.println("⚠️ Fichier .env non trouvé, utilisation des variables d'environnement système");
            // Fallback: utiliser les variables d'environnement système
        }
    }

    public static String get(String key) {
        // Priorité: variables système > fichier .env
        String systemValue = System.getenv(key);
        if (systemValue != null && !systemValue.isEmpty()) {
            return systemValue;
        }

        if (properties != null) {
            return properties.getProperty(key, "");
        }

        return "";
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value.isEmpty() ? defaultValue : value;
    }

    public static boolean hasValidKey(String key) {
        String value = get(key);
        return value != null && !value.isEmpty() &&
                !value.contains("votre_cle") && !value.equals(key);
    }
}