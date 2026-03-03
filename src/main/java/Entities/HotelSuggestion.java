package Entities;

public class HotelSuggestion {
    private String nom;
    private String adresse;
    private Double rating;
    private Double prixEstime;
    private double latitude;
    private double longitude;
    private boolean accepted;

    public HotelSuggestion(String nom, String adresse, Double rating, double latitude, double longitude) {
        this.nom = nom;
        this.adresse = adresse;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accepted = false;
    }

    public HotelSuggestion(String nom, String adresse, Double rating, Double prixEstime, double latitude, double longitude) {
        this.nom = nom;
        this.adresse = adresse;
        this.rating = rating;
        this.prixEstime = prixEstime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accepted = false;
    }

    // Getters and Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Double getPrixEstime() { return prixEstime; }
    public void setPrixEstime(Double prixEstime) { this.prixEstime = prixEstime; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
}