package Entities;

import java.sql.Date;
import java.util.Objects;

public class Voyage {
    private int id_voyage;
    private String titre_voyage;
    private Date date_debut;    // java.sql.Date
    private Date date_fin;      // java.sql.Date
    private String statut;
    private int id_destination;

    // Constructeurs
    public Voyage() {}
    public Voyage(String titre_voyage, Date date_debut, Date date_fin, int id_destination) {
        this.titre_voyage = titre_voyage;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.statut = "a venir"; // default statut
        this.id_destination = id_destination;
    }

    public Voyage(String titre_voyage, Date date_debut, Date date_fin, String statut, int id_destination) {
        this.titre_voyage = titre_voyage;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.statut = statut;
        this.id_destination = id_destination;
    }

    public Voyage(int id_voyage, String titre_voyage, Date date_debut, Date date_fin, String statut, int id_destination) {
        this.id_voyage = id_voyage;
        this.titre_voyage = titre_voyage;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.statut = statut;
        this.id_destination = id_destination;
    }

    // Getters et Setters
    public int getId_voyage() { return id_voyage; }
    public void setId_voyage(int id_voyage) { this.id_voyage = id_voyage; }

    public String getTitre_voyage() { return titre_voyage; }
    public void setTitre_voyage(String titre_voyage) { this.titre_voyage = titre_voyage; }

    public Date getDate_debut() { return date_debut; }
    public void setDate_debut(Date date_debut) { this.date_debut = date_debut; }

    public Date getDate_fin() { return date_fin; }
    public void setDate_fin(Date date_fin) { this.date_fin = date_fin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getId_destination() { return id_destination; }
    public void setId_destination(int id_destination) { this.id_destination = id_destination; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Voyage voyage = (Voyage) o;
        return id_voyage == voyage.id_voyage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_voyage);
    }

    @Override
    public String toString() {
        return "Voyage{" +
                "id_voyage=" + id_voyage +
                ", titre_voyage='" + titre_voyage + '\'' +
                ", date_debut=" + date_debut +
                ", date_fin=" + date_fin +
                ", statut='" + statut + '\'' +
                ", id_destination=" + id_destination +
                '}';
    }
}