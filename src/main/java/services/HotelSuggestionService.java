package services;

import com.google.gson.*;
import entities.HotelSuggestion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HotelSuggestionService {

    private static final String GEOAPIFY_API_URL = "https://api.geoapify.com/v2/places";
    private final String apiKey;
    private final Gson gson;

    public HotelSuggestionService(String apiKey) {
        this.apiKey = apiKey;
        this.gson = new GsonBuilder().create();
    }

    /**
     * Fetches hotel suggestions from Geoapify API
     * @param latitude Destination latitude
     * @param longitude Destination longitude
     * @param radius Search radius in meters
     * @param limit Maximum number of results
     * @return List of HotelSuggestion objects
     * @throws ApiException if API call fails
     */
    public List<HotelSuggestion> getHotelSuggestions(double latitude, double longitude, int radius, int limit) throws ApiException {
        List<HotelSuggestion> suggestions = new ArrayList<>();

        try {
            // Build API URL
            String urlString = GEOAPIFY_API_URL +
                    "?categories=accommodation.hotel" +
                    "&filter=circle:" + longitude + "," + latitude + "," + radius +
                    "&limit=" + limit +
                    "&apiKey=" + apiKey;

            System.out.println("Calling Geoapify API: " + urlString);

            String jsonResponse = makeApiCall(urlString);
            suggestions = parseHotelResponse(jsonResponse);

        } catch (Exception e) {
            throw new ApiException("Failed to fetch hotel suggestions: " + e.getMessage(), e);
        }

        return suggestions;
    }

    private String makeApiCall(String urlString) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                return readResponse(connection.getInputStream());
            } else {
                String errorResponse = readResponse(connection.getErrorStream());
                throw new Exception("API error " + responseCode + ": " + errorResponse);
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(InputStream inputStream) throws Exception {
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

    private List<HotelSuggestion> parseHotelResponse(String jsonResponse) {
        List<HotelSuggestion> suggestions = new ArrayList<>();

        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (root.has("features") && root.get("features").isJsonArray()) {
                JsonArray features = root.getAsJsonArray("features");

                for (JsonElement feature : features) {
                    JsonObject properties = feature.getAsJsonObject()
                            .getAsJsonObject("properties");

                    JsonObject geometry = feature.getAsJsonObject()
                            .getAsJsonObject("geometry");

                    // Extract hotel name
                    String name = properties.has("name") && !properties.get("name").isJsonNull() ?
                            properties.get("name").getAsString() : "Hôtel sans nom";

                    // Extract address
                    String address = properties.has("formatted") && !properties.get("formatted").isJsonNull() ?
                            properties.get("formatted").getAsString() : "Adresse non disponible";

                    // Extract rating (if available)
                    Double rating = null;
                    if (properties.has("rating") && !properties.get("rating").isJsonNull()) {
                        rating = properties.get("rating").getAsDouble();
                    }

                    // Extract coordinates
                    Double lat = null;
                    Double lon = null;
                    if (geometry != null && geometry.has("coordinates")) {
                        JsonArray coords = geometry.getAsJsonArray("coordinates");
                        if (coords.size() >= 2) {
                            lon = coords.get(0).getAsDouble();
                            lat = coords.get(1).getAsDouble();
                        }
                    }

                    if (lat != null && lon != null) {
                        suggestions.add(new HotelSuggestion(name, address, rating, lat, lon));
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing hotel response: " + e.getMessage());
        }

        return suggestions;
    }

    // Custom exception class
    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}