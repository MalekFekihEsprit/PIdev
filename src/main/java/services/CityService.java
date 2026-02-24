package services;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CityService {
    private static final String BASE_URL = "https://wft-geo-db.p.rapidapi.com/v1/geo/cities";
    private static final String RAPIDAPI_HOST = "wft-geo-db.p.rapidapi.com";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    private final String rapidApiKey;
    private final Gson gson;

    public CityService(String rapidApiKey) {
        this.rapidApiKey = rapidApiKey;
        this.gson = new GsonBuilder().create();
    }

    /**
     * Searches for cities in a given country
     *
     * @param countryCode The country code (e.g., "FR", "TN", "IT")
     * @param namePrefix The city name prefix to search for
     * @param limit Maximum number of results
     * @return List of CitySuggestion objects
     */
    public List<CitySuggestion> suggestCities(String countryCode, String namePrefix, int limit) {
        if (countryCode == null || countryCode.trim().isEmpty() ||
                namePrefix == null || namePrefix.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String encodedPrefix = URLEncoder.encode(namePrefix, StandardCharsets.UTF_8.toString());
            String urlString = BASE_URL +
                    "?namePrefix=" + encodedPrefix +
                    "&countryIds=" + countryCode +
                    "&limit=" + limit +
                    "&offset=0" +
                    "&sort=name" +
                    "&types=CITY";

            String jsonResponse = makeApiCall(urlString);
            return parseCityResponse(jsonResponse);

        } catch (Exception e) {
            System.err.println("Error fetching city suggestions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Searches for cities including deleted ones
     *
     * @param countryCode The country code (e.g., "FR", "TN", "IT")
     * @param namePrefix The city name prefix to search for
     * @param limit Maximum number of results
     * @return List of CitySuggestion objects
     */
    public List<CitySuggestion> suggestCitiesIncludeDeleted(String countryCode, String namePrefix, int limit) {
        if (countryCode == null || countryCode.trim().isEmpty() ||
                namePrefix == null || namePrefix.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String encodedPrefix = URLEncoder.encode(namePrefix, StandardCharsets.UTF_8.toString());
            String urlString = BASE_URL +
                    "?namePrefix=" + encodedPrefix +
                    "&countryIds=" + countryCode +
                    "&limit=" + limit +
                    "&offset=0" +
                    "&includeDeleted=ALL" +  // Include soft-deleted cities
                    "&sort=-population" +      // Sort by population (largest first)
                    "&types=CITY";

            String jsonResponse = makeApiCall(urlString);
            return parseCityResponse(jsonResponse);

        } catch (Exception e) {
            System.err.println("Error fetching city suggestions (includeDeleted): " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Searches for cities in all countries (use with caution)
     *
     * @param namePrefix The city name prefix to search for
     * @param limit Maximum number of results
     * @return List of CitySuggestion objects
     */
    public List<CitySuggestion> suggestCitiesAllCountries(String namePrefix, int limit) {
        if (namePrefix == null || namePrefix.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String encodedPrefix = URLEncoder.encode(namePrefix, StandardCharsets.UTF_8.toString());
            String urlString = BASE_URL +
                    "?namePrefix=" + encodedPrefix +
                    "&limit=" + limit +
                    "&offset=0" +
                    "&sort=-population" +      // Sort by population (largest first)
                    "&types=CITY";

            String jsonResponse = makeApiCall(urlString);
            return parseCityResponse(jsonResponse);

        } catch (Exception e) {
            System.err.println("Error fetching city suggestions (all countries): " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Validates if a city exists in the given country and returns its coordinates
     *
     * @param countryCode The country code (e.g., "FR", "TN", "IT")
     * @param cityName The city name to validate
     * @return CityCoordinates if found, null otherwise
     */
    public CityCoordinates validateCity(String countryCode, String cityName) {
        if (countryCode == null || countryCode.trim().isEmpty() ||
                cityName == null || cityName.trim().isEmpty()) {
            return null;
        }

        try {
            String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
            String urlString = BASE_URL +
                    "?namePrefix=" + encodedCity +
                    "&countryIds=" + countryCode +
                    "&limit=5" +
                    "&offset=0" +
                    "&types=CITY";

            String jsonResponse = makeApiCall(urlString);
            List<CitySuggestion> cities = parseCityResponse(jsonResponse);

            if (!cities.isEmpty()) {
                for (CitySuggestion city : cities) {
                    if (city.getName().equalsIgnoreCase(cityName)) {
                        return new CityCoordinates(city.getLatitude(), city.getLongitude());
                    }
                }
            }
            return null;

        } catch (Exception e) {
            System.err.println("Error validating city: " + e.getMessage());
            return null;
        }
    }

    private String makeApiCall(String urlString) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-rapidapi-key", rapidApiKey);
            connection.setRequestProperty("x-rapidapi-host", RAPIDAPI_HOST);

            int responseCode = connection.getResponseCode();

            if (responseCode == 429) {
                System.err.println("Rate limit exceeded. Please wait before making more requests.");
                return "{\"data\":[]}";
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection.getInputStream());
            } else {
                String errorResponse = readResponse(connection.getErrorStream());
                System.err.println("API error " + responseCode + ": " + errorResponse);
                return "{\"data\":[]}";
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) return "{\"data\":[]}";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private List<CitySuggestion> parseCityResponse(String jsonResponse) {
        List<CitySuggestion> suggestions = new ArrayList<>();

        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Check if the response contains a "data" array
            if (root.has("data") && root.get("data").isJsonArray()) {
                JsonArray cities = root.getAsJsonArray("data");

                for (JsonElement element : cities) {
                    JsonObject city = element.getAsJsonObject();

                    String name = city.has("name") ? city.get("name").getAsString() : "";
                    String country = city.has("country") ? city.get("country").getAsString() : "";
                    String countryCode = city.has("countryCode") ? city.get("countryCode").getAsString() : "";
                    double latitude = city.has("latitude") ? city.get("latitude").getAsDouble() : 0.0;
                    double longitude = city.has("longitude") ? city.get("longitude").getAsDouble() : 0.0;
                    int population = city.has("population") ? city.get("population").getAsInt() : 0;

                    suggestions.add(new CitySuggestion(name, country, countryCode, latitude, longitude, population));
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing city response: " + e.getMessage());
        }

        return suggestions;
    }

    // Inner class for city suggestions
    public static class CitySuggestion {
        private final String name;
        private final String country;
        private final String countryCode;
        private final double latitude;
        private final double longitude;
        private final int population;

        public CitySuggestion(String name, String country, String countryCode,
                              double latitude, double longitude, int population) {
            this.name = name;
            this.country = country;
            this.countryCode = countryCode;
            this.latitude = latitude;
            this.longitude = longitude;
            this.population = population;
        }

        public String getName() { return name; }
        public String getCountry() { return country; }
        public String getCountryCode() { return countryCode; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public int getPopulation() { return population; }

        @Override
        public String toString() {
            // Return just the city name without population
            return name;
        }
    }

    // Inner class for coordinates
    public static class CityCoordinates {
        private final double latitude;
        private final double longitude;

        public CityCoordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}