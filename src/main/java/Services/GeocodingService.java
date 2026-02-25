package Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class GeocodingService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org";
    private static final String USER_AGENT = "TravelMate/1.0";
    private static final Map<String, LocationResult> CACHE = new HashMap<>();

    static {
        // Cache de secours pour les tests
        LocationResult sidiBouSaid = new LocationResult();
        sidiBouSaid.setLatitude(36.8710935);
        sidiBouSaid.setLongitude(10.3490516);
        sidiBouSaid.setCity("Sidi Bou Saïd");
        sidiBouSaid.setCountry("Tunisie");
        sidiBouSaid.setDisplayName("Sidi Bou Saïd, Tunisie");
        CACHE.put("sidi bou said, tunisie", sidiBouSaid);
        CACHE.put("sidi bou said", sidiBouSaid);

        LocationResult tunis = new LocationResult();
        tunis.setLatitude(36.8065);
        tunis.setLongitude(10.1815);
        tunis.setCity("Tunis");
        tunis.setCountry("Tunisie");
        tunis.setDisplayName("Tunis, Tunisie");
        CACHE.put("tunis, tunisie", tunis);
        CACHE.put("tunis", tunis);

        LocationResult hammamet = new LocationResult();
        hammamet.setLatitude(36.4);
        hammamet.setLongitude(10.6167);
        hammamet.setCity("Hammamet");
        hammamet.setCountry("Tunisie");
        hammamet.setDisplayName("Hammamet, Tunisie");
        CACHE.put("hammamet, tunisie", hammamet);
        CACHE.put("hammamet", hammamet);
    }

    /**
     * Géocode une adresse (convertit une adresse en coordonnées)
     */
    public static LocationResult geocode(String address) {
        try {
            if (address == null || address.trim().isEmpty()) {
                System.out.println("❌ Adresse vide");
                return null;
            }

            String normalizedAddress = address.toLowerCase().trim();
            System.out.println("🔍 Géocodage: '" + address + "'");

            // Vérifier le cache d'abord
            if (CACHE.containsKey(normalizedAddress)) {
                System.out.println("  ✅ Utilisation du cache pour: " + address);
                return CACHE.get(normalizedAddress);
            }

            // Appel API
            LocationResult apiResult = callNominatimSearch(address);

            if (apiResult != null) {
                // Mettre en cache
                CACHE.put(normalizedAddress, apiResult);
                System.out.println("  ✅ SUCCÈS API - Lat: " + apiResult.getLatitude() + ", Lon: " + apiResult.getLongitude());
                return apiResult;
            }

            System.out.println("  ❌ Aucun résultat pour: '" + address + "'");
            return null;

        } catch (Exception e) {
            System.err.println("  ❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Géocodage inverse (convertit des coordonnées en adresse)
     */
    public static LocationResult reverseGeocode(double latitude, double longitude) {
        try {
            String urlStr = NOMINATIM_URL + "/reverse?format=json&lat=" + latitude +
                    "&lon=" + longitude + "&addressdetails=1&accept-language=fr";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "fr");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return parseReverseGeocodeResponse(response.toString());

        } catch (Exception e) {
            System.err.println("❌ Erreur reverse geocoding: " + e.getMessage());
            return null;
        }
    }

    /**
     * Appelle l'API Nominatim pour la recherche
     */
    private static LocationResult callNominatimSearch(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlStr = NOMINATIM_URL + "/search?q=" + encodedAddress +
                    "&format=json&addressdetails=1&limit=1&accept-language=fr";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "fr");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("  ⚠️ Code réponse: " + responseCode);
                return null;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String json = response.toString();

            if (json.equals("[]") || json.length() < 10) {
                return null;
            }

            return parseSearchResponse(json);

        } catch (Exception e) {
            System.err.println("  ⚠️ Erreur API: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse la réponse de recherche
     */
    private static LocationResult parseSearchResponse(String json) {
        try {
            LocationResult result = new LocationResult();

            // Extraire latitude
            int latIndex = json.indexOf("\"lat\":\"");
            if (latIndex > 0) {
                int latStart = latIndex + 7;
                int latEnd = json.indexOf("\"", latStart);
                String latStr = json.substring(latStart, latEnd);
                result.setLatitude(Double.parseDouble(latStr));
            }

            // Extraire longitude
            int lonIndex = json.indexOf("\"lon\":\"");
            if (lonIndex > 0) {
                int lonStart = lonIndex + 7;
                int lonEnd = json.indexOf("\"", lonStart);
                String lonStr = json.substring(lonStart, lonEnd);
                result.setLongitude(Double.parseDouble(lonStr));
            }

            // Extraire display_name
            int nameIndex = json.indexOf("\"display_name\":\"");
            if (nameIndex > 0) {
                int nameStart = nameIndex + 16;
                int nameEnd = json.indexOf("\"", nameStart);
                result.setDisplayName(json.substring(nameStart, nameEnd));
            }

            // Extraire les détails de l'adresse
            int addrIndex = json.indexOf("\"address\":{");
            if (addrIndex > 0) {
                int addrEnd = json.indexOf("}", addrIndex);
                String addrSection = json.substring(addrIndex, addrEnd);

                // Ville
                if (addrSection.contains("\"city\":")) {
                    int cityStart = addrSection.indexOf("\"city\":\"") + 8;
                    int cityEnd = addrSection.indexOf("\"", cityStart);
                    result.setCity(addrSection.substring(cityStart, cityEnd));
                } else if (addrSection.contains("\"town\":")) {
                    int townStart = addrSection.indexOf("\"town\":\"") + 8;
                    int townEnd = addrSection.indexOf("\"", townStart);
                    result.setCity(addrSection.substring(townStart, townEnd));
                } else if (addrSection.contains("\"village\":")) {
                    int villageStart = addrSection.indexOf("\"village\":\"") + 11;
                    int villageEnd = addrSection.indexOf("\"", villageStart);
                    result.setCity(addrSection.substring(villageStart, villageEnd));
                }

                // Pays
                if (addrSection.contains("\"country\":")) {
                    int countryStart = addrSection.indexOf("\"country\":\"") + 11;
                    int countryEnd = addrSection.indexOf("\"", countryStart);
                    result.setCountry(addrSection.substring(countryStart, countryEnd));
                }

                // Code postal
                if (addrSection.contains("\"postcode\":")) {
                    int postStart = addrSection.indexOf("\"postcode\":\"") + 12;
                    int postEnd = addrSection.indexOf("\"", postStart);
                    result.setPostcode(addrSection.substring(postStart, postEnd));
                }

                // Rue
                if (addrSection.contains("\"road\":")) {
                    int roadStart = addrSection.indexOf("\"road\":\"") + 8;
                    int roadEnd = addrSection.indexOf("\"", roadStart);
                    result.setStreet(addrSection.substring(roadStart, roadEnd));
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("  ⚠️ Erreur parsing JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse la réponse de reverse geocoding
     */
    private static LocationResult parseReverseGeocodeResponse(String json) {
        try {
            LocationResult result = new LocationResult();

            // Extraire display_name
            int nameIndex = json.indexOf("\"display_name\":\"");
            if (nameIndex > 0) {
                int nameStart = nameIndex + 16;
                int nameEnd = json.indexOf("\"", nameStart);
                result.setDisplayName(json.substring(nameStart, nameEnd));
            }

            // Extraire les détails de l'adresse
            int addrIndex = json.indexOf("\"address\":{");
            if (addrIndex > 0) {
                int addrEnd = json.indexOf("}", addrIndex);
                String addrSection = json.substring(addrIndex, addrEnd);

                // Rue
                if (addrSection.contains("\"road\":")) {
                    int roadStart = addrSection.indexOf("\"road\":\"") + 8;
                    int roadEnd = addrSection.indexOf("\"", roadStart);
                    result.setStreet(addrSection.substring(roadStart, roadEnd));
                } else if (addrSection.contains("\"pedestrian\":")) {
                    int pedStart = addrSection.indexOf("\"pedestrian\":\"") + 14;
                    int pedEnd = addrSection.indexOf("\"", pedStart);
                    result.setStreet(addrSection.substring(pedStart, pedEnd));
                }

                // Ville
                if (addrSection.contains("\"city\":")) {
                    int cityStart = addrSection.indexOf("\"city\":\"") + 8;
                    int cityEnd = addrSection.indexOf("\"", cityStart);
                    result.setCity(addrSection.substring(cityStart, cityEnd));
                } else if (addrSection.contains("\"town\":")) {
                    int townStart = addrSection.indexOf("\"town\":\"") + 8;
                    int townEnd = addrSection.indexOf("\"", townStart);
                    result.setCity(addrSection.substring(townStart, townEnd));
                } else if (addrSection.contains("\"village\":")) {
                    int villageStart = addrSection.indexOf("\"village\":\"") + 11;
                    int villageEnd = addrSection.indexOf("\"", villageStart);
                    result.setCity(addrSection.substring(villageStart, villageEnd));
                }

                // Code postal
                if (addrSection.contains("\"postcode\":")) {
                    int postStart = addrSection.indexOf("\"postcode\":\"") + 12;
                    int postEnd = addrSection.indexOf("\"", postStart);
                    result.setPostcode(addrSection.substring(postStart, postEnd));
                }

                // Pays
                if (addrSection.contains("\"country\":")) {
                    int countryStart = addrSection.indexOf("\"country\":\"") + 11;
                    int countryEnd = addrSection.indexOf("\"", countryStart);
                    result.setCountry(addrSection.substring(countryStart, countryEnd));
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing reverse geocoding: " + e.getMessage());
            return null;
        }
    }

    /**
     * Classe interne pour stocker les résultats de localisation
     */
    public static class LocationResult {
        private double latitude;
        private double longitude;
        private String displayName;
        private String city;
        private String country;
        private String postcode;
        private String street;

        // Getters et Setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getDisplayName() { return displayName != null ? displayName : ""; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getCity() { return city != null ? city : ""; }
        public void setCity(String city) { this.city = city; }

        public String getCountry() { return country != null ? country : ""; }
        public void setCountry(String country) { this.country = country; }

        public String getPostcode() { return postcode != null ? postcode : ""; }
        public void setPostcode(String postcode) { this.postcode = postcode; }

        public String getStreet() { return street != null ? street : ""; }
        public void setStreet(String street) { this.street = street; }

        /**
         * Formate l'adresse complète
         */
        public String getFormattedAddress() {
            StringBuilder sb = new StringBuilder();
            if (street != null && !street.isEmpty()) sb.append(street);
            if (city != null && !city.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(city);
            }
            if (postcode != null && !postcode.isEmpty()) {
                if (sb.length() > 0 && city != null && !city.isEmpty()) sb.append(" ");
                else if (sb.length() > 0) sb.append(", ");
                sb.append(postcode);
            }
            if (country != null && !country.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(country);
            }
            return sb.length() > 0 ? sb.toString() : displayName;
        }

        @Override
        public String toString() {
            return getFormattedAddress();
        }
    }
}