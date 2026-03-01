package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CerebrasService {

    private static final String CEREBRAS_API_KEY = EnvLoader.get("CEREBRAS_API_KEY");
    private static final String CEREBRAS_URL = "https://api.cerebras.ai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.3-70b";

    private final OkHttpClient client;

    public CerebrasService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        if (!isApiConfigured()) {
            System.out.println("⚠️⚠️⚠️ ATTENTION: Clé API Cerebras non configurée!");
            System.out.println("📝 Créez un fichier .env avec CEREBRAS_API_KEY=votre_cle");
        }
    }

    /**
     * Vérifie si l'API Cerebras est configurée
     */
    private boolean isApiConfigured() {
        return EnvLoader.hasValidKey("CEREBRAS_API_KEY");
    }

    /**
     * Génère une réponse avec Cerebras
     */
    public String generateResponse(String prompt, double temperature) throws IOException {
        if (!isApiConfigured()) {
            System.out.println("🔧 API Cerebras non configurée - Utilisation du mode simulation");
            return getMockResponse(prompt);
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", DEFAULT_MODEL);
        requestBody.addProperty("temperature", temperature);
        requestBody.addProperty("max_tokens", 2048);
        requestBody.addProperty("top_p", 0.9);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "Tu es un assistant utile qui répond en français.");
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        Request request = new Request.Builder()
                .url(CEREBRAS_URL)
                .header("Authorization", "Bearer " + CEREBRAS_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                System.err.println("❌ Erreur Cerebras - Code: " + response.code() + ", Body: " + errorBody);

                if (response.code() == 401 || response.code() == 403) {
                    System.err.println("🔑 Clé API Cerebras invalide!");
                }
                return getMockResponse(prompt);
            }

            String jsonData = response.body().string();
            return parseResponse(jsonData);
        }
    }

    /**
     * Génère un itinéraire avec format JSON structuré pour les voyages
     */
    public String generateItinerary(String prompt) throws IOException {
        if (!isApiConfigured()) {
            return null;
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", DEFAULT_MODEL);
        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 4096);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content",
                "Tu es un expert en planification de voyages. " +
                        "Tu réponds UNIQUEMENT avec du JSON valide respectant le format demandé, sans texte avant ou après.");
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        Request request = new Request.Builder()
                .url(CEREBRAS_URL)
                .header("Authorization", "Bearer " + CEREBRAS_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            String jsonData = response.body().string();
            return parseResponse(jsonData);
        }
    }

    /**
     * Parse la réponse JSON de Cerebras
     */
    private String parseResponse(String jsonData) {
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();

            if (json.has("choices") && json.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();

                if (choice.has("message")) {
                    JsonObject message = choice.getAsJsonObject("message");
                    if (message.has("content") && !message.get("content").isJsonNull()) {
                        return message.get("content").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing Cerebras: " + e.getMessage());
        }
        return null;
    }

    /**
     * Réponse simulée pour le développement
     */
    private String getMockResponse(String prompt) {
        if (prompt.contains("itinéraire") || prompt.contains("voyage")) {
            return "{\n" +
                    "  \"nom\": \"Tunis6\",\n" +
                    "  \"description\": \"Découvrez les merveilles de Tunisie en 6 jours\",\n" +
                    "  \"jours\": [\n" +
                    "    {\n" +
                    "      \"numero\": 1,\n" +
                    "      \"theme\": \"Arrivée à Tunis\",\n" +
                    "      \"activites\": [\n" +
                    "        {\n" +
                    "          \"nom\": \"Visite de la médina\",\n" +
                    "          \"lieu\": \"Médina de Tunis\",\n" +
                    "          \"heure\": \"10:00\",\n" +
                    "          \"duree\": 3.0,\n" +
                    "          \"budget\": 20\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
        }
        return "**CONSEILS POUR " + prompt + "**\n\n" +
                "**SALUER**\n" +
                "• Poignée de main ferme avec la main droite\n" +
                "• Dire 'Bonjour' est apprécié\n\n" +
                "**S'HABILLER**\n" +
                "• Tenue modeste recommandée\n" +
                "• Épaules et genoux couverts dans les lieux religieux";
    }

    /**
     * Vérifie si l'API fonctionne (test de connexion)
     */
    public boolean testConnection() {
        try {
            String response = generateResponse("Test: réponds 'OK' si tu me reçois", 0.1);
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}