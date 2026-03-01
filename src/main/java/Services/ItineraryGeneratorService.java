package Services;

import Entities.Itineraire;
import Entities.Voyage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ItineraryGeneratorService {

    // Utilisation de Cerebras
    private final CerebrasService cerebrasService;

    public ItineraryGeneratorService() {
        this.cerebrasService = new CerebrasService();

        // Test de connexion au démarrage
        if (cerebrasService.testConnection()) {
            System.out.println("✅ Cerebras AI est opérationnel");
        } else {
            System.out.println("⚠️ Cerebras AI non disponible");
        }
    }

    /**
     * Génère un itinéraire complet pour un voyage
     */
    public GeneratedItinerary generateItinerary(Voyage voyage, String destination, String pays) {
        System.out.println("🤖 Génération d'itinéraire avec Cerebras pour: " + voyage.getTitre_voyage());

        LocalDate debut = voyage.getDate_debut().toLocalDate();
        LocalDate fin = voyage.getDate_fin().toLocalDate();
        int nbJours = (int) ChronoUnit.DAYS.between(debut, fin) + 1;

        // Construire le prompt au format JSON
        String prompt = construirePrompt(voyage, destination, pays, nbJours);

        try {
            // Essayer Cerebras d'abord
            String reponse = cerebrasService.generateItinerary(prompt);

            if (reponse != null && !reponse.isEmpty()) {
                System.out.println("✅ Réponse reçue de Cerebras");
                return parserReponse(reponse, voyage, destination, nbJours);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur Cerebras: " + e.getMessage());
        }

        // Fallback ultime
        System.out.println("📋 Utilisation du générateur par défaut");
        return genererItineraireParDefaut(voyage, destination, nbJours);
    }

    /**
     * Construit le prompt pour Cerebras
     */
    private String construirePrompt(Voyage voyage, String destination, String pays, int nbJours) {
        LocalDate debut = voyage.getDate_debut().toLocalDate();
        String saison = determinerSaison(debut);

        return String.format(
                "Crée un itinéraire de voyage détaillé en français.\n\n" +
                        "DESTINATION: %s (%s)\n" +
                        "TITRE: %s\n" +
                        "DURÉE: %d jours\n" +
                        "SAISON: %s\n\n" +
                        "Format JSON attendu (obligatoire):\n" +
                        "{\n" +
                        "  \"nom\": \"NomCourt (max 10 caractères)\",\n" +
                        "  \"description\": \"Description générale\",\n" +
                        "  \"jours\": [\n" +
                        "    {\n" +
                        "      \"numero\": 1,\n" +
                        "      \"theme\": \"Thème du jour\",\n" +
                        "      \"activites\": [\n" +
                        "        {\n" +
                        "          \"nom\": \"Nom activité\",\n" +
                        "          \"lieu\": \"Lieu précis\",\n" +
                        "          \"heure\": \"HH:MM\",\n" +
                        "          \"duree\": 2.5,\n" +
                        "          \"budget\": 20\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "Règles:\n" +
                        "- 3-4 activités par jour\n" +
                        "- Heures entre 08:00 et 22:00\n" +
                        "- Activités variées (culture, détente, gastronomie)\n" +
                        "- Budget réaliste",
                destination, pays,
                voyage.getTitre_voyage(),
                nbJours,
                saison
        );
    }

    /**
     * Parse la réponse JSON de Cerebras
     */
    private GeneratedItinerary parserReponse(String reponse, Voyage voyage, String destination, int nbJours) {
        GeneratedItinerary itinerary = new GeneratedItinerary();
        itinerary.setVoyage(voyage);
        itinerary.setDestination(destination);

        try {
            // Nettoyer la réponse
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
                        // Activités par défaut
                        jour.setActivites(genererActivitesDefaut("Jour " + (i + 1), destination));
                    }

                    listeJours.add(jour);
                }
                itinerary.setJours(listeJours);
            } else {
                // Jours par défaut
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

        return "{}";
    }

    /**
     * Génère un itinéraire par défaut
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