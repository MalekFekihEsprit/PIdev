package Entites;

import java.sql.Time;

public class etape {
    private int id_etape;
    private Time heure;
    private String description_etape;
    private int id_itineraire;
    private int id_activite;

    // Champs pour l'affichage (lus depuis la base via jointures)
    private String nomActivite;
    private String lieuActivite;
    private String descriptionActivite;
    private Float dureeActivite;      // float dans la base
    private Float budgetActivite;      // float dans la base
    private Integer niveauDifficulteActivite; // niveaudifficulte
    private Integer ageMinActivite;    // agemin
    private String statutActivite;
    private String nomItineraire;

    public etape() {}

    public etape(Time heure, String description_etape, int id_itineraire, int id_activite) {
        this.heure = heure;
        this.description_etape = description_etape;
        this.id_itineraire = id_itineraire;
        this.id_activite = id_activite;
    }

    // Getters et Setters
    public int getId_etape() { return id_etape; }
    public void setId_etape(int id_etape) { this.id_etape = id_etape; }

    public Time getHeure() { return heure; }
    public void setHeure(Time heure) { this.heure = heure; }

    public String getDescription_etape() { return description_etape; }
    public void setDescription_etape(String description_etape) { this.description_etape = description_etape; }

    public int getId_itineraire() { return id_itineraire; }
    public void setId_itineraire(int id_itineraire) { this.id_itineraire = id_itineraire; }

    public int getId_activite() { return id_activite; }
    public void setId_activite(int id_activite) { this.id_activite = id_activite; }

    // Getters/Setters pour les données de l'activité
    public String getNomActivite() { return nomActivite; }
    public void setNomActivite(String nomActivite) { this.nomActivite = nomActivite; }

    public String getLieuActivite() { return lieuActivite; }
    public void setLieuActivite(String lieuActivite) { this.lieuActivite = lieuActivite; }

    public String getDescriptionActivite() { return descriptionActivite; }
    public void setDescriptionActivite(String descriptionActivite) { this.descriptionActivite = descriptionActivite; }

    public Float getDureeActivite() { return dureeActivite; }
    public void setDureeActivite(Float dureeActivite) { this.dureeActivite = dureeActivite; }

    public Float getBudgetActivite() { return budgetActivite; }
    public void setBudgetActivite(Float budgetActivite) { this.budgetActivite = budgetActivite; }

    public Integer getNiveauDifficulteActivite() { return niveauDifficulteActivite; }
    public void setNiveauDifficulteActivite(Integer niveauDifficulteActivite) { this.niveauDifficulteActivite = niveauDifficulteActivite; }

    public Integer getAgeMinActivite() { return ageMinActivite; }
    public void setAgeMinActivite(Integer ageMinActivite) { this.ageMinActivite = ageMinActivite; }

    public String getStatutActivite() { return statutActivite; }
    public void setStatutActivite(String statutActivite) { this.statutActivite = statutActivite; }

    public String getNomItineraire() { return nomItineraire; }
    public void setNomItineraire(String nomItineraire) { this.nomItineraire = nomItineraire; }

    // Méthodes utilitaires
    public String getLieu() {
        return lieuActivite != null ? lieuActivite : "Lieu non défini";
    }

    public String getHeureFormatted() {
        return heure != null ? heure.toString().substring(0, 5) : "--:--";
    }

    public String getNiveauTexte() {
        if (niveauDifficulteActivite == null) return "Non défini";
        switch (niveauDifficulteActivite) {
            case 1: return "Facile";
            case 2: return "Moyen";
            case 3: return "Difficile";
            case 4: return "Expert";
            default: return "Niveau " + niveauDifficulteActivite;
        }
    }

    @Override
    public String toString() {
        return "etape{" +
                "id_etape=" + id_etape +
                ", heure=" + heure +
                ", id_itineraire=" + id_itineraire +
                ", id_activite=" + id_activite +
                ", nomActivite='" + nomActivite + '\'' +
                '}';
    }
}