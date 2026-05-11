package Entities;

import java.time.LocalDateTime;

/**
 * Represents a row in the destination_voyage_notification table.
 * Created when a voyage linked to a destination is added/updated.
 */
public class VoyageNotification {

    private int id_notification;
    private int id_user;
    private int id_voyage;
    private LocalDateTime created_at;
    private boolean is_dismissed;

    // Joined fields (not stored in this table)
    private String titre_voyage;
    private String nom_destination;
    private String statut_voyage;
    private java.sql.Date date_debut;
    private java.sql.Date date_fin;

    public VoyageNotification() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId_notification()                    { return id_notification; }
    public void setId_notification(int v)              { this.id_notification = v; }

    public int getId_user()                            { return id_user; }
    public void setId_user(int v)                      { this.id_user = v; }

    public int getId_voyage()                          { return id_voyage; }
    public void setId_voyage(int v)                    { this.id_voyage = v; }

    public LocalDateTime getCreated_at()               { return created_at; }
    public void setCreated_at(LocalDateTime v)         { this.created_at = v; }

    public boolean isIs_dismissed()                    { return is_dismissed; }
    public void setIs_dismissed(boolean v)             { this.is_dismissed = v; }

    public String getTitre_voyage()                    { return titre_voyage; }
    public void setTitre_voyage(String v)              { this.titre_voyage = v; }

    public String getNom_destination()                 { return nom_destination; }
    public void setNom_destination(String v)           { this.nom_destination = v; }

    public String getStatut_voyage()                   { return statut_voyage; }
    public void setStatut_voyage(String v)             { this.statut_voyage = v; }

    public java.sql.Date getDate_debut()               { return date_debut; }
    public void setDate_debut(java.sql.Date v)         { this.date_debut = v; }

    public java.sql.Date getDate_fin()                 { return date_fin; }
    public void setDate_fin(java.sql.Date v)           { this.date_fin = v; }
}
