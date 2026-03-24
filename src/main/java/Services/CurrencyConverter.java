package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static Utils.ConfigV.dotenv;

/**
 * Services/CurrencyConverter.java
 * Convertit des montants entre devises via ExchangeRate-API (gratuit).
 *
 * Inscription gratuite : https://www.exchangerate-api.com
 */
public class CurrencyConverter {

    // ✅ Remplacez par votre clé API
    private static final String API_KEY = dotenv.get("CurrencyConverterBudget");
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private static final int TIMEOUT_SECONDS = 10;

    private final HttpClient httpClient;

    // Cache des taux pour éviter trop d'appels API
    private final Map<String, Double> ratesCache = new HashMap<>();
    private String cachedBaseCurrency = null;

    public CurrencyConverter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Convertit un montant d'une devise vers une autre.
     *
     * @param amount       Montant à convertir
     * @param fromCurrency Devise source (ex: "USD")
     * @param toCurrency   Devise cible (ex: "TND")
     * @return Montant converti, ou -1 en cas d'erreur
     */
    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) return amount;

        try {
            double rate = getRate(fromCurrency, toCurrency);
            if (rate > 0) {
                double result = amount * rate;
                System.out.printf("[CurrencyConverter] %.2f %s = %.2f %s (taux: %.4f)%n",
                        amount, fromCurrency, result, toCurrency, rate);
                return result;
            }
        } catch (Exception e) {
            System.err.println("[CurrencyConverter] Erreur de conversion: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Récupère le taux de change entre deux devises.
     * Utilise un cache pour éviter les appels répétés.
     */
    public double getRate(String fromCurrency, String toCurrency) throws Exception {
        String cacheKey = fromCurrency + "_" + toCurrency;

        // Retourne depuis le cache si disponible
        if (ratesCache.containsKey(cacheKey)) {
            return ratesCache.get(cacheKey);
        }

        // Appel API si base différente du cache
        if (!fromCurrency.equals(cachedBaseCurrency)) {
            fetchRates(fromCurrency);
        }

        return ratesCache.getOrDefault(cacheKey, -1.0);
    }

    /**
     * Récupère tous les taux depuis l'API pour une devise de base.
     */
    private void fetchRates(String baseCurrency) throws Exception {
        String url = BASE_URL + API_KEY + "/latest/" + baseCurrency.toUpperCase();
        System.out.println("[CurrencyConverter] Appel API: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            parseRates(baseCurrency, response.body());
            cachedBaseCurrency = baseCurrency;
            System.out.println("[CurrencyConverter] Taux chargés pour: " + baseCurrency);
        } else {
            throw new Exception("API Error: HTTP " + response.statusCode());
        }
    }

    /**
     * Parse la réponse JSON de l'API (sans librairie externe).
     * Format attendu: {"conversion_rates":{"EUR":0.92,"USD":1.0,...}}
     */
    private void parseRates(String baseCurrency, String json) {
        ratesCache.clear();

        // Extraction simple du bloc "conversion_rates"
        int start = json.indexOf("\"conversion_rates\":{");
        if (start == -1) {
            System.err.println("[CurrencyConverter] Format JSON inattendu");
            return;
        }

        start = json.indexOf("{", start + 19);
        int end = json.indexOf("}", start);
        String ratesBlock = json.substring(start + 1, end);

        // Parse chaque paire "DEVISE":valeur
        String[] pairs = ratesBlock.split(",");
        for (String pair : pairs) {
            try {
                String[] parts = pair.trim().split(":");
                String currency = parts[0].trim().replace("\"", "");
                double rate = Double.parseDouble(parts[1].trim());
                ratesCache.put(baseCurrency + "_" + currency, rate);
            } catch (Exception e) {
                // Ignore les entrées mal formées
            }
        }
    }

    /**
     * Vide le cache (utile pour forcer la mise à jour des taux).
     */
    public void clearCache() {
        ratesCache.clear();
        cachedBaseCurrency = null;
        System.out.println("[CurrencyConverter] Cache vidé.");
    }
}