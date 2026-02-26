package Services;

import Entities.HotelSuggestion;
import com.google.gson.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HotelSuggestionService {

    private static final String GEOAPIFY_API_URL = "https://api.geoapify.com/v2/places";
    private static final String OSM_NOMINATIM_API = "https://nominatim.openstreetmap.org/search";
    private static final String OSM_OVERPASS_API = "https://overpass-api.de/api/interpreter";
    private final String apiKey;
    private final Gson gson;

    public HotelSuggestionService(String apiKey) {
        this.apiKey = apiKey;
        this.gson = new GsonBuilder().create();
    }

    /**
     * Fetches hotel suggestions from Geoapify API and enriches with OSM star ratings
     */
    public List<HotelSuggestion> getHotelSuggestions(double latitude, double longitude, int radius, int limit) throws ApiException {
        List<HotelSuggestion> suggestions = new ArrayList<>();

        try {
            // Step 1: Get hotels from Geoapify
            String urlString = GEOAPIFY_API_URL +
                    "?categories=accommodation.hotel" +
                    "&filter=circle:" + longitude + "," + latitude + "," + radius +
                    "&limit=" + limit +
                    "&apiKey=" + apiKey;

            System.out.println("Calling Geoapify API: " + urlString);

            String jsonResponse = makeApiCall(urlString);
            suggestions = parseHotelResponse(jsonResponse);

            // Step 2: Enrich each hotel with OSM star ratings
            for (HotelSuggestion hotel : suggestions) {
                enrichWithOsmStars(hotel);
                // Small delay to respect Nominatim usage policy
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            throw new ApiException("Failed to fetch hotel suggestions: " + e.getMessage(), e);
        }

        return suggestions;
    }

    /**
     * Enriches a hotel suggestion with star rating from OpenStreetMap
     */
    private void enrichWithOsmStars(HotelSuggestion hotel) throws Exception {
        // Try Overpass API first (more comprehensive)
        Float osmStars = getStarsFromOverpass(hotel.getLatitude(), hotel.getLongitude(), hotel.getNom());

        if (osmStars != null) {
            hotel.setRating(osmStars.doubleValue());
            System.out.println("⭐ Found OSM stars for " + hotel.getNom() + ": " + osmStars);
        } else {
            // Fallback to Nominatim
            osmStars = getStarsFromNominatim(hotel.getNom(), hotel.getLatitude(), hotel.getLongitude());
            if (osmStars != null) {
                hotel.setRating(osmStars.doubleValue());
                System.out.println("⭐ Found Nominatim stars for " + hotel.getNom() + ": " + osmStars);
            }
        }
    }

    /**
     * Gets star rating from Overpass API using coordinates and name
     */
    private Float getStarsFromOverpass(double lat, double lon, String hotelName) throws Exception {
        // Search for hotel within 50m radius
        String overpassQuery = String.format(
                "[out:json];" +
                        "(" +
                        "  node[\"tourism\"=\"hotel\"](around:50,%f,%f);" +
                        "  way[\"tourism\"=\"hotel\"](around:50,%f,%f);" +
                        ");" +
                        "out body;",
                lat, lon, lat, lon
        );

        String encodedQuery = URLEncoder.encode(overpassQuery, StandardCharsets.UTF_8.toString());
        String urlString = OSM_OVERPASS_API + "?data=" + encodedQuery;

        try {
            String response = makeApiCall(urlString);
            return parseStarsFromOverpass(response, hotelName);
        } catch (Exception e) {
            System.err.println("Overpass API error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses star rating from Overpass JSON response
     */
    private Float parseStarsFromOverpass(String jsonResponse, String hotelName) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (root.has("elements") && root.get("elements").isJsonArray()) {
                JsonArray elements = root.getAsJsonArray("elements");

                for (JsonElement element : elements) {
                    JsonObject obj = element.getAsJsonObject();

                    // Check if this is the right hotel by name comparison
                    if (obj.has("tags")) {
                        JsonObject tags = obj.getAsJsonObject("tags");

                        // Check name match (case insensitive)
                        if (tags.has("name")) {
                            String osmName = tags.get("name").getAsString();
                            if (osmName.toLowerCase().contains(hotelName.toLowerCase()) ||
                                    hotelName.toLowerCase().contains(osmName.toLowerCase())) {

                                // Look for stars tag
                                if (tags.has("stars")) {
                                    String starsStr = tags.get("stars").getAsString();
                                    return parseStarToFloat(starsStr);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Overpass response: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets star rating from Nominatim API
     */
    private Float getStarsFromNominatim(String hotelName, double lat, double lon) throws Exception {
        String query = URLEncoder.encode(hotelName + " hotel", StandardCharsets.UTF_8.toString());
        String urlString = String.format(
                "%s?q=%s&format=json&limit=1&addressdetails=1&namedetails=1&extratags=1",
                OSM_NOMINATIM_API, query
        );

        try {
            String response = makeApiCall(urlString);
            return parseStarsFromNominatim(response);
        } catch (Exception e) {
            System.err.println("Nominatim API error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses star rating from Nominatim JSON response
     */
    private Float parseStarsFromNominatim(String jsonResponse) {
        try {
            JsonArray results = JsonParser.parseString(jsonResponse).getAsJsonArray();

            if (results.size() > 0) {
                JsonObject first = results.get(0).getAsJsonObject();

                // Check extratags for stars
                if (first.has("extratags")) {
                    JsonObject extratags = first.getAsJsonObject("extratags");
                    if (extratags.has("stars")) {
                        String starsStr = extratags.get("stars").getAsString();
                        return parseStarToFloat(starsStr);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Nominatim response: " + e.getMessage());
        }
        return null;
    }

    /**
     * Parses star value from various OSM formats to float
     * Handles formats: "4", "4.5", "4S", "4.5S", "☆☆☆☆", "★★★★" etc.
     */
    private Float parseStarToFloat(String starsStr) {
        try {
            String cleanStars = starsStr.trim();

            // Handle Superior suffix (S)
            if (cleanStars.endsWith("S") || cleanStars.endsWith("s")) {
                cleanStars = cleanStars.substring(0, cleanStars.length() - 1);
            }

            // Try parsing as number first
            try {
                return Float.parseFloat(cleanStars);
            } catch (NumberFormatException e) {
                // Count star symbols
                int starCount = countStarSymbols(cleanStars);
                if (starCount > 0) {
                    return (float) starCount;
                }

                // Check for half stars with Unicode symbols
                float halfStars = countHalfStars(cleanStars);
                if (halfStars > 0) {
                    return halfStars;
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing star value: " + starsStr);
        }
        return null;
    }

    /**
     * Counts full star symbols in a string
     */
    private int countStarSymbols(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == '☆' || c == '★' || c == '✩' || c == '✪' || c == '✫') {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts half star symbols (like "½" or "⯨")
     */
    private float countHalfStars(String text) {
        float total = 0;
        boolean hasHalf = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '½' || c == '⯨' || c == '◐' || c == '◓') {
                hasHalf = true;
            } else if (c == '☆' || c == '★' || c == '✩') {
                total += 1;
            }
        }

        if (hasHalf) {
            total += 0.5f;
        }

        return total > 0 ? total : 0;
    }

    private String makeApiCall(String urlString) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // Set User-Agent for OSM APIs (required by Nominatim policy)
            connection.setRequestProperty("User-Agent", "TravelMate-App/1.0");
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

                    // Extract rating from Geoapify (if available)
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