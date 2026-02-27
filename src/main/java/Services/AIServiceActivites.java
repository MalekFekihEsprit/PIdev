package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import Utils.Config;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AIServiceActivites {

    private static final String API_KEY = Config.get("gemini.api.key");
    // Liste des modèles à essayer en cas d'échec
    private static final String[] MODELES = {
            "gemini-1.5-flash",
            "gemini-1.5-pro",
            "gemini-1.0-pro"
    };

    private static int modeleActuel = 0;

    public static String genererDescriptionAttrayante(String nom, String niveauDifficulte, String lieu) {
        // Vérifier que la clé API est configurée
        if (API_KEY == null || API_KEY.isEmpty() || API_KEY.equals("VOTRE_CLE_API_ICI")) {
            System.out.println("⚠️ Clé API non configurée, utilisation du mode fallback");
            return getFallbackDescription(nom, niveauDifficulte, lieu);
        }

        // Essayer chaque modèle jusqu'à ce qu'un fonctionne
        for (int i = 0; i < MODELES.length; i++) {
            try {
                String modele = MODELES[i];
                String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + modele + ":generateContent?key=" + API_KEY;

                String description = appelerAPI(apiUrl, nom, niveauDifficulte, lieu);
                if (description != null && !description.isEmpty()) {
                    System.out.println("✅ Modèle utilisé: " + modele);
                    return description;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Modèle " + MODELES[i] + " échoué, essai du suivant...");
            }
        }

        // Si tous les modèles échouent
        System.out.println("⚠️ Tous les modèles API ont échoué, utilisation du mode fallback");
        return getFallbackDescription(nom, niveauDifficulte, lieu);
    }

    private static String appelerAPI(String apiUrl, String nom, String niveauDifficulte, String lieu) throws IOException {
        String prompt = String.format(
                "Tu es un rédacteur professionnel pour une agence de voyages. " +
                        "Génère UNE SEULE description courte (entre 100 et 200 caractères) pour une activité touristique. " +
                        "La description doit être attrayante, enthousiasmante et donner envie de participer.\n\n" +
                        "Activité : %s\n" +
                        "Lieu : %s\n" +
                        "Niveau de difficulté : %s\n\n" +
                        "Réponds UNIQUEMENT avec la description.",
                nom, lieu, niveauDifficulte
        );

        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.8);
        generationConfig.addProperty("maxOutputTokens", 200);
        requestBody.add("generationConfig", generationConfig);

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                String description = jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();

                return description.trim();
            }
        }
        return null;
    }

    private static String getFallbackDescription(String nom, String niveauDifficulte, String lieu) {
        String[] templates = {
                "✨ Préparez-vous pour une aventure inoubliable ! %s vous attend à %s. Une activité de niveau %s qui vous fera vibrer !",
                "🌟 Vivez une expérience unique avec %s à %s ! Cette activité de niveau %s saura vous séduire.",
                "🎉 Découvrez %s comme vous ne l'avez jamais vu à %s ! Activité de niveau %s, moments de pur bonheur !"
        };
        java.util.Random rand = new java.util.Random();
        String template = templates[rand.nextInt(templates.length)];
        return String.format(template, nom, lieu, niveauDifficulte.toLowerCase());
    }

    public static boolean testConnection() {
        try {
            String result = genererDescriptionAttrayante("Test", "Facile", "Paris");
            System.out.println("✅ Test: " + result);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Test échoué: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Test de l'API Gemini...");
        boolean connected = testConnection();
        System.out.println("Statut: " + (connected ? "Connecté ✅" : "Non connecté ❌ (mode fallback actif)"));
    }
}