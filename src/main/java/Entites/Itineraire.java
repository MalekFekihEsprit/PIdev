package Entites;

public class Itineraire {
    private int id;
    private String nom;
    private String description;
    private int voyage_id;
    public Itineraire() {}
    public Itineraire(String nom, String description, int voyage_id) {
        this.nom = nom;
        this.description = description;
        this.voyage_id = voyage_id;
    }

    public int getId() {
        return id;
    }

    public int getVoyage_id() {
        return voyage_id;
    }

    public String getDescription() {
        return description;
    }

    public String getNom() {
        return nom;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVoyage_id(int voyage_id) {
        this.voyage_id = voyage_id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return "Itineraire{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", voyage_id=" + voyage_id +
                '}';
    }
}
