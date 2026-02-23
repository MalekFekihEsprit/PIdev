package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AIService {

    // VOTRE CLÉ API GEMINI
    private static final String API_KEY = "AIzaSyBvrxclC-nJcx3-Qp3JTbuCU9ozpOFrf-s";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    /**
     * Génère une description attrayante et professionnelle pour une activité
     */
    public static String genererDescriptionAttrayante(String nom, String niveauDifficulte, String lieu) throws IOException {

        // Prompt amélioré pour des descriptions plus attrayantes
        String prompt = String.format(
                "Tu es un rédacteur professionnel pour une agence de voyages. " +
                        "Génère UNE SEULE description courte (entre 100 et 200 caractères) pour une activité touristique. " +
                        "La description doit être :\n" +
                        "- Attrayante et enthousiasmante (utilise des émojis avec parcimonie)\n" +
                        "- Professionnelle mais chaleureuse\n" +
                        "- Donner envie de participer\n" +
                        "- Mentionner les sensations, l'ambiance, ce qui rend l'expérience unique\n\n" +
                        "Activité : %s\n" +
                        "Lieu : %s\n" +
                        "Niveau de difficulté : %s\n\n" +
                        "Réponds UNIQUEMENT avec la description, sans phrase d'introduction ni explication. " +
                        "Exemple de style : '✨ Vivez une expérience magique au cœur de la médina ! Cette balade nocturne vous fera découvrir les secrets de Tunis sous un angle unique...'",
                nom, lieu, niveauDifficulte
        );

        // Création de la requête JSON
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

        // Configuration pour des résultats plus créatifs
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.9);
        generationConfig.addProperty("maxOutputTokens", 200);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("topK", 40);
        requestBody.add("generationConfig", generationConfig);

        // Envoi de la requête
        URL url = new URL(API_URL);
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

                // Parser la réponse
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                String description = jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();

                return description.trim()
                        .replaceAll("^[\"']|[\"']$", "")
                        .replaceAll("\\s+", " ");
            }
        } else {
            // Lire l'erreur pour debug
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line);
                }
                throw new IOException("Erreur API " + responseCode + ": " + error.toString());
            }
        }
    }

    /**
     * Version simplifiée avec fallback
     */
    public static String genererDescriptionSimple(String nom, String niveauDifficulte, String lieu) {
        try {
            return genererDescriptionAttrayante(nom, niveauDifficulte, lieu);
        } catch (Exception e) {
            e.printStackTrace();
            return String.format(
                    "✨ Découvrez %s à %s ! Une activité de niveau %s qui vous promet des moments inoubliables et des sensations uniques. Parfait pour les aventuriers !",
                    nom, lieu, niveauDifficulte.toLowerCase()
            );
        }
    }

    /**
     * Teste la connexion à l'API
     */
    public static boolean testConnection() {
        try {
            String result = genererDescriptionAttrayante("Test", "Facile", "Paris");
            System.out.println("✅ Test API réussi: " + result.substring(0, Math.min(50, result.length())) + "...");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Test API échoué: " + e.getMessage());
            return false;
        }
    }

    /**
     * Méthode principale pour tester directement
     */
    public static void main(String[] args) {
        System.out.println("Test de connexion à l'API Gemini...");
        boolean connected = testConnection();
        System.out.println("Statut: " + (connected ? "Connecté ✅" : "Non connecté ❌"));

        if (connected) {
            try {
                String description = genererDescriptionAttrayante("Soirée Halloween", "Facile", "La Marsa");
                System.out.println("\nDescription générée:");
                System.out.println(description);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}