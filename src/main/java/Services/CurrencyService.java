package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Services/CurrencyService.java
 *
 * Récupère la liste complète des devises supportées par ExchangeRate-API
 * et les formate pour l'affichage dans les ComboBox (ex: "EUR — Euro").
 * Utilise le même API_KEY que CurrencyConverter.
 */
public class CurrencyService {

    // Utilise la même clé API que CurrencyConverter
    private static final String API_KEY = "1e1736e18ab80fbd87699c58";
    private static final String CODES_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/codes";
    private static final int TIMEOUT_SECONDS = 10;

    private final HttpClient httpClient;

    // Cache des devises formatées
    private List<String> cachedFormattedCurrencies = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 3600_000; // 1 heure

    public CurrencyService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Retourne la liste complète des devises formatées "CODE — Nom".
     * Si le cache est valide, renvoie les données en cache.
     * Sinon, effectue un appel API (synchrone) pour les récupérer.
     *
     * @return Liste des devises formatées
     * @throws Exception si l'appel API échoue et qu'aucun cache n'est disponible
     */
    public List<String> getAllCurrenciesFormatted() throws Exception {
        // Vérifie si le cache est encore valide
        if (cachedFormattedCurrencies != null &&
                System.currentTimeMillis() - cacheTimestamp < CACHE_TTL_MS) {
            return cachedFormattedCurrencies;
        }

        // Appel API synchrone
        List<String> freshList = fetchCurrenciesFromApi();
        cachedFormattedCurrencies = freshList;
        cacheTimestamp = System.currentTimeMillis();
        return freshList;
    }

