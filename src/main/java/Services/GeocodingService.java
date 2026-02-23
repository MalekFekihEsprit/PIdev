package Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class GeocodingService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "TravelMate/1.0";

    private static final Map<String, LocationResult> CACHE = new HashMap<>();

    static {
        // Cache de secours
        LocationResult sidiBouSaid = new LocationResult();
        sidiBouSaid.setLatitude(36.8710935);
        sidiBouSaid.setLongitude(10.3490516);
        sidiBouSaid.setCity("Sidi Bou Saïd");
        sidiBouSaid.setCountry("Tunisie");
        sidiBouSaid.setDisplayName("Sidi Bou Saïd, Tunisie");
        CACHE.put("sidi bou said, tunisie", sidiBouSaid);
        CACHE.put("sidi bou said", sidiBouSaid);
    }

    public static LocationResult geocode(String address) {
        try {
            if (address == null || address.trim().isEmpty()) {
                System.out.println("❌ Adresse vide");
                return null;
            }

            String normalizedAddress = address.toLowerCase().trim();
            System.out.println("🔍 Géocodage: '" + address + "'");

            // Appel DIRECT à l'API (sans cache pour le test)
            System.out.println("  ⏳ Appel API...");
            LocationResult apiResult = callNominatimAPI(address);

            if (apiResult != null) {
                System.out.println("  ✅ SUCCÈS API - Lat: " + apiResult.getLatitude() + ", Lon: " + apiResult.getLongitude());
                System.out.println("  ✅ Ville: " + apiResult.getCity() + ", Pays: " + apiResult.getCountry());
                return apiResult;
            }

            // Si API échoue, essayer le cache
            if (CACHE.containsKey(normalizedAddress)) {
                System.out.println("  ✅ Utilisation du cache pour: " + address);
                return CACHE.get(normalizedAddress);
            }

            System.out.println("  ❌ Aucun résultat pour: '" + address + "'");
            return null;

        } catch (Exception e) {
            System.err.println("  ❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static LocationResult callNominatimAPI(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlStr = NOMINATIM_URL + "?q=" + encodedAddress +
                    "&format=json&addressdetails=1&limit=1";

            System.out.println("     URL: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "fr");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int responseCode = conn.getResponseCode();
            System.out.println("     Code réponse: " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String json = response.toString();
            System.out.println("     Réponse reçue, longueur: " + json.length() + " caractères");

            if (json.equals("[]") || json.length() < 10) {
                System.out.println("     ⚠️ Aucun résultat");
                return null;
            }

            // Parser le JSON
            return parseJsonResponse(json);

        } catch (Exception e) {
            System.err.println("  ⚠️ Erreur API: " + e.getMessage());
            return null;
        }
    }

    private static LocationResult parseJsonResponse(String json) {
        try {
            LocationResult result = new LocationResult();

            // La réponse est un tableau JSON: [ {...} ]
            // On extrait le premier objet

            // Extraire latitude - Format dans la réponse: "lat":"36.8710935"
            int latIndex = json.indexOf("\"lat\":\"");
            if (latIndex > 0) {
                int latStart = latIndex + 7;
                int latEnd = json.indexOf("\"", latStart);
                String latStr = json.substring(latStart, latEnd);
                result.setLatitude(Double.parseDouble(latStr));
                System.out.println("     Latitude extraite: " + latStr);
            }

            // Extraire longitude - Format dans la réponse: "lon":"10.3490516"
            int lonIndex = json.indexOf("\"lon\":\"");
            if (lonIndex > 0) {
                int lonStart = lonIndex + 7;
                int lonEnd = json.indexOf("\"", lonStart);
                String lonStr = json.substring(lonStart, lonEnd);
                result.setLongitude(Double.parseDouble(lonStr));
                System.out.println("     Longitude extraite: " + lonStr);
            }

            // Extraire display_name
            int nameIndex = json.indexOf("\"display_name\":\"");
            if (nameIndex > 0) {
                int nameStart = nameIndex + 16;
                int nameEnd = json.indexOf("\"", nameStart);
                result.setDisplayName(json.substring(nameStart, nameEnd));
            }

            // Chercher la ville dans l'objet address s'il existe
            int addrIndex = json.indexOf("\"address\":{");
            if (addrIndex > 0) {
                int addrEnd = json.indexOf("}", addrIndex);
                String addrSection = json.substring(addrIndex, addrEnd);

                // Extraire ville
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

                // Extraire pays
                if (addrSection.contains("\"country\":")) {
                    int countryStart = addrSection.indexOf("\"country\":\"") + 11;
                    int countryEnd = addrSection.indexOf("\"", countryStart);
                    result.setCountry(addrSection.substring(countryStart, countryEnd));
                }
            }

            // Si on n'a pas trouvé la ville dans address, utiliser le display_name
            if ((result.getCity() == null || result.getCity().isEmpty()) && result.getDisplayName() != null) {
                String[] parts = result.getDisplayName().split(",");
                if (parts.length > 0) {
                    result.setCity(parts[0].trim());
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("  ⚠️ Erreur parsing JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static class LocationResult {
        private double latitude;
        private double longitude;
        private String displayName;
        private String city;
        private String country;
        private String postcode;
        private String street;

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getCity() { return city != null ? city : ""; }
        public void setCity(String city) { this.city = city; }

        public String getCountry() { return country != null ? country : ""; }
        public void setCountry(String country) { this.country = country; }

        public String getPostcode() { return postcode != null ? postcode : ""; }
        public void setPostcode(String postcode) { this.postcode = postcode; }

        public String getStreet() { return street != null ? street : ""; }
        public void setStreet(String street) { this.street = street; }
    }
}