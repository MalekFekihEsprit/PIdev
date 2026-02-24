package services;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class LocalCityFallbackService {

    private static final String FALLBACK_FILE = "/cities.json";
    private Map<String, List<CityData>> fallbackCities;
    private final Gson gson;

    public LocalCityFallbackService() {
        this.gson = new GsonBuilder().create();
        loadFallbackData();
    }

    private void loadFallbackData() {
        try {
            // Load JSON file from resources
            InputStream inputStream = getClass().getResourceAsStream(FALLBACK_FILE);
            if (inputStream == null) {
                System.err.println("Fallback file not found: " + FALLBACK_FILE);
                fallbackCities = new HashMap<>();
                return;
            }

            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                Type type = new TypeToken<Map<String, List<CityData>>>(){}.getType();
                fallbackCities = gson.fromJson(reader, type);
                System.out.println("Loaded fallback cities for " + fallbackCities.size() + " countries");
            }

        } catch (Exception e) {
            System.err.println("Error loading fallback city data: " + e.getMessage());
            fallbackCities = new HashMap<>();
        }
    }

    /**
     * Get city suggestions from local fallback database
     */
    public List<CitySuggestion> getSuggestions(String countryCode, String prefix) {
        if (countryCode == null || prefix == null || prefix.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<CityData> cities = fallbackCities.get(countryCode.toUpperCase());
        if (cities == null) {
            return new ArrayList<>();
        }

        String lowerPrefix = prefix.toLowerCase().trim();

        return cities.stream()
                .filter(city -> city.name.toLowerCase().startsWith(lowerPrefix))
                .map(city -> new CitySuggestion(
                        city.name,
                        "", // country name not needed
                        countryCode,
                        city.latitude,
                        city.longitude,
                        0  // population not needed
                ))
                .collect(Collectors.toList());
    }

    /**
     * Validate a city and return its coordinates
     */
    public CityCoordinates validateCity(String countryCode, String cityName) {
        if (countryCode == null || cityName == null || cityName.trim().isEmpty()) {
            return null;
        }

        List<CityData> cities = fallbackCities.get(countryCode.toUpperCase());
        if (cities == null) {
            return null;
        }

        String lowerCityName = cityName.toLowerCase().trim();

        return cities.stream()
                .filter(city -> city.name.toLowerCase().equals(lowerCityName))
                .findFirst()
                .map(city -> new CityCoordinates(city.latitude, city.longitude))
                .orElse(null);
    }

    /**
     * Check if a country has fallback data
     */
    public boolean hasCountry(String countryCode) {
        return fallbackCities.containsKey(countryCode.toUpperCase());
    }

    /**
     * Get all fallback country codes
     */
    public Set<String> getAvailableCountries() {
        return fallbackCities.keySet();
    }

    // Inner class for JSON deserialization
    private static class CityData {
        String name;
        double latitude;
        double longitude;
    }

    // Recreate CitySuggestion and CityCoordinates to avoid dependency on CityService
    public static class CitySuggestion {
        private final String name;
        private final String country;
        private final String countryCode;
        private final double latitude;
        private final double longitude;
        private final int population;

        public CitySuggestion(String name, String country, String countryCode,
                              double latitude, double longitude, int population) {
            this.name = name;;
            this.countryCode = countryCode;
            this.country = country;
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
            return name;
        }
    }

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