package Entities;

import java.time.LocalDateTime;

/**
 * Entité Reservation — correspond à la table `reservations` de la base commune.
 *
 * Colonnes BD :
 *   id, activite_id, nom, prenom, telephone, email, commentaire,
 *   montant_total, acompte, statut_paiement, methode_confirmation,
 *   code_confirmation, qr_code_path, date_reservation,
 *   date_confirmation, date_paiement, transaction_id
 *
 * Table créée dans le sprint web Symfony ; ajoutée ici pour l'intégration finale.
 */
public class Reservation {

    private int id;

    /** FK vers la table activites. */
    private int activiteId;

    /** Optionnel : objet Activites complet. */
    private Activites activite;

    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String commentaire;

    /** Montant total de la réservation (en DT/€). */
    private double montantTotal;

    /** Acompte versé (généralement 30 % du montant total). */
    private double acompte;

    /**
     * Statut du paiement.
     * Valeurs possibles : "en_attente", "confirme"
     */
    private String statutPaiement;

    /**
     * Méthode de confirmation choisie par le client.
     * Valeurs possibles : "email", "sms"
     */
    private String methodeConfirmation;

    /**
     * Code à 5 chiffres envoyé par SMS pour confirmer la réservation.
     * Nullable (null si confirmation par email).
     */
    private String codeConfirmation;

    /** Chemin du QR code généré (nullable). */
    private String qrCodePath;

    /** Date/heure de création de la réservation. */
    private LocalDateTime dateReservation;

    /** Date/heure de confirmation effective (nullable). */
    private LocalDateTime dateConfirmation;

    /** Date/heure du paiement (nullable). */
    private LocalDateTime datePaiement;

    /** Identifiant de transaction Stripe / externe (nullable). */
    private String transactionId;

    // ─── Constructeurs ────────────────────────────────────────────────

    public Reservation() {
        this.dateReservation = LocalDateTime.now();
        this.statutPaiement  = "en_attente";
    }

    public Reservation(int activiteId, String nom, String prenom,
                       String telephone, String email,
                       double montantTotal, double acompte,
                       String methodeConfirmation) {
        this.activiteId           = activiteId;
        this.nom                  = nom;
        this.prenom               = prenom;
        this.telephone            = telephone;
        this.email                = email;
        this.montantTotal         = montantTotal;
        this.acompte              = acompte;
        this.methodeConfirmation  = methodeConfirmation;
        this.statutPaiement       = "en_attente";
        this.dateReservation      = LocalDateTime.now();
    }

    // ─── Getters / Setters ────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getActiviteId() { return activiteId; }
    public void setActiviteId(int activiteId) { this.activiteId = activiteId; }

    public Activites getActivite() { return activite; }
    public void setActivite(Activites activite) {
        this.activite = activite;
        if (activite != null) this.activiteId = activite.getId();
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    public double getAcompte() { return acompte; }
    public void setAcompte(double acompte) { this.acompte = acompte; }

    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }

    public String getMethodeConfirmation() { return methodeConfirmation; }
    public void setMethodeConfirmation(String methodeConfirmation) {
        this.methodeConfirmation = methodeConfirmation;
    }

    public String getCodeConfirmation() { return codeConfirmation; }
    public void setCodeConfirmation(String codeConfirmation) {
        this.codeConfirmation = codeConfirmation;
    }

    public String getQrCodePath() { return qrCodePath; }
    public void setQrCodePath(String qrCodePath) { this.qrCodePath = qrCodePath; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public LocalDateTime getDateConfirmation() { return dateConfirmation; }
    public void setDateConfirmation(LocalDateTime dateConfirmation) {
        this.dateConfirmation = dateConfirmation;
    }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    // ─── Méthodes utilitaires ─────────────────────────────────────────

    /** Retourne le nom complet (prénom + nom). */
    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    /** Indique si la réservation est confirmée. */
    public boolean isConfirmee() {
        return "confirme".equals(statutPaiement);
    }

    /**
     * Génère un code de confirmation aléatoire à 5 chiffres.
     * Le code est stocké dans {@code codeConfirmation} et retourné.
     */
    public String generateCodeConfirmation() {
        int code = (int)(Math.random() * 100000);
        this.codeConfirmation = String.format("%05d", code);
        return this.codeConfirmation;
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", activiteId=" + activiteId
                + ", client=" + getNomComplet()
                + ", statut=" + statutPaiement + "}";
    }
}