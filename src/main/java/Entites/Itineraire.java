package Entites;

public class Itineraire {
    private int id_itineraire;
    private String nom_itineraire;        // Correspond au nom dans la base
    private String description_itineraire; // Correspond au nom dans la base
    private int id_voyage;
    private int nombre_jour;              // Attribut manquant

    public Itineraire() {}

    public Itineraire(String nom_itineraire, String description_itineraire, int id_voyage, int nombre_jour) {
        this.nom_itineraire = nom_itineraire;
        this.description_itineraire = description_itineraire;
        this.id_voyage = id_voyage;
        this.nombre_jour = nombre_jour;
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

    public int getNombre_jour() {
        return nombre_jour;
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

    public void setNombre_jour(int nombre_jour) {
        this.nombre_jour = nombre_jour;
    }

    @Override
    public String toString() {
        return "Itineraire{" +
                "id_itineraire=" + id_itineraire +
                ", nom_itineraire='" + nom_itineraire + '\'' +
                ", description_itineraire='" + description_itineraire + '\'' +
                ", id_voyage=" + id_voyage +
                ", nombre_jour=" + nombre_jour +
                '}';
    }
}