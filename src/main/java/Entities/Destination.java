package Entities;

public class Destination {
    private int id_destination;
    private String nom_destination;
    private String pays_destination;
    private String description_destination;
    private String climat_destination;
    private String saison_ideale;
    private double altitude_destination;
    private double longitude_destination;
    private double score_destination;

    // Constructeur par défaut
    public Destination() {}

    // Constructeur sans ID (pour ajout)
    public Destination(String nom_destination, String pays_destination, String description_destination,
                       String climat_destination, String saison_ideale, double altitude_destination,
                       double longitude_destination, double score_destination) {
        this.nom_destination = nom_destination;
        this.pays_destination = pays_destination;
        this.description_destination = description_destination;
        this.climat_destination = climat_destination;
        this.saison_ideale = saison_ideale;
        this.altitude_destination = altitude_destination;
        this.longitude_destination = longitude_destination;
        this.score_destination = score_destination;
    }

    // Constructeur avec ID complet
    public Destination(int id_destination, String nom_destination, String pays_destination,
                       String description_destination, String climat_destination, String saison_ideale,
                       double altitude_destination, double longitude_destination, double score_destination) {
        this.id_destination = id_destination;
        this.nom_destination = nom_destination;
        this.pays_destination = pays_destination;
        this.description_destination = description_destination;
        this.climat_destination = climat_destination;
        this.saison_ideale = saison_ideale;
        this.altitude_destination = altitude_destination;
        this.longitude_destination = longitude_destination;
        this.score_destination = score_destination;
    }

    public int getId_destination() {
        return id_destination;
    }

    public String getNom_destination() {
        return nom_destination;
    }

    public String getClimat_destination() {
        return climat_destination;
    }

    public String getPays_destination() {
        return pays_destination;
    }

    public double getScore_destination() {
        return score_destination;
    }

    public double getLongitude_destination() {
        return longitude_destination;
    }

    public double getAltitude_destination() {
        return altitude_destination;
    }

    public String getSaison_ideale() {
        return saison_ideale;
    }

    public String getDescription_destination() {
        return description_destination;
    }

    public void setId_destination(int id_destination) {
        this.id_destination = id_destination;
    }

    public void setNom_destination(String nom_destination) {
        this.nom_destination = nom_destination;
    }

    public void setPays_destination(String pays_destination) {
        this.pays_destination = pays_destination;
    }

    public void setDescription_destination(String description_destination) {
        this.description_destination = description_destination;
    }

    public void setClimat_destination(String climat_destination) {
        this.climat_destination = climat_destination;
    }

    public void setSaison_ideale(String saison_ideale) {
        this.saison_ideale = saison_ideale;
    }

    public void setAltitude_destination(double altitude_destination) {
        this.altitude_destination = altitude_destination;
    }

    public void setLongitude_destination(double longitude_destination) {
        this.longitude_destination = longitude_destination;
    }

    @Override
    public String toString() {
        return "Destination{" +
                "id_destination=" + id_destination +
                ", nom_destination='" + nom_destination + '\'' +
                ", pays_destination='" + pays_destination + '\'' +
                ", description_destination='" + description_destination + '\'' +
                ", climat_destination='" + climat_destination + '\'' +
                ", saison_ideale='" + saison_ideale + '\'' +
                ", altitude_destination=" + altitude_destination +
                ", longitude_destination=" + longitude_destination +
                ", score_destination=" + score_destination +
                '}';
    }

    public void setScore_destination(double score_destination) {
        this.score_destination = score_destination;
    }


}
