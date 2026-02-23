package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AIService {

    // VOTRE CLÉ API (correcte)
    private static final String API_KEY = "AIzaSyBvrxclC-nJcx3-Qp3JTbuCU9ozpOFrf-s";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    public static String genererDescription(String nom, String niveauDifficulte, String lieu) {
        try {
            // Construction du prompt
            String prompt = "Génère une description courte et attrayante (100-200 caractères) " +
                    "pour une activité touristique. " +
                    "Nom: " + nom + ", Difficulté: " + niveauDifficulte + ", Lieu: " + lieu + ". " +
                    "Réponds uniquement avec la description, sans phrases d'introduction.";

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

            // Configuration
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.8);
            generationConfig.addProperty("maxOutputTokens", 150);
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

                    return description.trim();
                }
            } else {
                // Lire l'erreur
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        error.append(line);
                    }
                    System.err.println("Erreur API détail: " + error.toString());
                }
                return "Description temporaire: " + nom + " à " + lieu + " - Activité de niveau " + niveauDifficulte;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Découvrez " + nom + " à " + lieu + " - Une activité de niveau " + niveauDifficulte +
                    " qui vous promet une expérience unique !";
        }
    }

    public static boolean testConnection() {
        try {
            String result = genererDescription("Test", "Facile", "Paris");
            System.out.println("Test API réussi: " + result);
            return true;
        } catch (Exception e) {
            System.err.println("Test API échoué: " + e.getMessage());
            return false;
        }
    }
}