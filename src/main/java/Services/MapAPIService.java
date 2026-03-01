package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MapAPIService {

    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "TravelMateApp/1.0";
    private final OkHttpClient client;

    public MapAPIService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Géocode un lieu pour obtenir ses coordonnées GPS
     */
    public GeoPoint geocode(String lieu) throws IOException {
        if (lieu == null || lieu.trim().isEmpty()) {
            return null;
        }

        String url = String.format("%s?q=%s&format=json&limit=1",
                NOMINATIM_API, java.net.URLEncoder.encode(lieu, "UTF-8"));

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur API: " + response.code());
            }

            String jsonData = response.body().string();
            JsonArray results = JsonParser.parseString(jsonData).getAsJsonArray();

            if (results.size() > 0) {
                JsonObject first = results.get(0).getAsJsonObject();
                double lat = first.get("lat").getAsDouble();
                double lon = first.get("lon").getAsDouble();
                String displayName = first.get("display_name").getAsString();

                return new GeoPoint(lat, lon, displayName);
            }
        }

        return null;
    }

    /**
     * Génère une URL pour une carte statique (optionnel)
     */
    public String getStaticMapUrl(double lat, double lon, int zoom, int width, int height) {
        return String.format("https://staticmap.openstreetmap.de/staticmap.php?center=%f,%f&zoom=%d&size=%dx%d&maptype=mapnik",
                lat, lon, zoom, width, height);
    }

    /**
     * Génère une URL pour OpenStreetMap (version interactive)
     */
    public String getOpenStreetMapUrl(double lat, double lon, int zoom) {
        return String.format("https://www.openstreetmap.org/?mlat=%f&mlon=%f#map=%d/%f/%f",
                lat, lon, zoom, lat, lon);
    }

    /**
     * Génère une URL pour Google Maps
     */
    public String getGoogleMapsUrl(double lat, double lon) {
        return String.format("https://www.google.com/maps?q=%f,%f", lat, lon);
    }

    /**
     * Classe représentant un point géographique
     */
    public static class GeoPoint {
        private final double latitude;
        private final double longitude;
        private final String displayName;

        public GeoPoint(double latitude, double longitude, String displayName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayName = displayName;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() {
            return String.format("%f, %f", latitude, longitude);
        }
    }
}