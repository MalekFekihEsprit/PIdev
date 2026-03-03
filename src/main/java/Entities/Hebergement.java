package Entities;

public class Hebergement {

    private int id_hebergement;
    private String nom_hebergement;
    private String type_hebergement;
    private double prixNuit_hebergement;
    private String adresse_hebergement;
    private double note_hebergement;
    private double latitude_hebergement;
    private double longitude_hebergement;
    private Destination destination; // association objet
    private int added_by; // User ID who added it (foreign key)
    private String added_by_name; // For display purposes

    public Hebergement() {}

    // Constructor with ID (for retrieving from DB)
    public Hebergement(int id_hebergement, String nom_hebergement, String type_hebergement,
                       double prixNuit_hebergement, String adresse_hebergement, double note_hebergement,
                       double latitude_hebergement, double longitude_hebergement,
                       Destination destination, int added_by) {
        this.id_hebergement = id_hebergement;
        this.nom_hebergement = nom_hebergement;
        this.type_hebergement = type_hebergement;
        this.prixNuit_hebergement = prixNuit_hebergement;
        this.adresse_hebergement = adresse_hebergement;
        this.note_hebergement = note_hebergement;
        this.latitude_hebergement = latitude_hebergement;
        this.longitude_hebergement = longitude_hebergement;
        this.destination = destination;
        this.added_by = added_by;
    }

    // Constructor without ID (for new hebergements) - USED IN AJOUTER
    public Hebergement(String nom_hebergement, String type_hebergement,
                       double prixNuit_hebergement, String adresse_hebergement,
                       double note_hebergement, double latitude_hebergement,
                       double longitude_hebergement, Destination destination) {
        this.nom_hebergement = nom_hebergement;
        this.type_hebergement = type_hebergement;
        this.prixNuit_hebergement = prixNuit_hebergement;
        this.adresse_hebergement = adresse_hebergement;
        this.note_hebergement = note_hebergement;
        this.latitude_hebergement = latitude_hebergement;
        this.longitude_hebergement = longitude_hebergement;
        this.destination = destination;
    }

    // Getters and Setters
    public int getId_hebergement() { return id_hebergement; }
    public void setId_hebergement(int id_hebergement) { this.id_hebergement = id_hebergement; }

    public String getNom_hebergement() { return nom_hebergement; }
    public void setNom_hebergement(String nom_hebergement) { this.nom_hebergement = nom_hebergement; }

    public String getType_hebergement() { return type_hebergement; }
    public void setType_hebergement(String type_hebergement) { this.type_hebergement = type_hebergement; }

    public double getPrixNuit_hebergement() { return prixNuit_hebergement; }
    public void setPrixNuit_hebergement(double prixNuit_hebergement) { this.prixNuit_hebergement = prixNuit_hebergement; }

    public String getAdresse_hebergement() { return adresse_hebergement; }
    public void setAdresse_hebergement(String adresse_hebergement) { this.adresse_hebergement = adresse_hebergement; }

    public double getNote_hebergement() { return note_hebergement; }
    public void setNote_hebergement(double note_hebergement) { this.note_hebergement = note_hebergement; }

    public double getLatitude_hebergement() { return latitude_hebergement; }
    public void setLatitude_hebergement(double latitude_hebergement) { this.latitude_hebergement = latitude_hebergement; }

    public double getLongitude_hebergement() { return longitude_hebergement; }
    public void setLongitude_hebergement(double longitude_hebergement) { this.longitude_hebergement = longitude_hebergement; }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }

    public int getAdded_by() { return added_by; }
    public void setAdded_by(int added_by) { this.added_by = added_by; }

    public String getAdded_by_name() { return added_by_name; }
    public void setAdded_by_name(String added_by_name) { this.added_by_name = added_by_name; }

    @Override
    public String toString() {
        return nom_hebergement + " (" + type_hebergement + ") - " +
                (destination != null ? destination.getNom_destination() : "Destination inconnue");
    }
}