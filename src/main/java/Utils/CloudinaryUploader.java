package Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Properties;

/**
 * Utilitaire d'upload d'images vers Cloudinary via l'API REST (sans SDK).
 *
 * Configuration requise dans src/main/resources/cloudinary.env (ou .env) :
 *   CLOUDINARY_CLOUD_NAME=votre_cloud_name
 *   CLOUDINARY_API_KEY=votre_api_key
 *   CLOUDINARY_API_SECRET=votre_api_secret
 */
public class CloudinaryUploader {

    // ──────────────────────────────────────────────────
    // Chargement de la config depuis le fichier .env
    // ──────────────────────────────────────────────────
    private static final String CLOUD_NAME;
    private static final String API_KEY;
    private static final String API_SECRET;

    static {
        Properties props = new Properties();
        // Cherche le fichier dans les resources (sur le classpath)
        try (InputStream is = CloudinaryUploader.class
                .getClassLoader().getResourceAsStream("cloudinary.env")) {
            if (is != null) {
                props.load(is);
            } else {
                // Fallback : fichier .env à la racine du projet
                File envFile = new File(".env");
                if (envFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(envFile)) {
                        props.load(fis);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[CloudinaryUploader] Impossible de lire le fichier de config : " + e.getMessage());
        }

        CLOUD_NAME  = props.getProperty("CLOUDINARY_CLOUD_NAME",  "");
        API_KEY     = props.getProperty("CLOUDINARY_API_KEY",     "");
        API_SECRET  = props.getProperty("CLOUDINARY_API_SECRET",  "");
    }

    // ──────────────────────────────────────────────────
    // Upload principal
    // ──────────────────────────────────────────────────

    /**
     * Upload un fichier image vers Cloudinary.
     *
     * @param imageFile  Le fichier image local à envoyer
     * @param folder     Dossier Cloudinary de destination (ex: "evenements", "activites")
     * @return           L'URL sécurisée (https) retournée par Cloudinary
     * @throws IOException En cas d'erreur réseau ou de réponse inattendue
     */
    public static String uploadImage(File imageFile, String folder) throws IOException {
        if (CLOUD_NAME.isEmpty() || API_KEY.isEmpty() || API_SECRET.isEmpty()) {
            throw new IOException(
                    "Configuration Cloudinary manquante. " +
                            "Vérifiez le fichier cloudinary.env (CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET)."
            );
        }

        long timestamp = System.currentTimeMillis() / 1000L;

        // Signature : "folder=<folder>&timestamp=<ts><API_SECRET>"
        String toSign = "folder=" + folder + "&timestamp=" + timestamp + API_SECRET;
        String signature = sha1Hex(toSign);

        String boundary = "----JavaMultipartBoundary" + System.nanoTime();
        String uploadUrl = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

        HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(30_000);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        String mimeType   = guessMimeType(imageFile.getName());

        try (OutputStream out = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true)) {

            // Champ : api_key
            addTextField(writer, out, boundary, "api_key",   API_KEY);
            // Champ : timestamp
            addTextField(writer, out, boundary, "timestamp", String.valueOf(timestamp));
            // Champ : signature
            addTextField(writer, out, boundary, "signature", signature);
            // Champ : folder
            addTextField(writer, out, boundary, "folder",    folder);
            // Fichier image
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(imageFile.getName()).append("\"").append("\r\n");
            writer.append("Content-Type: ").append(mimeType).append("\r\n");
            writer.append("\r\n");
            writer.flush();
            out.write(imageBytes);
            out.flush();
            writer.append("\r\n");
            // Fin du multipart
            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.flush();
        }

        int responseCode = conn.getResponseCode();
        InputStream responseStream = (responseCode == 200)
                ? conn.getInputStream()
                : conn.getErrorStream();

        String responseBody = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);

        if (responseCode != 200) {
            throw new IOException("Cloudinary a retourné une erreur " + responseCode + " : " + responseBody);
        }

        // Extraction de secure_url depuis le JSON sans dépendance externe
        String secureUrl = extractJsonField(responseBody, "secure_url");
        if (secureUrl == null || secureUrl.isEmpty()) {
            throw new IOException("Réponse Cloudinary invalide (secure_url manquant) : " + responseBody);
        }

        return secureUrl;
    }

    // ──────────────────────────────────────────────────
    // Méthodes utilitaires privées
    // ──────────────────────────────────────────────────

    private static void addTextField(PrintWriter writer, OutputStream out, String boundary, String name, String value)
            throws IOException {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append("\r\n");
        writer.append("\r\n");
        writer.append(value).append("\r\n");
        writer.flush();
    }

    /** Calcule le SHA-1 hex d'une chaîne (utilisé pour la signature Cloudinary). */
    private static String sha1Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-1 non disponible", e);
        }
    }

    /** Extrait la valeur d'un champ JSON simple (chaîne) sans librairie externe. */
    private static String extractJsonField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int colonIdx = json.indexOf(':', idx + key.length());
        if (colonIdx < 0) return null;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart < 0) return null;
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd < 0) return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    /** Devine le type MIME à partir de l'extension du fichier. */
    private static String guessMimeType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp"))  return "image/bmp";
        return "application/octet-stream";
    }
}