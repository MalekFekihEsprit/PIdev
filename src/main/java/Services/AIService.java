package Services;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AIService {

    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private final String apiKey;
    private final Gson gson;

    // Using the working Google Gemma model
    private final String[] modelsToTry = {
            "google/gemma-3-4b-it:free",  // This one works!
            "nex-agi/deepseek-v3.1-nex-n1:free",
            "meta-llama/llama-3.3-70b-instruct:free"
    };

    public AIService(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        this.apiKey = apiKey.trim();
        this.gson = new GsonBuilder().create();
    }

    public String generateDescription(String city, String country) throws AIException {
        if (city == null || city.trim().isEmpty() || country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("City and country cannot be empty");
        }

        List<String> errors = new ArrayList<>();

        for (String model : modelsToTry) {
            try {
                System.out.println("Trying model: " + model);
                String requestBody = buildRequestBody(city, country, model);
                String jsonResponse = makeApiCall(requestBody);
                String content = parseResponse(jsonResponse);

                if (content != null && !content.trim().isEmpty()) {
                    System.out.println("✅ Success with model: " + model);
                    return content.trim();
                }
            } catch (Exception e) {
                String error = "Model " + model + " failed: " + e.getMessage();
                System.err.println(error);
                errors.add(error);
            }
        }

        throw new AIException("All models failed. Last error: " +
                (errors.isEmpty() ? "Unknown" : errors.get(errors.size() - 1)));
    }

    private String buildRequestBody(String city, String country, String model) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("max_tokens", 1000);
        requestBody.addProperty("temperature", 0.7);

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");

        // UPDATED PROMPT - Removed the header line
        String prompt = String.format(
                "You are a travel expert. Write about %s, %s in the following format:\n\n" +
                        "Write a short, professional tourism description (maximum 50 words) for the city of %s, %s. " +
                        "Mention culture, famous landmarks, and overall atmosphere. Do not use emojis or headers like 'Description:' - just write the description text directly.\n\n" +
                        "Then create a detailed 7-day travel itinerary for %s, %s. For each day, provide:\n" +
                        "Day 1: [Title] - Brief description\n" +
                        "Day 2: [Title] - Brief description\n" +
                        "Day 3: [Title] - Brief description\n" +
                        "Day 4: [Title] - Brief description\n" +
                        "Day 5: [Title] - Brief description\n" +
                        "Day 6: [Title] - Brief description\n" +
                        "Day 7: [Title] - Brief description\n\n" +
                        "Important: Do not include any headers like '=== DESCRIPTION ===' or 'Nice, France: Tourism Description'. " +
                        "Start directly with the description text, then have a blank line, then start the itinerary with 'Day 1:'.",
                city, country, city, country, city, country
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

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("HTTP-Referer", "http://localhost:8080");
            connection.setRequestProperty("X-Title", "TravelMate App");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response code for " + modelFromRequest(requestBody) + ": " + responseCode);

            if (responseCode == 200) {
                return readResponse(connection.getInputStream());
            } else {
                String errorResponse = readResponse(connection.getErrorStream());
                throw new IOException("API returned " + responseCode + ": " + errorResponse);
            }

        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String modelFromRequest(String requestBody) {
        try {
            JsonObject obj = JsonParser.parseString(requestBody).getAsJsonObject();
            return obj.get("model").getAsString();
        } catch (Exception e) {
            return "unknown";
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

            if (root.has("error")) {
                JsonObject error = root.getAsJsonObject("error");
                String message = error.has("message") ? error.get("message").getAsString() : "Unknown error";
                throw new AIException("API error: " + message);
            }

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

            throw new AIException("Unexpected API response: " + jsonResponse);

        } catch (JsonParseException e) {
            throw new AIException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    public static class AIException extends Exception {
        public AIException(String message) { super(message); }
        public AIException(String message, Throwable cause) { super(message, cause); }
    }
}