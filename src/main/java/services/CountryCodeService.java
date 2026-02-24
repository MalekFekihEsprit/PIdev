package services;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CountryCodeService {
    private static final String BASE_URL = "https://restcountries.com/v3.1/name/";
    private static final String FIELDS = "?fields=cca2";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    private final Gson gson;

    public CountryCodeService() {
        this.gson = new GsonBuilder().create();
    }

    /**
     * Gets the ISO country code for a country name
     *
     * @param countryName The country name (e.g., "France", "Tunisia")
     * @return The two-letter country code (e.g., "FR", "TN") or null if not found
     */
    public String getCountryCode(String countryName) {
        if (countryName == null || countryName.trim().isEmpty()) {
            return null;
        }

        try {
            String encodedName = URLEncoder.encode(countryName, StandardCharsets.UTF_8.toString());
            String urlString = BASE_URL + encodedName + FIELDS;

            String jsonResponse = makeApiCall(urlString);
            return parseCountryCode(jsonResponse);

        } catch (Exception e) {
            System.err.println("Error getting country code: " + e.getMessage());
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

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection.getInputStream());
            } else {
                return "";
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";

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

    private String parseCountryCode(String jsonResponse) {
        try {
            JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();
            if (jsonArray.size() > 0) {
                JsonObject country = jsonArray.get(0).getAsJsonObject();
                return country.get("cca2").getAsString();
            }
        } catch (Exception e) {
            System.err.println("Error parsing country code: " + e.getMessage());
        }
        return null;
    }
}