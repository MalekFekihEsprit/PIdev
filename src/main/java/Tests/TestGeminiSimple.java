package Tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TestGeminiSimple {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("=== TEST GEMINI 2.5 SIMPLE ===\n");

        // 1. Charger la clé
        String apiKey = loadApiKey();
        if (apiKey == null) {
            System.out.println("❌ Clé API non trouvée");
            return;
        }
        System.out.println("✅ Clé chargée: " + maskKey(apiKey));

        // 2. Tester chaque modèle disponible
        String[] models = {
                "gemini-2.5-flash",
                "gemini-2.5-pro",
                "gemini-2.0-flash",
                "gemini-2.0-flash-lite",
                "gemini-2.5-flash-lite"
        };

        for (String model : models) {
            testModel(apiKey, model);
        }

        // 3. Test avec le format exact de votre application
        System.out.println("\n=== TEST AVEC FORMAT APPLICATION ===");
        testApplicationFormat(apiKey, "gemini-2.5-flash");
    }

    private static String loadApiKey() {
        try {
            // Variable d'environnement
            String envKey = System.getenv("AI_AGENT");
            if (envKey != null && !envKey.isBlank()) {
                return envKey;
            }

            // Fichier .env
            var path = Paths.get(".env");
            if (Files.exists(path)) {
                var lines = Files.readAllLines(path);
                for (String line : lines) {
                    if (line.startsWith("AI_AGENT=")) {
                        return line.substring("AI_AGENT=".length()).trim();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return null;
    }

    private static String maskKey(String key) {
        if (key == null || key.length() < 10) return "invalide";
        return key.substring(0, 6) + "..." + key.substring(key.length() - 4);
    }

    private static void testModel(String apiKey, String model) {
        System.out.println("\n--- Test modèle: " + model + " ---");

        String url = "https://generativelanguage.googleapis.com/v1/models/"
                + model + ":generateContent?key=" + apiKey;

        try {
            // Prompt très simple
            String prompt = "Réponds uniquement avec le mot 'OK'";

            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.set("contents", mapper.valueToTree(List.of(
                    Map.of("parts", List.of(Map.of("text", prompt)))
            )));

            String jsonBody = mapper.writeValueAsString(requestBody);

            System.out.println("📤 URL: " + url.replace(apiKey, "MASKED"));
            System.out.println("📤 Corps: " + jsonBody);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            System.out.println("📊 Status: " + resp.statusCode());
            System.out.println("📥 Réponse: " + resp.body());

            if (resp.statusCode() == 200) {
                JsonNode root = mapper.readTree(resp.body());
                System.out.println("✅ Structure de la réponse:");
                System.out.println("   " + root.toPrettyString().replace("\n", "\n   "));

                // Essayer d'extraire le texte
                try {
                    String text = root.path("candidates").get(0)
                            .path("content").path("parts").get(0)
                            .path("text").asText();
                    System.out.println("📝 Texte extrait: '" + text + "'");
                } catch (Exception e) {
                    System.out.println("❌ Impossible d'extraire le texte: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testApplicationFormat(String apiKey, String model) {
        System.out.println("\n--- Test avec le format de l'application ---");

        String url = "https://generativelanguage.googleapis.com/v1/models/"
                + model + ":generateContent?key=" + apiKey;

        try {
            // Prompt identique à celui de l'application
            String prompt = """
                Tu es un expert en tourisme durable et empreinte carbone.
                Voici les dépenses d'un voyageur à Paris, France (total : 950.00 EUR) :
                
                - Hôtel [Hébergement] 450.00 EUR
                - Vol [Transport] 320.00 EUR
                - Restaurant [Restauration] 180.00 EUR
                
                Analyse ces dépenses sous l'angle écologique et réponds UNIQUEMENT en JSON valide, selon ce format exact :
                {
                  "score": <entier entre 0 et 100, 100 = très écologique>,
                  "label": "<emoji + courte évaluation>",
                  "co2Estimate": "<estimation CO2 en kg>",
                  "analysis": "<2-3 phrases d'analyse>",
                  "alternatives": "<3 alternatives concrètes>"
                }
                """;

            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.set("contents", mapper.valueToTree(List.of(
                    Map.of("parts", List.of(Map.of("text", prompt)))
            )));

            ObjectNode config = mapper.createObjectNode();
            config.put("response_mime_type", "application/json");
            config.put("temperature", 0.2);
            config.put("maxOutputTokens", 1000);
            requestBody.set("generationConfig", config);

            String jsonBody = mapper.writeValueAsString(requestBody);

            System.out.println("📤 URL: " + url.replace(apiKey, "MASKED"));
            System.out.println("📤 Corps (tronqué): " + jsonBody.substring(0, Math.min(300, jsonBody.length())) + "...");

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            System.out.println("📊 Status: " + resp.statusCode());

            if (resp.statusCode() == 200) {
                JsonNode root = mapper.readTree(resp.body());
                System.out.println("✅ Réponse reçue:");

                // Afficher la structure complète
                System.out.println(root.toPrettyString());

                // Essayer de parser le JSON retourné
                try {
                    String text = root.path("candidates").get(0)
                            .path("content").path("parts").get(0)
                            .path("text").asText();

                    System.out.println("\n📝 Texte JSON reçu:");
                    System.out.println(text);

                    // Tenter de parser ce texte comme JSON
                    JsonNode result = mapper.readTree(text);
                    System.out.println("\n✅ JSON parsé avec succès:");
                    System.out.println("   score: " + result.path("score").asInt());
                    System.out.println("   label: " + result.path("label").asText());
                    System.out.println("   co2Estimate: " + result.path("co2Estimate").asText());

                } catch (Exception e) {
                    System.out.println("\n❌ Erreur parsing JSON: " + e.getMessage());
                }

            } else {
                System.out.println("❌ Erreur API:");
                try {
                    JsonNode error = mapper.readTree(resp.body());
                    System.out.println(error.toPrettyString());
                } catch (Exception e) {
                    System.out.println(resp.body());
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}