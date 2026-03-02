package Services;

import Entities.Budget;
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

/**
 * IA DE GESTION DE CRISE — PLAN B FINANCIER (Google Gemini)
 * Détecte les situations critiques et propose automatiquement
 * des solutions pour finir le voyage sans dépasser le budget.
 */
public class CrisisManagementService {

    private static final String API_KEY = loadApiKey();

    // Utilisation de gemini-2.5-flash-lite pour économiser le quota
    private static final String MODEL = "gemini-2.5-flash-lite";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/"
            + MODEL + ":generateContent?key=";

    /** Seuil d'alerte : au-delà de 80 % du budget consommé, on déclenche l'analyse */
    public static final double CRISIS_THRESHOLD = 0.80;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper   = new ObjectMapper();

    private static String loadApiKey() {
        try {
            // Essayer d'abord la variable d'environnement
            String envKey = System.getenv("AI_AGENT");
            if (envKey != null && !envKey.isBlank()) {
                System.out.println("✅ CrisisManagementService: Clé chargée depuis variable d'environnement");
                return envKey;
            }

            // Sinon, charger depuis .env
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(".env")
                    .ignoreIfMissing()
                    .load();

            String dotenvKey = dotenv.get("AI_AGENT");
            if (dotenvKey != null && !dotenvKey.isBlank()) {
                System.out.println("✅ CrisisManagementService: Clé chargée depuis fichier .env");
                return dotenvKey;
            }

            System.err.println("❌ CrisisManagementService: Clé API non trouvée");
            return null;

        } catch (Exception e) {
            System.err.println("Erreur chargement clé API: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Modèles de résultats
    // ─────────────────────────────────────────────────────────────────────────

    /** Niveau de crise détecté. */
    public enum CrisisLevel {
        NONE,     // < 60 %
        WARNING,  // 60-80 %
        CRITICAL, // 80-95 %
        DANGER    // > 95 %
    }

    /** Résultat complet de l'analyse de crise. */
    public static class CrisisResult {
        public final CrisisLevel level;
        public final String      statusLabel;      // ex. "🚨 Situation critique"
        public final String      summary;          // Résumé de la situation
        public final String      immediatePlan;    // Actions immédiates (24-48 h)
        public final String      savingsTips;      // Économies possibles par catégorie
        public final String      emergencyOptions; // Options d'urgence si vraiment à court

        public CrisisResult(CrisisLevel level, String statusLabel, String summary,
                            String immediatePlan, String savingsTips, String emergencyOptions) {
            this.level            = level;
            this.statusLabel      = statusLabel;
            this.summary          = summary;
            this.immediatePlan    = immediatePlan;
            this.savingsTips      = savingsTips;
            this.emergencyOptions = emergencyOptions;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  API publique
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Évalue le niveau de crise financière et propose un Plan B asynchrone.
     *
     * @param budget      le budget sélectionné
     * @param depenses    liste des dépenses déjà enregistrées
     * @param destination nom de la destination
     * @param joursRestants nombre de jours restants dans le voyage (0 si inconnu)
     * @return CompletableFuture<CrisisResult>
     */
    public CompletableFuture<CrisisResult> analyzeAsync(
            Budget budget, List<Depense> depenses, String destination, int joursRestants) {

        return CompletableFuture.supplyAsync(() -> {

            // Vérification de la clé API
            if (API_KEY == null || API_KEY.isBlank()) {
                return new CrisisResult(CrisisLevel.WARNING,
                        "⚠️ Clé API manquante",
                        "La clé AI_AGENT n'est pas définie.",
                        "Configurez votre clé API dans le fichier .env",
                        "",
                        "");
            }

            String fullUrl = API_URL + API_KEY;

            double montantTotal  = budget.getMontantTotal();
            double totalDepense  = depenses.stream().mapToDouble(Depense::getMontantDepense).sum();
            double restant       = montantTotal - totalDepense;
            double ratio         = (montantTotal > 0) ? totalDepense / montantTotal : 0;

            // ── 1. Déterminer le niveau de crise sans appel IA ───────────────
            CrisisLevel level;
            if (ratio < 0.60)      level = CrisisLevel.NONE;
            else if (ratio < 0.80) level = CrisisLevel.WARNING;
            else if (ratio < 0.95) level = CrisisLevel.CRITICAL;
            else                   level = CrisisLevel.DANGER;

            // Pas de crise → réponse rapide sans appel IA
            if (level == CrisisLevel.NONE) {
                return new CrisisResult(level,
                        "✅ Budget sous contrôle",
                        String.format("%.0f%% du budget utilisé. Tout va bien !", ratio * 100),
                        "Continuez à suivre vos dépenses régulièrement.",
                        "Aucune réduction nécessaire pour l'instant.",
                        "");
            }

            // ── 2. Résumé des dépenses par catégorie ──────────────────────────
            Map<String, Double> parCategorie = depenses.stream().collect(
                    Collectors.groupingBy(
                            d -> d.getCategorieDepense() != null ? d.getCategorieDepense() : "Autre",
                            Collectors.summingDouble(Depense::getMontantDepense)));

            String categoriesResume = parCategorie.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .map(e -> String.format("  - %s : %.2f %s (%.0f%%)",
                            e.getKey(), e.getValue(), budget.getDeviseBudget(),
                            (montantTotal > 0 ? e.getValue() / montantTotal * 100 : 0)))
                    .collect(Collectors.joining("\n"));

            // ── 3. Prompt IA amélioré pour obtenir du JSON ────────────────────
            String urgenceLabel = switch (level) {
                case WARNING  -> "⚠️ Attention (60-80%)";
                case CRITICAL -> "🔴 Critique (80-95%)";
                case DANGER   -> "🚨 Danger (>95%)";
                default       -> "";
            };

            String userPrompt = String.format("""
                Tu es un expert en gestion de budget de voyage.
                
                Situation financière du voyageur à %s :
                - Budget total : %.2f %s
                - Dépensé : %.2f %s (%.0f%% du budget)
                - Restant : %.2f %s
                - Jours restants : %s
                - Niveau d'urgence : %s
                
                Répartition des dépenses :
                %s
                
                Génère un Plan B financier complet.
                
                Réponds UNIQUEMENT avec un objet JSON valide, sans aucun texte avant ou après, en respectant ce format exact :
                {
                  "statusLabel": "<emoji + résumé statut>",
                  "summary": "<2 phrases de diagnostic>",
                  "immediatePlan": "<3 actions concrètes à faire dans les 24-48h>",
                  "savingsTips": "<économies possibles par catégorie avec montants estimés>",
                  "emergencyOptions": "<solutions d'urgence si budget dépassé : ambassade, assurance, virement, etc.>"
                }
                
                Exemple de format attendu :
                {
                  "statusLabel": "🔴 Situation critique",
                  "summary": "Vous avez déjà dépensé 85%% de votre budget. Avec 5 jours restants, il ne vous reste que 150€.",
                  "immediatePlan": "1. Annulez les excursions non réservées. 2. Passez en mode 'économies' sur la restauration. 3. Contactez votre hébergement pour négocier un tarif.",
                  "savingsTips": "Restauration : économisez 40€ en cuisinant. Transport : utilisez les transports en commun (économie 30€).",
                  "emergencyOptions": "Contactez votre assurance voyage, votre ambassade, ou demandez une avance de fonds à vos proches."
                }
                """,
                    destination,
                    montantTotal, budget.getDeviseBudget(),
                    totalDepense, budget.getDeviseBudget(), ratio * 100,
                    restant, budget.getDeviseBudget(),
                    joursRestants > 0 ? joursRestants + " jour(s)" : "inconnu",
                    urgenceLabel,
                    categoriesResume);

            try {
                // ── 4. Construction de la requête Gemini (SANS response_mime_type) ───
                ObjectNode requestBody = mapper.createObjectNode();

                // Contenu principal
                ObjectNode content = mapper.createObjectNode();
                content.set("parts", mapper.valueToTree(List.of(Map.of("text", userPrompt))));
                requestBody.set("contents", mapper.valueToTree(List.of(content)));

                // Configuration simple (sans response_mime_type)
                ObjectNode generationConfig = mapper.createObjectNode();
                generationConfig.put("temperature", 0.2);
                generationConfig.put("maxOutputTokens", 1000);
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
                    throw new RuntimeException("API Gemini a retourné " + resp.statusCode() + " : " + errorMsg);
                }

                // ── 5. Parser la réponse ──────────────────────────────────────
                JsonNode root = mapper.readTree(resp.body());
                String text = root.path("candidates").get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText();

                // Nettoyer la réponse (enlever les éventuels backticks markdown)
                String cleanText = text.replaceAll("```json", "").replaceAll("```", "").trim();

                JsonNode result = mapper.readTree(cleanText);

                return new CrisisResult(
                        level,
                        result.path("statusLabel").asText(urgenceLabel),
                        result.path("summary").asText(""),
                        result.path("immediatePlan").asText(""),
                        result.path("savingsTips").asText(""),
                        result.path("emergencyOptions").asText("")
                );

            } catch (Exception e) {
                System.err.println("[CrisisManagementService] Erreur: " + e.getMessage());
                e.printStackTrace();
                return new CrisisResult(level,
                        "⚠️ Plan B indisponible",
                        "Votre budget est fortement consommé. Une action est nécessaire.",
                        "1. Suspendez les achats non essentiels.\n2. Contactez votre banque pour un plafond d'urgence.\n3. Cherchez des hébergements moins coûteux.",
                        "Réduisez les dépenses de restauration et shopping.",
                        "Contactez l'ambassade ou votre assurance voyage en cas d'urgence.");
            }
        });
    }

    /**
     * Raccourci pour savoir rapidement si une analyse est nécessaire.
     */
    public static boolean isCrisisDetected(double montantTotal, double totalDepense) {
        if (montantTotal <= 0) return false;
        return (totalDepense / montantTotal) >= CRISIS_THRESHOLD;
    }
}