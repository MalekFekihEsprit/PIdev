package Services;

import Utils.Config;
import Utils.ConfigV;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.image.Image;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.TimeUnit;

/**
 * Service de récupération de photos de destinations via l'API Unsplash.
 * Nécessite une clé d'accès gratuite (UNSPLASH_ACCESS_KEY dans .env).
 * Limite gratuite : 50 requêtes/heure.
 */
public class UnsplashService {

    private static final String BASE_URL = "https://api.unsplash.com";
    private final OkHttpClient client;

    public UnsplashService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Vérifie si le service Unsplash est configuré.
     */
    public boolean isConfigured() {
        return ConfigV.isUnsplashConfigured();
    }

    /**
     * Recherche une photo de destination et retourne une Image JavaFX.
     *
     * @param destination le nom de la destination (ex: "Paris", "Marrakech")
     * @return une Image JavaFX ou null si non trouvée
     */
    public Image rechercherPhotoDestination(String destination) {
        String url = getPhotoUrl(destination);
        if (url == null) return null;

        try {
            return new Image(url, 400, 250, true, true, true);
        } catch (Exception e) {
            System.out.println("⚠️ Erreur chargement image Unsplash: " + e.getMessage());
            return null;
        }
    }

    /**
     * Recherche une photo et retourne directement l'URL de l'image.
     *
     * @param destination le nom de la destination
     * @return l'URL de l'image (taille regular) ou null
     */
    public String getPhotoUrl(String destination) {
        if (!isConfigured()) {
            System.out.println("⚠️ Unsplash non configuré. Ajoutez UNSPLASH_ACCESS_KEY dans .env");
            return null;
        }

        if (destination == null || destination.trim().isEmpty()) return null;

        try {
            String query = destination.trim().replace(" ", "%20") + "%20travel%20landscape";
            String url = BASE_URL + "/search/photos?query=" + query
                    + "&per_page=1&orientation=landscape&content_filter=high";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Client-ID " + ConfigV.UNSPLASH_ACCESS_KEY)
                    .addHeader("Accept-Version", "v1")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    System.out.println("⚠️ Unsplash API erreur: " + response.code());
                    return null;
                }

                String body = response.body().string();
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                JsonArray results = json.getAsJsonArray("results");

                if (results == null || results.isEmpty()) {
                    return null;
                }

                JsonObject photo = results.get(0).getAsJsonObject();
                JsonObject urls = photo.getAsJsonObject("urls");

                // Retourner l'URL "regular" (1080px) pour un bon rapport qualité/taille
                return urls.has("regular") ? urls.get("regular").getAsString() : null;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Unsplash API erreur: " + e.getMessage());
            return null;
        }
    }

    /**
     * Récupère des infos complètes sur la photo (auteur, description, etc.)
     */
    public PhotoInfo rechercherPhotoInfo(String destination) {
        if (!isConfigured() || destination == null || destination.trim().isEmpty()) return null;

        try {
            String query = destination.trim().replace(" ", "%20") + "%20travel%20landscape";
            String url = BASE_URL + "/search/photos?query=" + query
                    + "&per_page=1&orientation=landscape&content_filter=high";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Client-ID " + ConfigV.UNSPLASH_ACCESS_KEY)
                    .addHeader("Accept-Version", "v1")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return null;

                String body = response.body().string();
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                JsonArray results = json.getAsJsonArray("results");

                if (results == null || results.isEmpty()) return null;

                JsonObject photo = results.get(0).getAsJsonObject();
                PhotoInfo info = new PhotoInfo();

                JsonObject urls = photo.getAsJsonObject("urls");
                info.urlRegular = urls.has("regular") ? urls.get("regular").getAsString() : "";
                info.urlSmall = urls.has("small") ? urls.get("small").getAsString() : "";
                info.urlThumb = urls.has("thumb") ? urls.get("thumb").getAsString() : "";

                if (photo.has("description") && !photo.get("description").isJsonNull()) {
                    info.description = photo.get("description").getAsString();
                } else if (photo.has("alt_description") && !photo.get("alt_description").isJsonNull()) {
                    info.description = photo.get("alt_description").getAsString();
                }

                if (photo.has("user")) {
                    JsonObject user = photo.getAsJsonObject("user");
                    info.photographe = user.has("name") ? user.get("name").getAsString() : "Inconnu";
                }

                if (photo.has("likes")) {
                    info.likes = photo.get("likes").getAsInt();
                }

                return info;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Unsplash API erreur: " + e.getMessage());
            return null;
        }
    }

    /**
     * Classe interne contenant les informations d'une photo Unsplash.
     */
    public static class PhotoInfo {
        public String urlRegular = "";
        public String urlSmall = "";
        public String urlThumb = "";
        public String description = "";
        public String photographe = "";
        public int likes = 0;

        public String getCredit() {
            return "📸 " + photographe + " via Unsplash";
        }
    }
}
