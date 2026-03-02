package Services;

import Entities.Depense;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EcologicalScoreService {

    // Utiliser gemini-2.5-flash-lite (moins de quota) ou gemini-2.5-flash
    private static final String MODEL = "gemini-2.5-flash-lite";
    private static String API_KEY = null;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/"
            + MODEL + ":generateContent?key=";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            API_KEY = System.getenv("AI_AGENT");
            if (API_KEY == null || API_KEY.isBlank()) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(".")
                        .filename(".env")
                        .ignoreIfMissing()
                        .load();
                API_KEY = dotenv.get("AI_AGENT");
            }
            if (API_KEY != null && !API_KEY.isBlank()) {
                System.out.println("✅ EcologicalScoreService: Clé API chargée");
            } else {
                System.err.println("❌ EcologicalScoreService: Clé API non trouvée");
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement clé: " + e.getMessage());
        }
    }

    public static class EcologicalResult {
        public final int score;
        public final String label;
        public final String co2Estimate;
        public final String analysis;
        public final String alternatives;

        public EcologicalResult(int score, String label, String co2Estimate,
                                String analysis, String alternatives) {
            this.score = score;
            this.label = label;
            this.co2Estimate = co2Estimate;
            this.analysis = analysis;
            this.alternatives = alternatives;
        }
    }

    public CompletableFuture<EcologicalResult> analyzeAsync(
            List<Depense> depenses, String destination, String devise) {

        return CompletableFuture.supplyAsync(() -> {

            if (API_KEY == null || API_KEY.isBlank()) {
                return new EcologicalResult(50, "⚠️ Clé API manquante", "N/A",
                        "La clé AI_AGENT n'est pas définie",
                        "Configurez votre clé API dans le fichier .env");
            }

            String fullUrl = API_URL + API_KEY;

            String depensesResume = depenses.stream()
                    .map(d -> String.format("- %s [%s] %.2f %s",
                            d.getLibelleDepense(),
                            d.getCategorieDepense(),
                            d.getMontantDepense(),
                            d.getDeviseDepense() != null ? d.getDeviseDepense() : devise))
                    .collect(Collectors.joining("\n"));

            double totalDepense = depenses.stream()
                    .mapToDouble(Depense::getMontantDepense).sum();

            // PROMPT CORRIGÉ : on demande explicitement du JSON sans utiliser response_mime_type
            String prompt = String.format("""
                Tu es un expert en tourisme durable et empreinte carbone.
                Voici les dépenses d'un voyageur à %s (total : %.2f %s) :
                
                %s
                
                Analyse ces dépenses sous l'angle écologique.
                
                Réponds UNIQUEMENT avec un objet JSON valide, sans aucun texte avant ou après, en respectant ce format exact :
                {
                  "score": 0-100,
                  "label": "texte court avec emoji",
                  "co2Estimate": "estimation en kg",
                  "analysis": "analyse en 2-3 phrases",
                  "alternatives": "3 alternatives concrètes"
                }
                
                Exemple de format attendu :
                {
                  "score": 65,
                  "label": "🌱 Impact modéré",
                  "co2Estimate": "280 kg CO₂",
                  "analysis": "Votre plus gros impact vient du transport aérien...",
                  "alternatives": "1. Préférez le train pour les trajets courts. 2. Choisissez des hébergements éco-labellisés. 3. Mangez local et de saison."
                }
                """, destination, totalDepense, devise, depensesResume);

            try {
                // Construction de la requête SANS generationConfig problématique
                ObjectNode requestBody = mapper.createObjectNode();
                requestBody.set("contents", mapper.valueToTree(List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )));

                // Optionnel : ajouter une configuration simple sans response_mime_type
                ObjectNode generationConfig = mapper.createObjectNode();
                generationConfig.put("temperature", 0.2);
                generationConfig.put("maxOutputTokens", 1000);
                // Ne pas mettre response_mime_type
                requestBody.set("generationConfig", generationConfig);

                String jsonBody = mapper.writeValueAsString(requestBody);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(fullUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() != 200) {
                    JsonNode errorNode = mapper.readTree(resp.body());
                    String errorMsg = errorNode.path("error").path("message").asText("Erreur inconnue");
                    throw new RuntimeException("API Error (" + resp.statusCode() + "): " + errorMsg);
                }

                JsonNode root = mapper.readTree(resp.body());
                String text = root.path("candidates").get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText();

                // Nettoyer la réponse (enlever les éventuels backticks markdown)
                String cleanText = text.replaceAll("```json", "").replaceAll("```", "").trim();

                JsonNode result = mapper.readTree(cleanText);

                return new EcologicalResult(
                        result.path("score").asInt(50),
                        result.path("label").asText("🌱 Analyse disponible"),
                        result.path("co2Estimate").asText("N/A"),
                        result.path("analysis").asText(""),
                        result.path("alternatives").asText("")
                );

            } catch (Exception e) {
                System.err.println("[EcologicalScoreService] Erreur: " + e.getMessage());
                e.printStackTrace();
                return new EcologicalResult(50, "⚠️ Analyse indisponible", "N/A",
                        "Erreur: " + e.getMessage(),
                        "Vérifiez votre connexion et votre clé API.");
            }
        });
    }
}