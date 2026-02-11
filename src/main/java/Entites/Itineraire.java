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

    public int getId() {
        return id_itineraire;
    }

    public int getVoyage_id() {
        return id_voyage;
    }

    public String getDescription() {
        return description_itineraire;
    }

    public String getNom() {
        return nom_itineraire;
    }

    public void setId(int id_itineraire) {
        this.id_itineraire = id_itineraire;
    }

    public void setVoyage_id(int id_voyage) {
        this.id_voyage = id_voyage;
    }

    public void setDescription(String description_itineraire) {
        this.description_itineraire = description_itineraire;
    }

    public void setNom(String nom_itineraire) {
        this.nom_itineraire = nom_itineraire;
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
