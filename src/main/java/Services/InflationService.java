package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class InflationService {

    private static final String FRED_API_KEY = "InflationBudget"; // Remplacez par votre clé
    private static final String BASE_URL = "https://api.stlouisfed.org/fred/series/observations";
    private static final int TIMEOUT_SECONDS = 10;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public InflationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Récupère l'IPC annuel moyen pour une série et une année.
     *
     * @param seriesId Identifiant FRED de la série (ex: "CPALTT01FRM657N" pour France)
     * @param year Année (ex: 2020)
     * @return CompletableFuture contenant la moyenne annuelle ou 0.0 en cas d'erreur
     */
    public CompletableFuture<Double> getAnnualCPI(String seriesId, int year) {
        String url = String.format("%s?series_id=%s&api_key=%s&file_type=json&observation_start=%d-01-01&observation_end=%d-12-31",
                BASE_URL, seriesId, FRED_API_KEY, year, year);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseAnnualCPI(response.body());
                    } else {
                        System.err.println("[FRED API] Erreur " + response.statusCode() + ": " + response.body());
                        return 0.0;
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("[FRED API] Exception: " + ex.getMessage());
                    return 0.0;
                });
    }

    private double parseAnnualCPI(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode observations = root.path("observations");
            if (observations.isArray() && observations.size() > 0) {
                double sum = 0.0;
                int count = 0;
                for (JsonNode obs : observations) {
                    String valueStr = obs.path("value").asText();
                    if (!valueStr.equals(".")) {
                        sum += Double.parseDouble(valueStr);
                        count++;
                    }
                }
                if (count > 0) {
                    return sum / count;
                }
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("[FRED API] Erreur de parsing JSON: " + e.getMessage());
            return 0.0;
        }
    }
}