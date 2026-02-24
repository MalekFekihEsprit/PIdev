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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeasonService {

    private static final String OPEN_METEO_URL = "https://archive-api.open-meteo.com/v1/archive";
    private static final double IDEAL_TEMP_MIN = 18.0;
    private static final double IDEAL_TEMP_MAX = 30.0;
    private static final double MAX_PRECIPITATION = 100.0;

    public String getBestSeason(double latitude, double longitude) {
        try {
            // Use daily API which we know works
            String jsonResponse = fetchDailyClimateData(latitude, longitude);

            // Parse daily data and aggregate to monthly
            List<MonthlyClimate> monthlyData = aggregateDailyToMonthly(jsonResponse);

            if (monthlyData.isEmpty()) {
                System.out.println("No climate data available, using hemisphere fallback");
                return getHemisphereSeason(latitude);
            }

            // Log the aggregated monthly data
            System.out.println("=== Aggregated Monthly Climate Data for " + latitude + ", " + longitude + " ===");
            for (MonthlyClimate mc : monthlyData) {
                System.out.println(mc.monthName + ": " +
                        String.format("%.1f", mc.temperature) + "°C, " +
                        String.format("%.1f", mc.precipitation) + "mm");
            }

            return calculateBestSeason(monthlyData, latitude);

        } catch (Exception e) {
            System.err.println("Error determining best season: " + e.getMessage());
            e.printStackTrace();
            return getHemisphereSeason(latitude);
        }
    }

    /**
     * Fetch daily climate data (which works reliably)
     */
    private String fetchDailyClimateData(double latitude, double longitude) throws Exception {
        String urlString = OPEN_METEO_URL +
                "?latitude=" + latitude +
                "&longitude=" + longitude +
                "&start_date=2023-01-01" +
                "&end_date=2023-12-31" +
                "&daily=temperature_2m_mean,precipitation_sum" +
                "&timezone=auto";

        System.out.println("Fetching daily climate data from: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorResponse = readResponse(conn.getErrorStream());
            throw new Exception("API returned error code: " + responseCode + " - " + errorResponse);
        }

        String response = readResponse(conn.getInputStream());
        conn.disconnect();

        return response;
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

    /**
     * Aggregate daily data into monthly averages
     */
    private List<MonthlyClimate> aggregateDailyToMonthly(String jsonResponse) {
        List<MonthlyClimate> monthlyData = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(jsonResponse);

            if (!root.has("daily")) {
                System.err.println("No daily data in response");
                return monthlyData;
            }

            JSONObject daily = root.getJSONObject("daily");

            if (!daily.has("time") || !daily.has("temperature_2m_mean") || !daily.has("precipitation_sum")) {
                System.err.println("Missing required daily data fields");
                return monthlyData;
            }

            JSONArray times = daily.getJSONArray("time");
            JSONArray temperatures = daily.getJSONArray("temperature_2m_mean");
            JSONArray precipitations = daily.getJSONArray("precipitation_sum");

            // Map to store monthly aggregates
            Map<Integer, MonthAggregate> monthMap = new HashMap<>();

            String[] monthNames = {
                    "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
            };

            for (int i = 0; i < times.length(); i++) {
                String dateStr = times.getString(i);
                // Parse date to get month (format: "2023-01-01")
                String[] dateParts = dateStr.split("-");
                if (dateParts.length < 2) continue;

                int month = Integer.parseInt(dateParts[1]); // 1-12

                double temp = temperatures.getDouble(i);
                double precip = precipitations.getDouble(i);

                MonthAggregate agg = monthMap.getOrDefault(month, new MonthAggregate());
                agg.sumTemp += temp;
                agg.sumPrecip += precip;
                agg.count++;
                monthMap.put(month, agg);
            }

            // Calculate averages and create MonthlyClimate objects
            for (int month = 1; month <= 12; month++) {
                MonthAggregate agg = monthMap.get(month);
                if (agg != null && agg.count > 0) {
                    double avgTemp = agg.sumTemp / agg.count;
                    double totalPrecip = agg.sumPrecip; // Precipitation is sum, not average

                    monthlyData.add(new MonthlyClimate(
                            monthNames[month-1],
                            month,
                            avgTemp,
                            totalPrecip
                    ));
                } else {
                    // If no data for a month, add placeholder
                    monthlyData.add(new MonthlyClimate(
                            monthNames[month-1],
                            month,
                            15.0, // Default mild temperature
                            50.0  // Default moderate precipitation
                    ));
                }
            }

        } catch (Exception e) {
            System.err.println("Error aggregating daily data: " + e.getMessage());
            e.printStackTrace();
        }

        return monthlyData;
    }

    /**
     * Calculate best season based on monthly climate data
     */
    private String calculateBestSeason(List<MonthlyClimate> monthlyData, double latitude) {
        if (monthlyData.size() < 12) {
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

            System.out.println(seasonNames[s] + " score: " + String.format("%.2f", seasonScores[s]) +
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
            return "Été"; // Northern hemisphere
        } else {
            return "Hiver"; // Southern hemisphere
        }
    }

    /**
     * Helper class for monthly aggregation
     */
    private static class MonthAggregate {
        double sumTemp = 0;
        double sumPrecip = 0;
        int count = 0;
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