package Entities;

import java.time.LocalDateTime;

/**
 * Entité Avis — correspond à la table `avis` de la base commune.
 *
 * Colonnes BD :
 *   id, note, commentaire, created_at, user_id, activite_id, is_flagged
 *
 * Ajout sprint web : colonne `is_flagged` (tinyint, défaut 0).
 */
public class Avis {

    private int id;
    private int note;
    private String commentaire;
    private LocalDateTime createdAt;

    /** FK vers la table user */
    private int userId;

    /** FK vers la table activites */
    private int activiteId;

    /**
     * true = commentaire contient des gros mots / contenu inapproprié.
     * Masqué côté front-office (ajouté lors du sprint web Symfony).
     */
    private boolean isFlagged;

    // ─── Constructeurs ────────────────────────────────────────────────

    public Avis() {
        this.createdAt = LocalDateTime.now();
        this.isFlagged = false;
    }

    public Avis(int note, String commentaire, int userId, int activiteId) {
        this.note        = note;
        this.commentaire = commentaire;
        this.userId      = userId;
        this.activiteId  = activiteId;
        this.createdAt   = LocalDateTime.now();
        this.isFlagged   = false;
    }

    // ─── Getters / Setters ────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getActiviteId() { return activiteId; }
    public void setActiviteId(int activiteId) { this.activiteId = activiteId; }

    /** Indique si le commentaire a été marqué comme inapproprié. */
    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    // ─── toString ────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Avis{id=" + id + ", note=" + note + ", activiteId=" + activiteId
                + ", userId=" + userId + ", flagged=" + isFlagged + "}";
    }
}