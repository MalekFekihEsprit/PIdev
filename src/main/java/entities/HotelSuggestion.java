package entities;

public class HotelSuggestion {
    private String nom;
    private String adresse;
    private Double rating;
    private Double prixEstime;
    private Double latitude;
    private Double longitude;
    private boolean accepted = false;

    public HotelSuggestion(String nom, String adresse, Double rating, Double latitude, Double longitude) {
        this.nom = nom;
        this.adresse = adresse;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
        calculatePrixEstime();
    }

    private void calculatePrixEstime() {
        if (rating != null) {
            this.prixEstime = 50 + (rating * 25);
        } else {
            this.prixEstime = null;
        }
    }

    // Getters and setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) {
        this.rating = rating;
        calculatePrixEstime();
    }

    public Double getPrixEstime() { return prixEstime; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    @Override
    public String toString() {
        return nom + " - " + adresse;
    }
}