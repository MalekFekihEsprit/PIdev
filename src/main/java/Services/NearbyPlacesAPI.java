package Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyPlacesAPI {

    private static final String OVERPASS_API = "https://overpass-api.de/api/interpreter";

    // RÉDUIRE le nombre de types pour accélérer la requête
    private static final String[][] PLACE_TYPES = {
            {"tourism", "museum", "Musée"},
            {"tourism", "attraction", "Attraction"},
            {"amenity", "restaurant", "Restaurant"},
            {"amenity", "cafe", "Café"},
            {"leisure", "park", "Parc"},
            {"historic", "monument", "Monument"}
            // J'ai enlevé shop, cinema, hotel, bar, etc. pour aller plus vite
    };

    public static List<Map<String, Object>> findNearbyPlaces(double lat, double lon, int radius) {
        List<Map<String, Object>> places = new ArrayList<>();

        try {
            StringBuilder query = new StringBuilder();
            query.append("[out:json];(");

            for (String[] type : PLACE_TYPES) {
                query.append("node[\"").append(type[0]).append("\"=\"").append(type[1]).append("\"]")
                        .append("(around:").append(radius).append(",").append(lat).append(",").append(lon).append(");");
            }

            query.append(");out body;");

            String encodedQuery = URLEncoder.encode(query.toString(), "UTF-8");
            String urlStr = OVERPASS_API + "?data=" + encodedQuery;

            System.out.println("🔍 Appel API Overpass (timeout 15s)...");

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "TravelMate/1.0");
            conn.setConnectTimeout(15000); // 15 secondes
            conn.setReadTimeout(15000);     // 15 secondes

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                places = parseOverpassResponse(response.toString(), lat, lon);
                System.out.println("✅ API Overpass a retourné " + places.size() + " résultats");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur API Overpass: " + e.getMessage());
            // Ne pas activer le mode simulation automatiquement
        }

        return places;
    }

    private static List<Map<String, Object>> parseOverpassResponse(String json, double centerLat, double centerLon) {
        List<Map<String, Object>> places = new ArrayList<>();

        try {
            if (json == null || json.length() < 10) {
                return places;
            }

            // Parsing simplifié
            String[] elements = json.split("\\},\\{");

            for (String element : elements) {
                if (element.contains("\"tags\":") && element.contains("\"name\":")) {
                    Map<String, Object> place = new HashMap<>();

                    // Extraire le nom
                    int nameIdx = element.indexOf("\"name\":\"");
                    if (nameIdx > 0) {
                        int nameStart = nameIdx + 8;
                        int nameEnd = element.indexOf("\"", nameStart);
                        if (nameEnd > nameStart) {
                            String name = element.substring(nameStart, nameEnd);
                            place.put("name", name);
                        }
                    }

                    if (!place.containsKey("name")) continue;

                    // Extraire les coordonnées
                    double lat = centerLat, lon = centerLon;

                    int latIdx = element.indexOf("\"lat\":");
                    if (latIdx > 0) {
                        int latStart = latIdx + 6;
                        int latEnd = element.indexOf(",", latStart);
                        if (latEnd == -1) latEnd = element.indexOf("}", latStart);
                        try {
                            lat = Double.parseDouble(element.substring(latStart, latEnd));
                        } catch (Exception e) {}
                    }

                    int lonIdx = element.indexOf("\"lon\":");
                    if (lonIdx > 0) {
                        int lonStart = lonIdx + 6;
                        int lonEnd = element.indexOf(",", lonStart);
                        if (lonEnd == -1) lonEnd = element.indexOf("}", lonStart);
                        try {
                            lon = Double.parseDouble(element.substring(lonStart, lonEnd));
                        } catch (Exception e) {}
                    }

                    place.put("lat", lat);
                    place.put("lon", lon);

                    // Calculer la distance
                    double distance = calculateDistance(centerLat, centerLon, lat, lon);
                    place.put("distance", distance);

                    // Déterminer le type
                    String type = "attraction";
                    if (element.contains("museum")) type = "museum";
                    else if (element.contains("restaurant")) type = "restaurant";
                    else if (element.contains("cafe")) type = "cafe";
                    else if (element.contains("park")) type = "park";
                    else if (element.contains("monument")) type = "monument";

                    place.put("type", type);

                    places.add(place);
                }
            }

            places.sort((p1, p2) -> Double.compare(
                    (Double) p1.get("distance"),
                    (Double) p2.get("distance")));

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing: " + e.getMessage());
        }

        return places;
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 100) / 100.0;
    }
}