    /**
     * Version asynchrone pour ne pas bloquer l'UI.
     * Retourne un CompletableFuture avec la liste.
     */
    public CompletableFuture<List<String>> getAllCurrenciesFormattedAsync() {
        if (cachedFormattedCurrencies != null &&
                System.currentTimeMillis() - cacheTimestamp < CACHE_TTL_MS) {
            return CompletableFuture.completedFuture(cachedFormattedCurrencies);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> list = fetchCurrenciesFromApi();
                cachedFormattedCurrencies = list;
                cacheTimestamp = System.currentTimeMillis();
                return list;
            } catch (Exception e) {
                // En cas d'échec, retourne une liste par défaut
                System.err.println("[CurrencyService] Erreur API, utilisation fallback: " + e.getMessage());
                return getFallbackCurrencies();
            }
        });
    }

    /**
     * Appel effectif à l'API ExchangeRate-API /codes.
     */
    private List<String> fetchCurrenciesFromApi() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CODES_URL))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("API HTTP " + response.statusCode());
        }

        return parseCodesResponse(response.body());
    }

    /**
     * Parse la réponse JSON de l'endpoint /codes.
     * Format attendu : { "supported_codes": [ ["AED","UAE Dirham"], ... ] }
     */
    private List<String> parseCodesResponse(String json) {
        List<String> result = new ArrayList<>();

        // Recherche du tableau "supported_codes"
        int start = json.indexOf("\"supported_codes\":[");
        if (start == -1) {
            System.err.println("[CurrencyService] Format JSON inattendu (supported_codes manquant)");
            return getFallbackCurrencies();
        }

        start = json.indexOf("[", start + 18); // début du tableau externe
        int end = json.lastIndexOf("]"); // fin du tableau externe
        if (start == -1 || end == -1) {
            return getFallbackCurrencies();
        }

        String arrayContent = json.substring(start + 1, end);
        // Maintenant on a une chaîne comme : ["AED","UAE Dirham"],["AFN","Afghan Afghani"],...
        // On va la parser simplement (sans librairie JSON)
        int i = 0;
        while (i < arrayContent.length()) {
            // Chercher le début d'un sous-tableau : [
            int subStart = arrayContent.indexOf('[', i);
            if (subStart == -1) break;
            int subEnd = arrayContent.indexOf(']', subStart);
            if (subEnd == -1) break;

            String sub = arrayContent.substring(subStart + 1, subEnd);
            // sub contient "AED","UAE Dirham"
            String[] parts = sub.split(",");
            if (parts.length >= 2) {
                String code = parts[0].trim().replace("\"", "");
                String name = parts[1].trim().replace("\"", "");
                result.add(code + " — " + name);
            }
            i = subEnd + 1;
        }

        if (result.isEmpty()) {
            return getFallbackCurrencies();
        }
        return result;
    }

    /**
     * Liste de secours (50+ devises courantes) en cas d'indisponibilité de l'API.
     */
    private List<String> getFallbackCurrencies() {
        List<String> fallback = new ArrayList<>();
        fallback.add("AED — UAE Dirham");
        fallback.add("AFN — Afghan Afghani");
        fallback.add("ALL — Albanian Lek");
        fallback.add("AMD — Armenian Dram");
        fallback.add("ANG — Netherlands Antillian Guilder");
        fallback.add("AOA — Angolan Kwanza");
        fallback.add("ARS — Argentine Peso");
        fallback.add("AUD — Australian Dollar");
        fallback.add("AWG — Aruban Florin");
        fallback.add("AZN — Azerbaijani Manat");
        fallback.add("BAM — Bosnia and Herzegovina Mark");
        fallback.add("BBD — Barbados Dollar");
        fallback.add("BDT — Bangladeshi Taka");
        fallback.add("BGN — Bulgarian Lev");
        fallback.add("BHD — Bahraini Dinar");
        fallback.add("BIF — Burundian Franc");
        fallback.add("BMD — Bermudian Dollar");
        fallback.add("BND — Brunei Dollar");
        fallback.add("BOB — Bolivian Boliviano");
        fallback.add("BRL — Brazilian Real");
        fallback.add("BSD — Bahamian Dollar");
        fallback.add("BTN — Bhutanese Ngultrum");
        fallback.add("BWP — Botswana Pula");
        fallback.add("BYN — Belarusian Ruble");
        fallback.add("BZD — Belize Dollar");
        fallback.add("CAD — Canadian Dollar");
        fallback.add("CDF — Congolese Franc");
        fallback.add("CHF — Swiss Franc");
        fallback.add("CLP — Chilean Peso");
        fallback.add("CNY — Chinese Renminbi");
        fallback.add("COP — Colombian Peso");
        fallback.add("CRC — Costa Rican Colon");
        fallback.add("CUP — Cuban Peso");
        fallback.add("CVE — Cape Verdean Escudo");
        fallback.add("CZK — Czech Koruna");
        fallback.add("DJF — Djiboutian Franc");
        fallback.add("DKK — Danish Krone");
        fallback.add("DOP — Dominican Peso");
        fallback.add("DZD — Algerian Dinar");
        fallback.add("EGP — Egyptian Pound");
        fallback.add("ERN — Eritrean Nakfa");
        fallback.add("ETB — Ethiopian Birr");
        fallback.add("EUR — Euro");
        fallback.add("FJD — Fiji Dollar");
        fallback.add("FKP — Falkland Islands Pound");
        fallback.add("FOK — Faroese Króna");
        fallback.add("GBP — Pound Sterling");
        fallback.add("GEL — Georgian Lari");
        fallback.add("GGP — Guernsey Pound");
        fallback.add("GHS — Ghanaian Cedi");
        fallback.add("GIP — Gibraltar Pound");
        fallback.add("GMD — Gambian Dalasi");
        fallback.add("GNF — Guinean Franc");
        fallback.add("GTQ — Guatemalan Quetzal");
        fallback.add("GYD — Guyanese Dollar");
        fallback.add("HKD — Hong Kong Dollar");
        fallback.add("HNL — Honduran Lempira");
        fallback.add("HRK — Croatian Kuna");
        fallback.add("HTG — Haitian Gourde");
        fallback.add("HUF — Hungarian Forint");
        fallback.add("IDR — Indonesian Rupiah");
        fallback.add("ILS — Israeli New Shekel");
        fallback.add("IMP — Manx Pound");
        fallback.add("INR — Indian Rupee");
        fallback.add("IQD — Iraqi Dinar");
        fallback.add("IRR — Iranian Rial");
        fallback.add("ISK — Icelandic Króna");
        fallback.add("JEP — Jersey Pound");
        fallback.add("JMD — Jamaican Dollar");
        fallback.add("JOD — Jordanian Dinar");
        fallback.add("JPY — Japanese Yen");
        fallback.add("KES — Kenyan Shilling");
        fallback.add("KGS — Kyrgyzstani Som");
        fallback.add("KHR — Cambodian Riel");
        fallback.add("KID — Kiribati Dollar");
        fallback.add("KMF — Comorian Franc");
        fallback.add("KRW — South Korean Won");
        fallback.add("KWD — Kuwaiti Dinar");
        fallback.add("KYD — Cayman Islands Dollar");
        fallback.add("KZT — Kazakhstani Tenge");
        fallback.add("LAK — Lao Kip");
        fallback.add("LBP — Lebanese Pound");
        fallback.add("LKR — Sri Lanka Rupee");
        fallback.add("LRD — Liberian Dollar");
        fallback.add("LSL — Lesotho Loti");
        fallback.add("LYD — Libyan Dinar");
        fallback.add("MAD — Moroccan Dirham");
        fallback.add("MDL — Moldovan Leu");
        fallback.add("MGA — Malagasy Ariary");
        fallback.add("MKD — Macedonian Denar");
        fallback.add("MMK — Burmese Kyat");
        fallback.add("MNT — Mongolian Tögrög");
        fallback.add("MOP — Macanese Pataca");
        fallback.add("MRU — Mauritanian Ouguiya");
        fallback.add("MUR — Mauritian Rupee");
        fallback.add("MVR — Maldivian Rufiyaa");
        fallback.add("MWK — Malawian Kwacha");
        fallback.add("MXN — Mexican Peso");
        fallback.add("MYR — Malaysian Ringgit");
        fallback.add("MZN — Mozambican Metical");
        fallback.add("NAD — Namibian Dollar");
        fallback.add("NGN — Nigerian Naira");
        fallback.add("NIO — Nicaraguan Córdoba");
        fallback.add("NOK — Norwegian Krone");
        fallback.add("NPR — Nepalese Rupee");
        fallback.add("NZD — New Zealand Dollar");
        fallback.add("OMR — Omani Rial");
        fallback.add("PAB — Panamanian Balboa");
        fallback.add("PEN — Peruvian Sol");
        fallback.add("PGK — Papua New Guinean Kina");
        fallback.add("PHP — Philippine Peso");
        fallback.add("PKR — Pakistani Rupee");
        fallback.add("PLN — Polish Złoty");
        fallback.add("PYG — Paraguayan Guaraní");
        fallback.add("QAR — Qatari Riyal");
        fallback.add("RON — Romanian Leu");
        fallback.add("RSD — Serbian Dinar");
        fallback.add("RUB — Russian Ruble");
        fallback.add("RWF — Rwandan Franc");
        fallback.add("SAR — Saudi Riyal");
        fallback.add("SBD — Solomon Islands Dollar");
        fallback.add("SCR — Seychellois Rupee");
        fallback.add("SDG — Sudanese Pound");
        fallback.add("SEK — Swedish Krona");
        fallback.add("SGD — Singapore Dollar");
        fallback.add("SHP — Saint Helena Pound");
        fallback.add("SLE — Sierra Leonean Leone");
        fallback.add("SOS — Somali Shilling");
        fallback.add("SRD — Surinamese Dollar");
        fallback.add("SSP — South Sudanese Pound");
        fallback.add("STN — São Tomé and Príncipe Dobra");
        fallback.add("SYP — Syrian Pound");
        fallback.add("SZL — Swazi Lilangeni");
        fallback.add("THB — Thai Baht");
        fallback.add("TJS — Tajikistani Somoni");
        fallback.add("TMT — Turkmenistani Manat");
        fallback.add("TND — Tunisian Dinar");
        fallback.add("TOP — Tongan Paʻanga");
        fallback.add("TRY — Turkish Lira");
        fallback.add("TTD — Trinidad and Tobago Dollar");
        fallback.add("TVD — Tuvaluan Dollar");
        fallback.add("TWD — New Taiwan Dollar");
        fallback.add("TZS — Tanzanian Shilling");
        fallback.add("UAH — Ukrainian Hryvnia");
        fallback.add("UGX — Ugandan Shilling");
        fallback.add("USD — US Dollar");
        fallback.add("UYU — Uruguayan Peso");
        fallback.add("UZS — Uzbekistani Soʻm");
        fallback.add("VES — Venezuelan Bolívar Soberano");
        fallback.add("VND — Vietnamese Đồng");
        fallback.add("VUV — Vanuatu Vatu");
        fallback.add("WST — Samoan Tālā");
        fallback.add("XAF — Central African CFA Franc");
        fallback.add("XCD — East Caribbean Dollar");
        fallback.add("XDR — Special Drawing Rights");
        fallback.add("XOF — West African CFA Franc");
        fallback.add("XPF — CFP Franc");
        fallback.add("YER — Yemeni Rial");
        fallback.add("ZAR — South African Rand");
        fallback.add("ZMW — Zambian Kwacha");
        fallback.add("ZWL — Zimbabwean Dollar");
        return fallback;
    }

    /**
     * Extrait le code devise à partir d'une chaîne formatée "EUR — Euro".
     *
     * @param formatted Chaîne formatée (peut être aussi simplement "EUR")
     * @return Le code devise (ex: "EUR")
     */
    public static String extractCode(String formatted) {
        if (formatted == null || formatted.isBlank()) return "";
        // Si c'est déjà un code simple (pas de " — "), on le retourne tel quel
        if (!formatted.contains(" — ")) {
            return formatted.trim();
        }
        // Sinon on prend la partie avant le séparateur
        return formatted.split(" — ")[0].trim();
    }

    /**
     * Vide le cache pour forcer un rechargement au prochain appel.
     */
    public void clearCache() {
        cachedFormattedCurrencies = null;
        cacheTimestamp = 0;
    }
}