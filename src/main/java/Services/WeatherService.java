package Services;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeatherService {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    public static WeatherForecast getForecast(double latitude, double longitude, LocalDate date) {
        try {
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

            String urlString = BASE_URL +
                    "?latitude=" + latitude +
                    "&longitude=" + longitude +
                    "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weathercode,windspeed_10m_max" +
                    "&timezone=auto" +
                    "&start_date=" + dateStr +
                    "&end_date=" + dateStr;

            System.out.println("🌤️ Appel API météo: " + urlString);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return parseForecast(response.toString(), dateStr);
            } else {
                System.err.println("❌ Erreur API météo: " + responseCode);
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Exception météo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static WeatherForecast parseForecast(String jsonResponse, String targetDate) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONObject daily = root.getJSONObject("daily");

            JSONArray times = daily.getJSONArray("time");
            JSONArray tempMax = daily.getJSONArray("temperature_2m_max");
            JSONArray tempMin = daily.getJSONArray("temperature_2m_min");
            JSONArray precip = daily.getJSONArray("precipitation_sum");
            JSONArray weatherCodes = daily.getJSONArray("weathercode");
            JSONArray windSpeed = daily.getJSONArray("windspeed_10m_max");

            // Trouver l'index correspondant à notre date
            int index = -1;
            for (int i = 0; i < times.length(); i++) {
                if (times.getString(i).equals(targetDate)) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                return null;
            }

            WeatherForecast forecast = new WeatherForecast();
            forecast.setDate(targetDate);
            forecast.setTemperatureMax(tempMax.getDouble(index));
            forecast.setTemperatureMin(tempMin.getDouble(index));
            forecast.setPrecipitation(precip.getDouble(index));
            forecast.setWeatherCode(weatherCodes.getInt(index));
            forecast.setWindSpeed(windSpeed.getDouble(index));
            forecast.setWeatherDescription(getWeatherDescription(weatherCodes.getInt(index)));
            forecast.setWeatherIcon(getWeatherIcon(weatherCodes.getInt(index)));

            return forecast;

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing météo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convertit le code météo WMO en description lisible
     */
    private static String getWeatherDescription(int code) {
        switch (code) {
            case 0: return "Ciel dégagé";
            case 1: return "Principalement dégagé";
            case 2: return "Partiellement nuageux";
            case 3: return "Nuageux";
            case 45: case 48: return "Brouillard";
            case 51: case 53: case 55: return "Bruine";
            case 56: case 57: return "Bruine verglaçante";
            case 61: case 63: case 65: return "Pluie";
            case 66: case 67: return "Pluie verglaçante";
            case 71: case 73: case 75: return "Neige";
            case 77: return "Grains de neige";
            case 80: case 81: case 82: return "Averses de pluie";
            case 85: case 86: return "Averses de neige";
            case 95: return "Orage";
            case 96: case 99: return "Orage avec grêle";
            default: return "Conditions variables";
        }
    }

    /**
     * Retourne une icône météo (emoji) selon le code
     */
    private static String getWeatherIcon(int code) {
        switch (code) {
            case 0: return "☀️";
            case 1: return "🌤️";
            case 2: return "⛅";
            case 3: return "☁️";
            case 45: case 48: return "🌫️";
            case 51: case 53: case 55: return "🌧️";
            case 61: case 63: case 65: return "☔";
            case 71: case 73: case 75: return "❄️";
            case 80: case 81: case 82: return "🌦️";
            case 85: case 86: return "🌨️";
            case 95: case 96: case 99: return "⛈️";
            default: return "🌡️";
        }
    }

    /**
     * Classe interne pour les données météo
     */
    public static class WeatherForecast {
        private String date;
        private double temperatureMax;
        private double temperatureMin;
        private double precipitation;
        private int weatherCode;
        private double windSpeed;
        private String weatherDescription;
        private String weatherIcon;

        // Getters et Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public double getTemperatureMax() { return temperatureMax; }
        public void setTemperatureMax(double temperatureMax) { this.temperatureMax = temperatureMax; }

        public double getTemperatureMin() { return temperatureMin; }
        public void setTemperatureMin(double temperatureMin) { this.temperatureMin = temperatureMin; }

        public double getPrecipitation() { return precipitation; }
        public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

        public int getWeatherCode() { return weatherCode; }
        public void setWeatherCode(int weatherCode) { this.weatherCode = weatherCode; }

        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

        public String getWeatherDescription() { return weatherDescription; }
        public void setWeatherDescription(String weatherDescription) { this.weatherDescription = weatherDescription; }

        public String getWeatherIcon() { return weatherIcon; }
        public void setWeatherIcon(String weatherIcon) { this.weatherIcon = weatherIcon; }

        public String getFormattedTemp() {
            return String.format("%.1f°C / %.1f°C", temperatureMin, temperatureMax);
        }

        @Override
        public String toString() {
            return weatherIcon + " " + weatherDescription + " - " + getFormattedTemp();
        }
    }
}