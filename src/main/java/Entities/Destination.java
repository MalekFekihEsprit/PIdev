package Entities;

public class Destination {
    private int id_destination;
    private String nom_destination;
    private String pays_destination;
    private String description_destination;
    private String climat_destination;
    private String saison_destination;
    private double latitude_destination;
    private double longitude_destination;
    private double score_destination;
    private String currency_destination;
    private String flag_destination;
    private String languages_destination;
    private String video_url; // New field for YouTube video

    // Default constructor
    public Destination() {}

    // Constructor without ID (for new destinations)
    public Destination(String nom_destination, String pays_destination, String description_destination,
                       String climat_destination, String saison_destination) {
        this.nom_destination = nom_destination;
        this.pays_destination = pays_destination;
        this.description_destination = description_destination;
        this.climat_destination = climat_destination;
        this.saison_destination = saison_destination;
    }

    // Full constructor with ID
    public Destination(int id_destination, String nom_destination, String pays_destination,
                       String description_destination, String climat_destination, String saison_destination) {
        this.id_destination = id_destination;
        this.nom_destination = nom_destination;
        this.pays_destination = pays_destination;
        this.description_destination = description_destination;
        this.climat_destination = climat_destination;
        this.saison_destination = saison_destination;
    }

    // Getters and Setters
    public int getId_destination() {
        return id_destination;
    }

    public void setId_destination(int id_destination) {
        this.id_destination = id_destination;
    }

    public String getNom_destination() {
        return nom_destination;
    }

    public void setNom_destination(String nom_destination) {
        this.nom_destination = nom_destination;
    }

    public String getPays_destination() {
        return pays_destination;
    }

    public void setPays_destination(String pays_destination) {
        this.pays_destination = pays_destination;
    }

    public String getDescription_destination() {
        return description_destination;
    }

    public void setDescription_destination(String description_destination) {
        this.description_destination = description_destination;
    }

    public String getClimat_destination() {
        return climat_destination;
    }

    public void setClimat_destination(String climat_destination) {
        this.climat_destination = climat_destination;
    }

    public String getSaison_destination() {
        return saison_destination;
    }

    public void setSaison_destination(String saison_destination) {
        this.saison_destination = saison_destination;
    }

    public double getLatitude_destination() {
        return latitude_destination;
    }

    public void setLatitude_destination(double latitude_destination) {
        this.latitude_destination = latitude_destination;
    }

    public double getLongitude_destination() {
        return longitude_destination;
    }

    public void setLongitude_destination(double longitude_destination) {
        this.longitude_destination = longitude_destination;
    }

    public double getScore_destination() {
        return score_destination;
    }

    public void setScore_destination(double score_destination) {
        this.score_destination = score_destination;
    }

    public String getCurrency_destination() {
        return currency_destination;
    }

    public void setCurrency_destination(String currency_destination) {
        this.currency_destination = currency_destination;
    }

    public String getFlag_destination() {
        return flag_destination;
    }

    public void setFlag_destination(String flag_destination) {
        this.flag_destination = flag_destination;
    }

    public String getLanguages_destination() {
        return languages_destination;
    }

    public void setLanguages_destination(String languages_destination) {
        this.languages_destination = languages_destination;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    @Override
    public String toString() {
        return nom_destination + ", " + pays_destination;
    }
}