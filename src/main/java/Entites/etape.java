package Entites;

import java.sql.Time;

public class etape {
    private int id_etape;
    private Time heure;        // time en base de données
    private String lieu;
    private String description_etape;
    private int id_activite;
    private int id_itineraire;

    public etape() {}

    public etape(Time heure, String lieu, String description_etape, int id_activite, int id_itineraire) {
        this.heure = heure;
        this.lieu = lieu;
        this.description_etape = description_etape;
        this.id_activite = id_activite;
        this.id_itineraire = id_itineraire;
    }

    public int getId_etape() {
        return id_etape;
    }

    public Time getHeure() {
        return heure;
    }

    public String getLieu() {
        return lieu;
    }

    public String getDescription_etape() {
        return description_etape;
    }

    public int getId_activite() {
        return id_activite;
    }

    public int getId_itineraire() {
        return id_itineraire;
    }

    public void setId_etape(int id_etape) {
        this.id_etape = id_etape;
    }

    public void setHeure(Time heure) {
        this.heure = heure;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public void setDescription_etape(String description_etape) {
        this.description_etape = description_etape;
    }

    public void setId_activite(int id_activite) {
        this.id_activite = id_activite;
    }

    public void setId_itineraire(int id_itineraire) {
        this.id_itineraire = id_itineraire;
    }

    @Override
    public String toString() {
        return "etape{" +
                "id_etape=" + id_etape +
                ", heure=" + heure +
                ", lieu='" + lieu + '\'' +
                ", description_etape='" + description_etape + '\'' +
                ", id_activite=" + id_activite +
                ", id_itineraire=" + id_itineraire +
                '}';
    }
}