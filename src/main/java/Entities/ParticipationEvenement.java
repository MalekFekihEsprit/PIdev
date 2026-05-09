package Entities;

import java.time.LocalDateTime;

/**
 * Entité ParticipationEvenement — correspond à la table `participation_evenement`.
 *
 * Colonnes BD :
 *   id, user_id, evenement_id, created_at
 *
 * Table créée dans le sprint web Symfony ; ajoutée ici pour l'intégration finale.
 */
public class ParticipationEvenement {

    private int id;

    /** FK vers la table user. */
    private int userId;

    /** FK vers la table evenement. */
    private int evenementId;

    /** Date/heure d'inscription (type DATETIME en BD). */
    private LocalDateTime createdAt;

    /**
     * Optionnel : objet Evenement complet pour éviter des requêtes supplémentaires
     * lors de l'affichage (lazy-loading manuel).
     */
    private Evenement evenement;

    // ─── Constructeurs ────────────────────────────────────────────────

    public ParticipationEvenement() {}

    public ParticipationEvenement(int userId, int evenementId) {
        this.userId      = userId;
        this.evenementId = evenementId;
        this.createdAt   = LocalDateTime.now();
    }

    public ParticipationEvenement(int id, int userId, int evenementId, LocalDateTime createdAt) {
        this.id           = id;
        this.userId       = userId;
        this.evenementId  = evenementId;
        this.createdAt    = createdAt;
    }

    // ─── Getters / Setters ────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getEvenementId() { return evenementId; }
    public void setEvenementId(int evenementId) { this.evenementId = evenementId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }

    // ─── Méthodes utilitaires ─────────────────────────────────────────

    @Override
    public String toString() {
        return "ParticipationEvenement{id=" + id
                + ", userId=" + userId
                + ", evenementId=" + evenementId
                + ", createdAt=" + createdAt + "}";
    }
}