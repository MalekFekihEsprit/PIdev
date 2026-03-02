package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NtfyApi {

    public enum Priority {
        MIN("min"), LOW("low"), DEFAULT("default"), HIGH("high"), URGENT("urgent");

        private final String value;
        Priority(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    private static final String SERVER          = "https://ntfy.sh";
    private static final int    TIMEOUT_SECONDS = 10;

    private final String     topic;
    private final HttpClient httpClient;

    public NtfyApi(String topic) {
        this.topic      = topic;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    public boolean send(String title, String message) {
        return send(title, message, Priority.DEFAULT, "bell");
    }

    public boolean sendWarning(String title, String message) {
        return send(title, message, Priority.HIGH, "warning,money_with_wings");
    }

    public boolean sendCritical(String title, String message) {
        return send(title, message, Priority.URGENT, "rotating_light,money");
    }

    public boolean send(String title, String message, Priority priority, String tags) {
        try {
            // ✅ Supprime uniquement les emojis du titre (garde le texte lisible)
            String cleanTitle = title.replaceAll("[^\\x00-\\x7F]", "").trim();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER + "/" + topic))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .header("Title",    cleanTitle)
                    .header("Priority", priority.getValue())
                    .header("Tags",     tags)
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            boolean success = response.statusCode() == 200;
            if (success) System.out.println("[NtfyApi] ✅ Notification envoyée : " + title);
            else System.err.println("[NtfyApi] ❌ HTTP " + response.statusCode() + " : " + response.body());
            return success;

        } catch (Exception e) {
            System.err.println("[NtfyApi] ❌ Erreur : " + e.getMessage());
            return false;
        }
    }
}