package services;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for fetching country information from REST Countries API
 */
public class CountryService {
    private static final String BASE_URL = "https://restcountries.com/v3.1/name/";
    private static final String FIELDS_PARAM = "?fields=currencies,flags,languages";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    private final Gson gson;

    public CountryService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Fetches country information for the specified country name
     *
     * @param countryName The name of the country to look up
     * @return CountryInfo object containing currency, flag URL, and languages
     * @throws CountryNotFoundException if the country is not found
     * @throws ApiException for other API errors
     */
    public CountryInfo getCountryInfo(String countryName)
            throws CountryNotFoundException, ApiException {

        validateCountryName(countryName);

        try {
            String encodedCountryName = URLEncoder.encode(countryName, StandardCharsets.UTF_8.toString());
            String urlString = BASE_URL + encodedCountryName + FIELDS_PARAM;

            String jsonResponse = makeApiCall(urlString);
            List<CountryResponse> countries = parseJsonResponse(jsonResponse);

            if (countries.isEmpty()) {
                throw new CountryNotFoundException("Country not found: " + countryName);
            }

            return mapToCountryInfo(countries.get(0));

        } catch (UnsupportedEncodingException e) {
            throw new ApiException("Failed to encode country name: " + countryName, e);
        } catch (IOException e) {
            throw new ApiException("IO error while fetching country data", e);
        }
    }

    private void validateCountryName(String countryName) {
        if (countryName == null || countryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Country name cannot be null or empty");
        }
    }

    private String makeApiCall(String urlString) throws IOException, ApiException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            // Configure connection
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection.getInputStream());
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new CountryNotFoundException("Country not found (HTTP 404)");
            } else {
                String errorResponse = readResponse(connection.getErrorStream());
                throw new ApiException(String.format(
                        "API request failed with status %d: %s",
                        responseCode, errorResponse));
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

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

    private List<CountryResponse> parseJsonResponse(String jsonResponse) {
        try {
            // The API returns an array of country objects
            JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();
            List<CountryResponse> countries = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                JsonObject countryObj = element.getAsJsonObject();
                CountryResponse country = new CountryResponse();

                // Parse currencies
                if (countryObj.has("currencies") && !countryObj.get("currencies").isJsonNull()) {
                    JsonObject currenciesObj = countryObj.getAsJsonObject("currencies");
                    country.currencies = new HashMap<>();

                    for (String currencyCode : currenciesObj.keySet()) {
                        JsonObject currencyObj = currenciesObj.getAsJsonObject(currencyCode);
                        CurrencyDetails details = new CurrencyDetails();

                        if (currencyObj.has("name") && !currencyObj.get("name").isJsonNull()) {
                            details.name = currencyObj.get("name").getAsString();
                        }
                        if (currencyObj.has("symbol") && !currencyObj.get("symbol").isJsonNull()) {
                            details.symbol = currencyObj.get("symbol").getAsString();
                        }

                        country.currencies.put(currencyCode, details);
                    }
                }

                // Parse flags
                if (countryObj.has("flags") && !countryObj.get("flags").isJsonNull()) {
                    JsonObject flagsObj = countryObj.getAsJsonObject("flags");
                    country.flags = new Flags();

                    if (flagsObj.has("png") && !flagsObj.get("png").isJsonNull()) {
                        country.flags.png = flagsObj.get("png").getAsString();
                    }
                    if (flagsObj.has("svg") && !flagsObj.get("svg").isJsonNull()) {
                        country.flags.svg = flagsObj.get("svg").getAsString();
                    }
                }

                // Parse languages
                if (countryObj.has("languages") && !countryObj.get("languages").isJsonNull()) {
                    JsonObject languagesObj = countryObj.getAsJsonObject("languages");
                    country.languages = new HashMap<>();

                    for (String langCode : languagesObj.keySet()) {
                        String languageName = languagesObj.get(langCode).getAsString();
                        country.languages.put(langCode, languageName);
                    }
                }

                countries.add(country);
            }

            return countries;

        } catch (Exception e) {
            throw new ApiException("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }

    private CountryInfo mapToCountryInfo(CountryResponse country) {
        // Extract and format currency
        String currency = extractCurrency(country.currencies);

        // Extract flag URL
        String flagUrl = country.flags != null && country.flags.png != null ?
                country.flags.png : "";

        // Extract and join languages
        String languages = extractLanguages(country.languages);

        return new CountryInfo(currency, flagUrl, languages);
    }

    private String extractCurrency(Map<String, CurrencyDetails> currencies) {
        if (currencies == null || currencies.isEmpty()) {
            return "No currency information available";
        }

        // Get the first currency entry
        Map.Entry<String, CurrencyDetails> firstCurrency =
                currencies.entrySet().iterator().next();

        String code = firstCurrency.getKey();
        CurrencyDetails details = firstCurrency.getValue();

        // Format as "CODE (symbol)" or just "CODE" if symbol is missing
        if (details != null && details.symbol != null && !details.symbol.isEmpty()) {
            return String.format("%s (%s)", code, details.symbol);
        } else if (details != null && details.name != null) {
            return String.format("%s (%s)", code, details.name);
        } else {
            return code;
        }
    }

    private String extractLanguages(Map<String, String> languages) {
        if (languages == null || languages.isEmpty()) {
            return "No language information available";
        }

        return languages.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    // Custom exception classes
    public static class CountryNotFoundException extends RuntimeException {
        public CountryNotFoundException(String message) {
            super(message);
        }
    }

    public static class ApiException extends RuntimeException {
        public ApiException(String message) {
            super(message);
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Inner classes for JSON parsing
    private static class CountryResponse {
        Map<String, CurrencyDetails> currencies;
        Flags flags;
        Map<String, String> languages;
    }

    private static class CurrencyDetails {
        String name;
        String symbol;
    }

    private static class Flags {
        String png;
        String svg;
    }

    // Public class to hold the formatted country information
    public static class CountryInfo {
        private final String currency;
        private final String flagUrl;
        private final String languages;

        public CountryInfo(String currency, String flagUrl, String languages) {
            this.currency = currency;
            this.flagUrl = flagUrl;
            this.languages = languages;
        }

        public String getCurrency() { return currency; }
        public String getFlagUrl() { return flagUrl; }
        public String getLanguages() { return languages; }

        @Override
        public String toString() {
            return String.format("CountryInfo{currency='%s', flagUrl='%s', languages='%s'}",
                    currency, flagUrl, languages);
        }
    }
}