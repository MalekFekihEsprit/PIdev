package Services;

import Entities.Itineraire;
import Entities.Voyage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ItineraryGeneratorService {

    // Chargement de la clé depuis .env
    private static final String MISTRAL_API_KEY = EnvLoader.get("MISTRAL_API_KEY");
    private static final String MISTRAL_API_URL = "https://api.mistral.ai/v1/chat/completions";

    // Modèles disponibles:
    // - "mistral-tiny" (le plus rapide, gratuit)
    // - "mistral-small" (bon équilibre)
    // - "mistral-medium" (meilleure qualité)
    // - "mistral-large-latest" (modèle le plus puissant)
    private static final String MODEL = "mistral-small-latest";

    private final OkHttpClient client;

    public ItineraryGeneratorService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        if (!isApiConfigured()) {
            System.out.println("⚠️⚠️⚠️ ATTENTION: Clé API Mistral non configurée!");
            System.out.println("📝 Créez un fichier .env avec MISTRAL_API_KEY=votre_cle");
        }
    }

    /**
     * Vérifie si l'API Mistral est configurée
     */
    private boolean isApiConfigured() {
        return EnvLoader.hasValidKey("MISTRAL_API_KEY");
    }

    /**
     * Génère un itinéraire complet pour un voyage avec Mistral AI
     */
    public GeneratedItinerary generateItinerary(Voyage voyage, String destination, String pays) {
        System.out.println("🤖 Génération d'itinéraire avec Mistral AI pour: " + voyage.getTitre_voyage());

        LocalDate debut = voyage.getDate_debut().toLocalDate();
        LocalDate fin = voyage.getDate_fin().toLocalDate();
        int nbJours = (int) ChronoUnit.DAYS.between(debut, fin) + 1;

        // Si l'API n'est pas configurée, utiliser directement le fallback
        if (!isApiConfigured()) {
            System.out.println("🔧 Utilisation du générateur par défaut (API Mistral non configurée)");
            return genererItineraireParDefaut(voyage, destination, nbJours);
        }

        // Construire le prompt pour Mistral
        String prompt = construirePrompt(voyage, destination, pays, nbJours);

        try {
            String reponse = appelMistral(prompt);
            if (reponse != null && !reponse.isEmpty()) {
                return parserReponse(reponse, voyage, destination, nbJours);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur Mistral AI: " + e.getMessage());
            e.printStackTrace();
        }

        // Fallback: générer un itinéraire par défaut
        System.out.println("⚠️ Utilisation du générateur par défaut (fallback)");
        return genererItineraireParDefaut(voyage, destination, nbJours);
    }

    /**
     * Construit le prompt pour Mistral
     */
    private String construirePrompt(Voyage voyage, String destination, String pays, int nbJours) {
        LocalDate debut = voyage.getDate_debut().toLocalDate();
        LocalDate fin = voyage.getDate_fin().toLocalDate();
        String saison = determinerSaison(debut);

        return String.format("""
            Tu es un expert en planification de voyages. Crée un itinéraire détaillé pour un voyage à %s (%s).
            
            INFORMATIONS DU VOYAGE:
            - Titre: %s
            - Date de début: %s
            - Date de fin: %s
            - Nombre de jours: %d
            - Saison: %s
            
            INSTRUCTIONS:
            Crée un itinéraire JOUR PAR JOUR avec:
            1. Un nom accrocheur pour l'itinéraire (MAXIMUM 10 caractères)
            2. Une description générale du voyage (2-3 phrases)
            3. Pour CHAQUE jour:
               - Un thème ou titre pour la journée
               - 3-4 activités recommandées avec:
                 * Nom de l'activité
                 * Lieu précis
                 * Heure approximative (format HH:MM, ex: 09:30)
                 * Durée estimée (en heures, nombre décimal)
                 * Budget approximatif (en euros, nombre entier)
            
            FORMAT DE RÉPONSE OBLIGATOIRE (JSON UNIQUEMENT, sans texte avant/après):
            {
              "nom": "NomCourt",
              "description": "Description générale du voyage...",
              "jours": [
                {
                  "numero": 1,
                  "theme": "Thème du jour 1",
                  "activites": [
                    {
                      "nom": "Visite du musée",
                      "lieu": "Nom du lieu",
                      "heure": "09:30",
                      "duree": 2.5,
                      "budget": 15
                    }
                  ]
                }
              ]
            }
            
            RÈGLES IMPORTANTES:
            - Le nom de l'itinéraire doit faire MAXIMUM 10 caractères
            - Sois créatif et réaliste
            - Adapte les activités à la saison (%s)
            - Propose des activités variées (culture, détente, gastronomie, nature)
            - Respecte les horaires normaux (activités entre 8h et 22h)
            - Réponds UNIQUEMENT avec le JSON, sans aucun autre texte
            """,
                destination, pays,
                voyage.getTitre_voyage(),
                debut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                nbJours,
                saison,
                saison
        );
    }

    /**
     * Appelle l'API Mistral AI
     */
    private String appelMistral(String prompt) throws Exception {
        if (!isApiConfigured()) {
            return null;
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 2000);
        requestBody.addProperty("top_p", 0.9);

        // Format de réponse JSON
        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        requestBody.add("response_format", responseFormat);

        // Construire les messages
        JsonArray messages = new JsonArray();

        // Message système
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "Tu es un expert en planification de voyages. Tu réponds uniquement avec du JSON valide, sans aucun texte supplémentaire.");
        messages.add(systemMessage);

        // Message utilisateur
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        // Construire la requête HTTP
        Request request = new Request.Builder()
                .url(MISTRAL_API_URL)
                .header("Authorization", "Bearer " + MISTRAL_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        // Exécuter la requête
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                System.err.println("❌ Erreur Mistral API - Code: " + response.code() + ", Body: " + errorBody);

                // Gestion des erreurs de quota et d'authentification
                if (response.code() == 429) {
                    System.err.println("⚠️ Quota Mistral dépassé. Utilisation du fallback.");
                }
                if (response.code() == 401 || response.code() == 403) {
                    System.err.println("🔑 Clé API Mistral invalide ou non configurée!");
                }
                return null;
            }

            String jsonData = response.body().string();
            return extraireTexteReponse(jsonData);
        }
    }

    /**
     * Extrait le texte de la réponse Mistral
     */
    private String extraireTexteReponse(String jsonData) {
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();

            if (json.has("choices") && json.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");

                if (message.has("content") && !message.get("content").isJsonNull()) {
                    return message.get("content").getAsString();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing réponse Mistral: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse la réponse JSON de Mistral
     */
    private GeneratedItinerary parserReponse(String reponse, Voyage voyage, String destination, int nbJours) {
        GeneratedItinerary itinerary = new GeneratedItinerary();
        itinerary.setVoyage(voyage);
        itinerary.setDestination(destination);

        try {
            // Nettoyer la réponse (parfois Mistral ajoute du texte avant/après le JSON)
            String jsonStr = extraireJson(reponse);
            System.out.println("📦 Réponse JSON extraite: " + jsonStr.substring(0, Math.min(100, jsonStr.length())) + "...");

            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();

            // Extraire le nom (limité à 10 caractères)
            if (json.has("nom")) {
                String nom = json.get("nom").getAsString();
                if (nom.length() > 10) {
                    nom = nom.substring(0, 10);
                }
                itinerary.setNomItineraire(nom);
            } else {
                itinerary.setNomItineraire(genererNom(destination, nbJours));
            }

            // Extraire la description
            if (json.has("description")) {
                itinerary.setDescription(json.get("description").getAsString());
            } else {
                itinerary.setDescription("Découvrez " + destination + " pendant " + nbJours + " jours inoubliables.");
            }

            // Extraire les jours
            if (json.has("jours")) {
                JsonArray jours = json.getAsJsonArray("jours");
                List<GeneratedJour> listeJours = new ArrayList<>();

                for (int i = 0; i < jours.size() && i < nbJours; i++) {
                    JsonObject jourJson = jours.get(i).getAsJsonObject();
                    GeneratedJour jour = new GeneratedJour();
                    jour.setNumero(i + 1);

                    // Thème du jour
                    if (jourJson.has("theme")) {
                        jour.setTheme(jourJson.get("theme").getAsString());
                    } else {
                        jour.setTheme("Jour " + (i + 1));
                    }

                    // Activités du jour
                    if (jourJson.has("activites")) {
                        JsonArray activites = jourJson.getAsJsonArray("activites");
                        List<GeneratedActivite> listeActivites = new ArrayList<>();

                        for (int j = 0; j < activites.size(); j++) {
                            JsonObject actJson = activites.get(j).getAsJsonObject();
                            GeneratedActivite act = new GeneratedActivite();

                            act.setNom(getJsonString(actJson, "nom", "Activité"));
                            act.setLieu(getJsonString(actJson, "lieu", destination));
                            act.setHeure(getJsonString(actJson, "heure", "10:00"));

                            // Durée (float)
                            if (actJson.has("duree")) {
                                try {
                                    act.setDuree(actJson.get("duree").getAsFloat());
                                } catch (Exception e) {
                                    act.setDuree(2.0f);
                                }
                            } else {
                                act.setDuree(2.0f);
                            }

                            // Budget (int)
                            if (actJson.has("budget")) {
                                try {
                                    act.setBudget(actJson.get("budget").getAsInt());
                                } catch (Exception e) {
                                    act.setBudget(20);
                                }
                            } else {
                                act.setBudget(20);
                            }

                            listeActivites.add(act);
                        }
                        jour.setActivites(listeActivites);
                    } else {
                        // Activités par défaut si aucune n'est fournie
                        jour.setActivites(genererActivitesDefaut("Jour " + (i + 1), destination));
                    }

                    listeJours.add(jour);
                }
                itinerary.setJours(listeJours);
            } else {
                // Jours par défaut si aucun n'est fourni
                List<GeneratedJour> joursDefaut = new ArrayList<>();
                for (int i = 1; i <= nbJours; i++) {
                    GeneratedJour jour = new GeneratedJour();
                    jour.setNumero(i);
                    jour.setTheme("Jour " + i);
                    jour.setActivites(genererActivitesDefaut("Jour " + i, destination));
                    joursDefaut.add(jour);
                }
                itinerary.setJours(joursDefaut);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing itinéraire: " + e.getMessage());
            e.printStackTrace();
            return genererItineraireParDefaut(voyage, destination, nbJours);
        }

        return itinerary;
    }

    /**
     * Extrait une valeur String d'un JsonObject avec valeur par défaut
     */
    private String getJsonString(JsonObject obj, String key, String defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return defaultValue;
    }

    /**
     * Extrait le JSON d'une réponse texte
     */
    private String extraireJson(String texte) {
        if (texte == null || texte.isEmpty()) return "{}";

        // Chercher le premier { et le dernier }
        int debut = texte.indexOf('{');
        int fin = texte.lastIndexOf('}');

        if (debut >= 0 && fin > debut) {
            return texte.substring(debut, fin + 1);
        }

        // Si pas de JSON trouvé, chercher un pattern JSON dans le texte
        String[] lignes = texte.split("\n");
        StringBuilder jsonBuilder = new StringBuilder();
        boolean dansJson = false;

        for (String ligne : lignes) {
            if (ligne.contains("{")) dansJson = true;
            if (dansJson) jsonBuilder.append(ligne);
            if (ligne.contains("}")) break;
        }

        String resultat = jsonBuilder.toString();
        if (!resultat.isEmpty() && resultat.contains("{") && resultat.contains("}")) {
            return resultat;
        }

        return "{}";
    }

    /**
     * Génère un itinéraire par défaut si Mistral ne répond pas
     */
    private GeneratedItinerary genererItineraireParDefaut(Voyage voyage, String destination, int nbJours) {
        GeneratedItinerary itinerary = new GeneratedItinerary();
        itinerary.setVoyage(voyage);
        itinerary.setDestination(destination);
        itinerary.setNomItineraire(genererNom(destination, nbJours));
        itinerary.setDescription("Découvrez les merveilles de " + destination + " pendant " + nbJours + " jours inoubliables.");

        List<GeneratedJour> jours = new ArrayList<>();

        for (int i = 1; i <= nbJours; i++) {
            GeneratedJour jour = new GeneratedJour();
            jour.setNumero(i);

            if (i == 1) {
                jour.setTheme("Arrivée et découverte");
            } else if (i == nbJours) {
                jour.setTheme("Dernier jour et départ");
            } else if (i == 2) {
                jour.setTheme("Immersion culturelle");
            } else if (i == 3) {
                jour.setTheme("Nature et détente");
            } else {
                jour.setTheme("Exploration jour " + i);
            }

            jour.setActivites(genererActivitesDefaut(jour.getTheme(), destination));
            jours.add(jour);
        }

        itinerary.setJours(jours);
        return itinerary;
    }

    /**
     * Génère des activités par défaut
     */
    private List<GeneratedActivite> genererActivitesDefaut(String theme, String destination) {
        List<GeneratedActivite> activites = new ArrayList<>();

        String[][] activitesDefaut = {
                {"Petit-déjeuner", "Hôtel", "08:30", "1.0", "15"},
                {"Visite guidée", "Centre-ville de " + destination, "10:00", "3.0", "25"},
                {"Déjeuner", "Restaurant local", "13:00", "1.5", "20"},
                {"Visite libre", "Quartier historique", "15:00", "2.5", "10"},
                {"Dîner", "Restaurant typique", "19:30", "2.0", "30"}
        };

        // Limiter le nombre d'activités (max 4 par jour)
        int maxActivites = Math.min(4, activitesDefaut.length);
        for (int i = 0; i < maxActivites; i++) {
            String[] data = activitesDefaut[i];
            GeneratedActivite act = new GeneratedActivite();
            act.setNom(data[0]);
            act.setLieu(data[1]);
            act.setHeure(data[2]);
            act.setDuree(Float.parseFloat(data[3]));
            act.setBudget(Integer.parseInt(data[4]));
            activites.add(act);
        }

        return activites;
    }

    /**
     * Génère un nom d'itinéraire par défaut
     */
    private String genererNom(String destination, int nbJours) {
        String nom = destination.substring(0, Math.min(4, destination.length())) + nbJours;
        if (nom.length() > 10) {
            nom = nom.substring(0, 10);
        }
        return nom;
    }

    /**
     * Détermine la saison à partir d'une date
     */
    private String determinerSaison(LocalDate date) {
        int mois = date.getMonthValue();
        if (mois >= 3 && mois <= 5) return "printemps";
        if (mois >= 6 && mois <= 8) return "été";
        if (mois >= 9 && mois <= 11) return "automne";
        return "hiver";
    }

    // ============== CLASSES INTERNES ==============

    public static class GeneratedItinerary {
        private Voyage voyage;
        private String destination;
        private String nomItineraire;
        private String description;
        private List<GeneratedJour> jours;

        public Voyage getVoyage() { return voyage; }
        public void setVoyage(Voyage voyage) { this.voyage = voyage; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public String getNomItineraire() { return nomItineraire; }
        public void setNomItineraire(String nomItineraire) { this.nomItineraire = nomItineraire; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<GeneratedJour> getJours() { return jours; }
        public void setJours(List<GeneratedJour> jours) { this.jours = jours; }

        public int getNbJours() { return jours != null ? jours.size() : 0; }

        public Itineraire toItineraire() {
            Itineraire itineraire = new Itineraire();
            itineraire.setNom_itineraire(this.nomItineraire);
            itineraire.setDescription_itineraire(this.description);
            itineraire.setId_voyage(this.voyage.getId_voyage());
            return itineraire;
        }
    }

    public static class GeneratedJour {
        private int numero;
        private String theme;
        private List<GeneratedActivite> activites;

        public int getNumero() { return numero; }
        public void setNumero(int numero) { this.numero = numero; }

        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }

        public List<GeneratedActivite> getActivites() { return activites; }
        public void setActivites(List<GeneratedActivite> activites) { this.activites = activites; }

        public int getNbActivites() { return activites != null ? activites.size() : 0; }
    }

    public static class GeneratedActivite {
        private String nom;
        private String lieu;
        private String heure;
        private float duree;
        private int budget;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getLieu() { return lieu; }
        public void setLieu(String lieu) { this.lieu = lieu; }

        public String getHeure() { return heure; }
        public void setHeure(String heure) { this.heure = heure; }

        public float getDuree() { return duree; }
        public void setDuree(float duree) { this.duree = duree; }

        public int getBudget() { return budget; }
        public void setBudget(int budget) { this.budget = budget; }

        public java.sql.Time getHeureAsTime() {
            try {
                return java.sql.Time.valueOf(heure + ":00");
            } catch (Exception e) {
                return java.sql.Time.valueOf("10:00:00");
            }
        }
    }
}