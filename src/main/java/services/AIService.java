package services;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AIService {

    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "deepseek/deepseek-chat";
    private static final int MAX_TOKENS = 150;
    private static final double TEMPERATURE = 0.7;

    private final String apiKey;
    private final Gson gson;

    /**
     * Constructor - reads API key from environment variable
     * @throws IllegalStateException if OPENROUTER_API_KEY is not set
     */
    public AIService() {
        this.apiKey= "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";
        //this.apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OPENROUTER_API_KEY environment variable not set");
        }
        this.gson = new GsonBuilder().create();
    }

    /**
     * Alternative constructor for testing with explicit API key
     */
    public AIService(String apiKey) {
        this.apiKey = apiKey;
        this.gson = new GsonBuilder().create();
    }

    /**
     * Generates a tourism description for a city
     * @param city City name
     * @param country Country name
     * @return Generated description text
     * @throws AIException if API call fails or response is invalid
     */
    public String generateDescription(String city, String country) throws AIException {
        if (city == null || city.trim().isEmpty() || country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("City and country cannot be empty");
        }

        try {
            // Build request body
            String requestBody = buildRequestBody(city, country);

            // Make API call
            String jsonResponse = makeApiCall(requestBody);

            // Parse response
            String description = parseResponse(jsonResponse);

            // Validate response
            if (description == null || description.trim().isEmpty()) {
                throw new AIException("Generated description is empty");
            }

            return description.trim();

        } catch (AIException e) {
            throw e;
        } catch (Exception e) {
            throw new AIException("Failed to generate description: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String city, String country) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        requestBody.addProperty("max_tokens", MAX_TOKENS);
        requestBody.addProperty("temperature", TEMPERATURE);

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");

        String prompt = String.format(
                "Write a short, professional tourism description (maximum 40 words) for the city of %s, %s. " +
                        "Mention culture, famous landmarks, and overall atmosphere. " +
                        "Do not use emojis. Do not use bullet points. Return plain text only.",
                city, country
        );
        userMessage.addProperty("content", prompt);

        messages.add(userMessage);
        requestBody.add("messages", messages);

        return requestBody.toString();
    }

    private String makeApiCall(String requestBody) throws IOException, AIException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(OPENROUTER_API_URL);
            connection = (HttpURLConnection) url.openConnection();

            // Configure connection
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000); // 15 seconds
            connection.setReadTimeout(30000);    // 30 seconds
            connection.setDoOutput(true);

            // Set headers
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("HTTP-Referer", "PI-Dev-TravelApp");
            connection.setRequestProperty("X-Title", "PI-Dev-TravelApp");

            // Write request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Check response code
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                // Success - read response
                return readResponse(connection.getInputStream());
            } else {
                // Error - read error stream
                String errorResponse = readResponse(connection.getErrorStream());
                throw new AIException("API returned error " + responseCode + ": " + errorResponse);
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private String parseResponse(String jsonResponse) throws AIException {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Check for error in response
            if (root.has("error")) {
                JsonObject error = root.getAsJsonObject("error");
                String errorMessage = error.has("message") ? error.get("message").getAsString() : "Unknown error";
                throw new AIException("OpenRouter API error: " + errorMessage);
            }

            // Extract message content from choices[0].message.content
            if (root.has("choices") && root.get("choices").isJsonArray()) {
                JsonArray choices = root.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    if (firstChoice.has("message")) {
                        JsonObject message = firstChoice.getAsJsonObject("message");
                        if (message.has("content")) {
                            return message.get("content").getAsString();
                        }
                    }
                }
            }

            throw new AIException("Unexpected API response structure");

        } catch (JsonParseException e) {
            throw new AIException("Failed to parse API response: " + e.getMessage(), e);
        }
    }

    /**
     * Custom exception for AI service errors
     */
    public static class AIException extends Exception {
        public AIException(String message) {
            super(message);
        }

        public AIException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}