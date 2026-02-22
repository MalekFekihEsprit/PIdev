package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour récupérer les données d'inflation depuis FRED API
 * (Federal Reserve Economic Data)
 */
public class FREDInflationService {

    // Remplacez par VOTRE clé API obtenue sur fred.stlouisfed.org
    private static final String API_KEY = "VOTRE_CLE_API_FRED";
    private static final String BASE_URL = "https://api.stlouisfed.org/fred/series/observations";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Mapping des codes pays vers les séries FRED (CPI)
    private static final Map<String, String> COUNTRY_SERIES = new HashMap<>();

    static {
        // Europe
        COUNTRY_SERIES.put("FR", "FRACPIALLMINMEI");  // France
        COUNTRY_SERIES.put("DE", "DEUCPIALLMINMEI");  // Allemagne
        COUNTRY_SERIES.put("IT", "ITACPIALLMINMEI");  // Italie
        COUNTRY_SERIES.put("ES", "ESPCPIALLMINMEI");  // Espagne
        COUNTRY_SERIES.put("GB", "GBRCPIALLMINMEI");  // Royaume-Uni
        COUNTRY_SERIES.put("BE", "BELCPIALLMINMEI");  // Belgique
        COUNTRY_SERIES.put("NL", "NLDCPIALLMINMEI");  // Pays-Bas

        // Amérique
        COUNTRY_SERIES.put("US", "CPIAUCSL");         // États-Unis
        COUNTRY_SERIES.put("CA", "CANCPIALLMINMEI");  // Canada
        COUNTRY_SERIES.put("MX", "MEXCPIALLMINMEI");  // Mexique
        COUNTRY_SERIES.put("BR", "BRACPIALLMINMEI");  // Brésil

        // Asie
        COUNTRY_SERIES.put("JP", "JPNCPIALLMINMEI");  // Japon
        COUNTRY_SERIES.put("CN", "CHNCPIALLMINMEI");  // Chine
        COUNTRY_SERIES.put("KR", "KORCPIALLMINMEI");  // Corée du Sud
        COUNTRY_SERIES.put("IN", "INDCPIALLMINMEI");  // Inde

        // Afrique
        COUNTRY_SERIES.put("ZA", "ZAFCPIALLMINMEI");  // Afrique du Sud
        COUNTRY_SERIES.put("MA", "MARCPIALLMINMEI");  // Maroc
        COUNTRY_SERIES.put("TN", "TUNCPIALLMINMEI");  // Tunisie
        COUNTRY_SERIES.put("EG", "EGYCPIALLMINMEI");  // Égypte

        // Océanie
        COUNTRY_SERIES.put("AU", "AUSCPIALLMINMEI");  // Australie
        COUNTRY_SERIES.put("NZ", "NZLCPIALLMINMEI");  // Nouvelle-Zélande
    }

    // Taux d'inflation par défaut si l'API échoue (fallback)
    private static final Map<String, Double> FALLBACK_RATES = new HashMap<>();

    static {
        FALLBACK_RATES.put("FR", 2.1);
        FALLBACK_RATES.put("US", 2.5);
        FALLBACK_RATES.put("GB", 2.3);
        FALLBACK_RATES.put("JP", 0.8);
        FALLBACK_RATES.put("TN", 4.2);
        FALLBACK_RATES.put("DE", 1.9);
        FALLBACK_RATES.put("IT", 2.0);
        FALLBACK_RATES.put("ES", 2.2);
        FALLBACK_RATES.put("CA", 2.1);
        FALLBACK_RATES.put("AU", 2.3);
    }

