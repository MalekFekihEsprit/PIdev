package Services;

import Utils.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ModerationService {

    private static final String API_USER = Config.get("sightengine.api.user");
    private static final String API_SECRET = Config.get("sightengine.api.secret");
    private static final String API_URL_IMAGE = "https://api.sightengine.com/1.0/check.json";
    private static final String API_URL_TEXT = "https://api.sightengine.com/1.0/text/check.json";

    public static class ModerationResult {
        public boolean estSuspect;
        public String raison;
        public String niveauAlerte;
        public double scoreTexte;
        public double scoreImage;

        public ModerationResult() {
            this.estSuspect = false;
            this.raison = "Contenu sain";
            this.niveauAlerte = "LOW";
            this.scoreTexte = 0.0;
            this.scoreImage = 0.0;
        }

        @Override
        public String toString() {
            return String.format("ModerationResult{suspect=%s, niveau=%s, raison='%s', texte=%.2f, image=%.2f}",
                    estSuspect, niveauAlerte, raison, scoreTexte, scoreImage);
        }
    }

    public static ModerationResult analyserActivite(String nom, String description, String imagePath) {
        ModerationResult resultat = new ModerationResult();
        String texteComplet = nom + " " + description;

        try {
            if (API_USER == null || API_USER.isEmpty() || API_USER.equals("VOTRE_API_USER")) {
                return resultat;
            }

            // 1. Analyse du texte
            if (!texteComplet.trim().isEmpty()) {
                JsonObject textResult = analyserTexte(texteComplet);
                if (textResult != null && textResult.has("status") && textResult.get("status").getAsString().equals("success")) {
                    resultat.scoreTexte = extraireScoreTexte(textResult);
                }
            }

            // 2. Analyse de l'image (upload direct)
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    JsonObject imageResult = analyserImage(imageFile);
                    if (imageResult != null && imageResult.has("status") && imageResult.get("status").getAsString().equals("success")) {
                        resultat.scoreImage = extraireScoreImage(imageResult);
                    }
                }
            }

            resultat.estSuspect = resultat.scoreTexte > 0.5 || resultat.scoreImage > 0.5;
            resultat.niveauAlerte = determinerNiveau(resultat.scoreTexte, resultat.scoreImage);
            resultat.raison = construireRaison(resultat.scoreTexte, resultat.scoreImage);

        } catch (Exception e) {
            // Ignorer silencieusement
        }

        return resultat;
    }

    // ====================== API TEXTE ======================
    private static JsonObject analyserTexte(String texte) throws IOException {
        String params = "text=" + URLEncoder.encode(texte, "UTF-8") +
                "&lang=fr" +
                "&mode=rules" +
                "&api_user=" + API_USER +
                "&api_secret=" + API_SECRET;

        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL_TEXT).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes("utf-8"));
        }

        return lireReponse(conn);
    }

    // ====================== API IMAGE (upload direct) ======================
    private static JsonObject analyserImage(File imageFile) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL_IMAGE).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setDoOutput(true);

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            // api_user
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"api_user\"\r\n\r\n");
            out.writeBytes(API_USER + "\r\n");

            // api_secret
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"api_secret\"\r\n\r\n");
            out.writeBytes(API_SECRET + "\r\n");

            // Fichier image (media)
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"media\"; filename=\"" + imageFile.getName() + "\"\r\n");
            out.writeBytes("Content-Type: image/jpeg\r\n\r\n");

            byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
            out.write(fileBytes);
            out.writeBytes("\r\n");

            // Modèles (uniquement nudity-2.1)
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"models\"\r\n\r\n");
            out.writeBytes("nudity-2.1\r\n");

            out.writeBytes("--" + boundary + "--\r\n");
        }

        return lireReponse(conn);
    }

    private static JsonObject lireReponse(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                return JsonParser.parseString(response.toString()).getAsJsonObject();
            }
        } else {
            return null;
        }
    }

    private static double extraireScoreTexte(JsonObject result) {
        try {
            if (result.has("profanity")) {
                JsonObject profanity = result.getAsJsonObject("profanity");
                if (profanity.has("matches")) {
                    var matches = profanity.getAsJsonArray("matches");
                    for (int i = 0; i < matches.size(); i++) {
                        String intensite = matches.get(i).getAsJsonObject().get("intensity").getAsString();
                        if ("high".equals(intensite)) return 0.9;
                        if ("medium".equals(intensite)) return 0.7;
                        if ("low".equals(intensite)) return 0.4;
                    }
                }
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    /**
     * Extrait le score de l'image en ignorant le champ "none".
     * Ne prend en compte que les champs à risque (sexual_activity, sexual_display, erotica, etc.)
     */
    private static double extraireScoreImage(JsonObject result) {
        double maxScore = 0.0;
        try {
            if (result.has("nudity")) {
                JsonObject nudity = result.getAsJsonObject("nudity");

                // Champs directs à risque
                String[] risqueFields = {"sexual_activity", "sexual_display", "erotica", "very_suggestive", "suggestive", "mildly_suggestive"};
                for (String field : risqueFields) {
                    if (nudity.has(field)) {
                        double val = nudity.get(field).getAsDouble();
                        maxScore = Math.max(maxScore, val);
                    }
                }

                // Dans suggestive_classes, ignorer "none"
                if (nudity.has("suggestive_classes")) {
                    JsonObject suggestive = nudity.getAsJsonObject("suggestive_classes");
                    for (String key : suggestive.keySet()) {
                        if (!key.equals("none") && suggestive.get(key).isJsonPrimitive() && suggestive.get(key).getAsJsonPrimitive().isNumber()) {
                            double val = suggestive.get(key).getAsDouble();
                            maxScore = Math.max(maxScore, val);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return maxScore;
    }

    private static String determinerNiveau(double scoreTexte, double scoreImage) {
        double max = Math.max(scoreTexte, scoreImage);
        if (max > 0.8) return "HIGH";
        if (max > 0.5) return "MEDIUM";
        return "LOW";
    }

    private static String construireRaison(double scoreTexte, double scoreImage) {
        if (scoreTexte > 0.5 && scoreImage > 0.5) return "Contenu texte et image suspects";
        if (scoreTexte > 0.5) return "Langage potentiellement inapproprié";
        if (scoreImage > 0.5) return "Image potentiellement inappropriée";
        return "Contenu sain";
    }
}