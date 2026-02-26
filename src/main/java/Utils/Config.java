package Utils;

import java.io.*;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                System.out.println("✅ Fichier config.properties chargé avec succès");
            } else {
                System.err.println("⚠️ Fichier config.properties non trouvé!");
                // Créer le fichier s'il n'existe pas
                createDefaultConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig() {
        try (OutputStream output = new FileOutputStream("src/" + CONFIG_FILE)) {
            // Propriétés pour l'API Gemini
            properties.setProperty("gemini.api.key", "VOTRE_CLE_API_ICI");
            properties.setProperty("gemini.api.url", "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent");

            // Propriétés pour Sightengine (modération)
            properties.setProperty("sightengine.api.user", "VOTRE_API_USER");
            properties.setProperty("sightengine.api.secret", "VOTRE_API_SECRET");
            properties.setProperty("sightengine.api.url", "https://api.sightengine.com/1.0/check.json");

            properties.store(output, null);
            System.out.println("✅ Fichier config.properties créé avec les valeurs par défaut");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
    public static void main(String[] args) {
        System.out.println("🔍 Test de lecture des configurations :");
        System.out.println("Gemini Key: " + get("gemini.api.key"));
        System.out.println("Sightengine User: " + get("sightengine.api.user"));
        System.out.println("Sightengine Secret: " + get("sightengine.api.secret"));
    }
}