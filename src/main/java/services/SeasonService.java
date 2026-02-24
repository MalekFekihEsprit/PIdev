package services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SeasonService {

    private static final String OPEN_METEO_URL = "https://archive-api.open-meteo.com/v1/archive";
    private static final String DATE_RANGE = "2023-01-01,2023-12-31";
    private static final double IDEAL_TEMP_MIN = 18.0; // Celsius
    private static final double IDEAL_TEMP_MAX = 30.0; // Celsius
    private static final double MAX_PRECIPITATION = 100.0; // mm per month

    /**
     * Determines the best season to visit based on climate data
     * @param latitude Latitude of the city
     * @param longitude Longitude of the city
     * @return Best season as String ("Printemps", "Été", "Automne", "Hiver")
     */
    public String getBestSeason(double latitude, double longitude) {
        try {
            // Fetch climate data from Open-Meteo
            String jsonResponse = fetchClimateData(latitude, longitude);

            // Parse monthly data
            List<MonthlyClimate> monthlyData = parseClimateData(jsonResponse);

            // Log data for debugging
            System.out.println("=== Climate Data for " + latitude + ", " + longitude + " ===");
            for (MonthlyClimate mc : monthlyData) {
                System.out.println(mc.monthName + ": " + mc.temperature + "°C, " + mc.precipitation + "mm");
            }

            // Calculate best season
            return calculateBestSeason(monthlyData, latitude);

        } catch (Exception e) {
            System.err.println("Error determining best season: " + e.getMessage());
            e.printStackTrace();
            // Default fallback based on hemisphere
            return getHemisphereSeason(latitude);
        }
    }

    /**
     * Makes API call to Open-Meteo
     */
    private String fetchClimateData(double latitude, double longitude) throws Exception {
        // Build URL with parameters - Open-Meteo returns Celsius by default
        String urlString = OPEN_METEO_URL +
                "?latitude=" + latitude +
                "&longitude=" + longitude +
                "&start_date=2023-01-01&end_date=2023-12-31" +
                "&monthly=temperature_2m_mean,precipitation_sum" +
                "&timezone=auto";

        System.out.println("Fetching climate data from: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("API returned error code: " + responseCode);
        }

        // Read response
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        return response.toString();
    }

    /**
     * Parses JSON response into list of MonthlyClimate objects
     */
    private List<MonthlyClimate> parseClimateData(String jsonResponse) {
        List<MonthlyClimate> monthlyData = new ArrayList<>();

        JSONObject root = new JSONObject(jsonResponse);

        // Check if monthly data exists
        if (!root.has("monthly")) {
            System.err.println("No monthly data in response");
            return monthlyData;
        }

        JSONObject monthly = root.getJSONObject("monthly");

        // Check if required arrays exist
        if (!monthly.has("temperature_2m_mean") || !monthly.has("precipitation_sum")) {
            System.err.println("Missing temperature or precipitation data");
            return monthlyData;
        }

        JSONArray temperatures = monthly.getJSONArray("temperature_2m_mean");
        JSONArray precipitations = monthly.getJSONArray("precipitation_sum");

        // Month names (1 = January)
        String[] monthNames = {
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };

        for (int i = 0; i < temperatures.length(); i++) {
            double temp = temperatures.getDouble(i);
            double precip = precipitations.getDouble(i);

            monthlyData.add(new MonthlyClimate(
                    monthNames[i],
                    i + 1, // month number
                    temp,
                    precip
            ));
        }

        return monthlyData;
    }

    /**
     * Calculates best season based on climate data and hemisphere
     */
    private String calculateBestSeason(List<MonthlyClimate> monthlyData, double latitude) {
        if (monthlyData.isEmpty()) {
            return getHemisphereSeason(latitude);
        }

        // Define seasons with month numbers adjusted for hemisphere
        int[][] northernSeasons = {
                {3, 4, 5},    // Printemps (Mars, Avril, Mai)
                {6, 7, 8},    // Été (Juin, Juillet, Août)
                {9, 10, 11},  // Automne (Septembre, Octobre, Novembre)
                {12, 1, 2}    // Hiver (Décembre, Janvier, Février)
        };

        int[][] southernSeasons = {
                {9, 10, 11},  // Printemps (Septembre, Octobre, Novembre)
                {12, 1, 2},   // Été (Décembre, Janvier, Février)
                {3, 4, 5},    // Automne (Mars, Avril, Mai)
                {6, 7, 8}     // Hiver (Juin, Juillet, Août)
        };

        String[] seasonNames = {"Printemps", "Été", "Automne", "Hiver"};

        // Choose season mapping based on hemisphere
        int[][] seasons = (latitude >= 0) ? northernSeasons : southernSeasons;

        double[] seasonScores = new double[4];
        int[] goodMonthsCount = new int[4];
        int[] totalMonthsCount = new int[4];

        // Calculate score for each season
        for (int s = 0; s < seasons.length; s++) {
            for (int monthNum : seasons[s]) {
                // Find climate data for this month
                for (MonthlyClimate mc : monthlyData) {
                    if (mc.monthNumber == monthNum) {
                        totalMonthsCount[s]++;

                        // Check if month meets ideal conditions
                        if (mc.temperature >= IDEAL_TEMP_MIN &&
                                mc.temperature <= IDEAL_TEMP_MAX &&
                                mc.precipitation <= MAX_PRECIPITATION) {
                            goodMonthsCount[s]++;
                        }
                        break;
                    }
                }
            }

            // Score = percentage of good months in the season
            if (totalMonthsCount[s] > 0) {
                seasonScores[s] = (double) goodMonthsCount[s] / totalMonthsCount[s];
            }

            System.out.println(seasonNames[s] + " score: " + seasonScores[s] +
                    " (" + goodMonthsCount[s] + "/" + totalMonthsCount[s] + " good months)");
        }

        // Find best season
        int bestSeasonIndex = 0;
        double bestScore = seasonScores[0];

        for (int i = 1; i < seasonScores.length; i++) {
            if (seasonScores[i] > bestScore) {
                bestScore = seasonScores[i];
                bestSeasonIndex = i;
            }
        }

        // If no good months at all, use temperature-based fallback
        if (bestScore == 0) {
            return getTemperatureBasedSeason(monthlyData, latitude);
        }

        return seasonNames[bestSeasonIndex];
    }

    /**
     * Fallback based on average temperatures when no month meets all criteria
     */
    private String getTemperatureBasedSeason(List<MonthlyClimate> monthlyData, double latitude) {
        // Calculate average temperature for each season
        double[] seasonAvgs = new double[4];
        int[] seasonCounts = new int[4];

        int[][] northernSeasons = {
                {3, 4, 5}, {6, 7, 8}, {9, 10, 11}, {12, 1, 2}
        };

        int[][] southernSeasons = {
                {9, 10, 11}, {12, 1, 2}, {3, 4, 5}, {6, 7, 8}
        };

        int[][] seasons = (latitude >= 0) ? northernSeasons : southernSeasons;
        String[] seasonNames = {"Printemps", "Été", "Automne", "Hiver"};

        for (int s = 0; s < seasons.length; s++) {
            for (int monthNum : seasons[s]) {
                for (MonthlyClimate mc : monthlyData) {
                    if (mc.monthNumber == monthNum) {
                        seasonAvgs[s] += mc.temperature;
                        seasonCounts[s]++;
                        break;
                    }
                }
            }
            if (seasonCounts[s] > 0) {
                seasonAvgs[s] /= seasonCounts[s];
            }
        }

        // Find season with temperature closest to 24°C (ideal average)
        double targetTemp = 24.0;
        int bestSeason = 0;
        double smallestDiff = Math.abs(seasonAvgs[0] - targetTemp);

        for (int i = 1; i < seasonAvgs.length; i++) {
            double diff = Math.abs(seasonAvgs[i] - targetTemp);
            if (diff < smallestDiff) {
                smallestDiff = diff;
                bestSeason = i;
            }
        }

        System.out.println("Temperature-based fallback: best season is " + seasonNames[bestSeason]);
        return seasonNames[bestSeason];
    }

    /**
     * Simple hemisphere-based fallback
     */
    private String getHemisphereSeason(double latitude) {
        if (latitude >= 0) {
            // Northern hemisphere
            return "Été";
        } else {
            // Southern hemisphere
            return "Hiver";
        }
    }

    /**
     * Inner class to hold monthly climate data
     */
    private static class MonthlyClimate {
        String monthName;
        int monthNumber;
        double temperature;
        double precipitation;

        MonthlyClimate(String monthName, int monthNumber, double temperature, double precipitation) {
            this.monthName = monthName;
            this.monthNumber = monthNumber;
            this.temperature = temperature;
            this.precipitation = precipitation;
        }
    }
}