    /**
     * Calcule la valeur ajustée avec l'inflation entre deux dates
     * @param montantInitial Montant original
     * @param paysCode Code pays (FR, US, TN, etc.)
     * @param dateDebut Date de début (yyyy-MM-dd)
     * @param dateFin Date de fin (yyyy-MM-dd)
     * @return Montant ajusté avec l'inflation
     */
    public double calculerValeurAjustee(double montantInitial, String paysCode,
                                        String dateDebut, String dateFin) {
        try {
            // Étape 1: Récupérer l'ID de série FRED pour le pays
            String seriesId = COUNTRY_SERIES.get(paysCode.toUpperCase());
            if (seriesId == null) {
                System.out.println("⚠️ Pays non supporté: " + paysCode + ", utilisation taux par défaut");
                return calculerFallback(montantInitial, paysCode, dateDebut, dateFin);
            }

            // Étape 2: Récupérer le CPI pour la date de début
            double cpiDebut = getCPIForDate(seriesId, dateDebut);

            // Étape 3: Récupérer le CPI pour la date de fin
            double cpiFin = getCPIForDate(seriesId, dateFin);

            // Étape 4: Calculer le facteur d'inflation
            if (cpiDebut > 0 && cpiFin > 0) {
                double facteurInflation = cpiFin / cpiDebut;
                double valeurAjustee = montantInitial * facteurInflation;

                System.out.println(String.format(
                        "✅ Inflation calculée: CPI %.2f → %.2f, facteur: %.3f",
                        cpiDebut, cpiFin, facteurInflation
                ));

                return valeurAjustee;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur API FRED: " + e.getMessage());
            e.printStackTrace();
        }

        // Fallback: utiliser taux d'inflation moyen
        return calculerFallback(montantInitial, paysCode, dateDebut, dateFin);
    }

    /**
     * Récupère le CPI pour une date spécifique
     */
    private double getCPIForDate(String seriesId, String date) throws Exception {
        // Construction de l'URL
        String url = String.format(
                "%s?series_id=%s&api_key=%s&observation_date=%s&file_type=json",
                BASE_URL, seriesId, API_KEY, date
        );

        // Création du client HTTP
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();

        // Envoi de la requête
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // Parsing de la réponse JSON
        String jsonResponse = response.body();
        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray observations = root.getAsJsonArray("observations");

        if (observations != null && observations.size() > 0) {
            JsonObject observation = observations.get(0).getAsJsonObject();
            String value = observation.get("value").getAsString();

            // "." signifie donnée manquante
            if (!value.equals(".")) {
                return Double.parseDouble(value);
            }
        }

        return 0;
    }

    /**
     * Calcul de fallback avec taux d'inflation moyen
     */
    private double calculerFallback(double montant, String paysCode,
                                    String dateDebut, String dateFin) {
        // Taux d'inflation annuel par défaut
        double tauxAnnuel = FALLBACK_RATES.getOrDefault(paysCode.toUpperCase(), 2.0);

        // Calculer le nombre d'années entre les dates
        LocalDate debut = LocalDate.parse(dateDebut);
        LocalDate fin = LocalDate.parse(dateFin);

        long jours = java.time.temporal.ChronoUnit.DAYS.between(debut, fin);
        double annees = jours / 365.0;

        // Formule d'intérêt composé
        double facteur = Math.pow(1 + (tauxAnnuel / 100), annees);

        System.out.println(String.format(
                "⚠️ Fallback: taux %.1f%%, facteur %.3f", tauxAnnuel, facteur
        ));

        return montant * facteur;
    }

    /**
     * Calcule pour une période d'un an (utile pour Time Capsule)
     */
    public double calculerInflationUnAn(double montantInitial, String paysCode,
                                        String dateDebut) {
        LocalDate debut = LocalDate.parse(dateDebut);
        LocalDate fin = debut.plusYears(1);

        return calculerValeurAjustee(
                montantInitial,
                paysCode,
                dateDebut,
                fin.format(DATE_FORMAT)
        );
    }

    /**
     * Vérifie si un pays est supporté par l'API
     */
    public static boolean isCountrySupported(String paysCode) {
        return COUNTRY_SERIES.containsKey(paysCode.toUpperCase());
    }

    /**
     * Liste tous les pays supportés
     */
    public static Map<String, String> getSupportedCountries() {
        return new HashMap<>(COUNTRY_SERIES);
    }
}