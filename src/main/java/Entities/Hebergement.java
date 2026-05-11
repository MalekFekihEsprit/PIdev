package Entities;

public class Hebergement {

    private int id_hebergement;
    private String nom_hebergement;
    private String type_hebergement;
    private Double prix_nuit_hebergement;   // Double (nullable) — correspond à la colonne BD prix_nuit_hebergement
    private String adresse_hebergement;
    private Double note_hebergement;        // Double (nullable)
    private Double latitude_hebergement;   // Double (nullable)
    private Double longitude_hebergement;  // Double (nullable)
    private Destination destination;
    private Integer added_by;              // Integer (nullable) — ON DELETE SET NULL
    private String added_by_name;          // affichage uniquement, non persisté
    private String image_name;

    // ── Constructeurs ──────────────────────────────────────────────

    public Hebergement() {}

    /** Utilisé lors de la lecture depuis la BD (avec id). */
    public Hebergement(int id_hebergement,
                       String nom_hebergement,
                       String type_hebergement,
                       Double prix_nuit_hebergement,
                       String adresse_hebergement,
                       Double note_hebergement,
                       Double latitude_hebergement,
                       Double longitude_hebergement,
                       Destination destination,
                       Integer added_by) {
        this.id_hebergement        = id_hebergement;
        this.nom_hebergement       = nom_hebergement;
        this.type_hebergement      = type_hebergement;
        this.prix_nuit_hebergement = prix_nuit_hebergement;
        this.adresse_hebergement   = adresse_hebergement;
        this.note_hebergement      = note_hebergement;
        this.latitude_hebergement  = latitude_hebergement;
        this.longitude_hebergement = longitude_hebergement;
        this.destination           = destination;
        this.added_by              = added_by;
    }

    /** Utilisé lors de l'ajout (sans id, sans added_by — géré via UserSession). */
    public Hebergement(String nom_hebergement,
                       String type_hebergement,
                       Double prix_nuit_hebergement,
                       String adresse_hebergement,
                       Double note_hebergement,
                       Double latitude_hebergement,
                       Double longitude_hebergement,
                       Destination destination) {
        this.nom_hebergement       = nom_hebergement;
        this.type_hebergement      = type_hebergement;
        this.prix_nuit_hebergement = prix_nuit_hebergement;
        this.adresse_hebergement   = adresse_hebergement;
        this.note_hebergement      = note_hebergement;
        this.latitude_hebergement  = latitude_hebergement;
        this.longitude_hebergement = longitude_hebergement;
        this.destination           = destination;
    }

    // ── Getters / Setters ──────────────────────────────────────────

    public int getId_hebergement() { return id_hebergement; }
    public void setId_hebergement(int id_hebergement) { this.id_hebergement = id_hebergement; }

    public String getNom_hebergement() { return nom_hebergement; }
    public void setNom_hebergement(String nom_hebergement) { this.nom_hebergement = nom_hebergement; }

    public String getType_hebergement() { return type_hebergement; }
    public void setType_hebergement(String type_hebergement) { this.type_hebergement = type_hebergement; }

    public Double getPrix_nuit_hebergement() { return prix_nuit_hebergement; }
    public void setPrix_nuit_hebergement(Double prix_nuit_hebergement) { this.prix_nuit_hebergement = prix_nuit_hebergement; }

    public String getAdresse_hebergement() { return adresse_hebergement; }
    public void setAdresse_hebergement(String adresse_hebergement) { this.adresse_hebergement = adresse_hebergement; }

    public Double getNote_hebergement() { return note_hebergement; }
    public void setNote_hebergement(Double note_hebergement) { this.note_hebergement = note_hebergement; }

    public Double getLatitude_hebergement() { return latitude_hebergement; }
    public void setLatitude_hebergement(Double latitude_hebergement) { this.latitude_hebergement = latitude_hebergement; }

    public Double getLongitude_hebergement() { return longitude_hebergement; }
    public void setLongitude_hebergement(Double longitude_hebergement) { this.longitude_hebergement = longitude_hebergement; }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }

    public Integer getAdded_by() { return added_by; }
    public void setAdded_by(Integer added_by) { this.added_by = added_by; }

    public String getAdded_by_name() { return added_by_name; }
    public void setAdded_by_name(String added_by_name) { this.added_by_name = added_by_name; }

    public String getImage_name() { return image_name; }
    public void setImage_name(String image_name) { this.image_name = image_name; }

    // ── Helpers ────────────────────────────────────────────────────

    /** Prix affiché : "-" si null ou 0. */
    public String getPrixFormatted() {
        if (prix_nuit_hebergement == null || prix_nuit_hebergement == 0.0) return "-";
        return String.format("%.2f €", prix_nuit_hebergement);
    }

    /** Note affichée : "-" si null ou 0. */
    public String getNoteFormatted() {
        if (note_hebergement == null || note_hebergement == 0.0) return "-";
        return String.format("%.1f ⭐", note_hebergement);
    }

    @Override
    public String toString() {
        return nom_hebergement + " (" + type_hebergement + ") - " +
                (destination != null ? destination.getNom_destination() : "Destination inconnue");
    }
}