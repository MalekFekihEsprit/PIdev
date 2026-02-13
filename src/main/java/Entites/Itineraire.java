package Entites;

public class Itineraire {
    private int id_itineraire;
    private String nom_itineraire;
    private String description_itineraire;
    private int id_voyage;

    public Itineraire() {}

    public Itineraire(String nom_itineraire, String description_itineraire, int id_voyage) {
        this.nom_itineraire = nom_itineraire;
        this.description_itineraire = description_itineraire;
        this.id_voyage = id_voyage;
    }

    public int getId_itineraire() {
        return id_itineraire;
    }

    public String getNom_itineraire() {
        return nom_itineraire;
    }

    public String getDescription_itineraire() {
        return description_itineraire;
    }

    public int getId_voyage() {
        return id_voyage;
    }

    public void setId_itineraire(int id_itineraire) {
        this.id_itineraire = id_itineraire;
    }

    public void setNom_itineraire(String nom_itineraire) {
        this.nom_itineraire = nom_itineraire;
    }

    public void setDescription_itineraire(String description_itineraire) {
        this.description_itineraire = description_itineraire;
    }

    public void setId_voyage(int id_voyage) {
        this.id_voyage = id_voyage;
    }

    @Override
    public String toString() {
        return "Itineraire{" +
                "id_itineraire=" + id_itineraire +
                ", nom_itineraire='" + nom_itineraire + '\'' +
                ", description_itineraire='" + description_itineraire + '\'' +
                ", id_voyage=" + id_voyage +
                '}';
    }
}