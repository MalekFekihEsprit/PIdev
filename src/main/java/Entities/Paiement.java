package Entities;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Paiement {
    private int id_paiement;
    private int id_voyage;
    private int id_utilisateur;
    private double montant;
    private String devise;
    private String methode; // "PAYPAL", "STRIPE", "CARTE"
    private String statut; // "EN_ATTENTE", "COMPLETE", "ECHEC", "REMBOURSE"
    private String transactionId;
    private Timestamp date_paiement;
    private String description;
    private String email_payeur;

    // Constructeurs
    public Paiement() {}

    public Paiement(int id_voyage, int id_utilisateur, double montant, String methode, String description, String email_payeur) {
        this.id_voyage = id_voyage;
        this.id_utilisateur = id_utilisateur;
        this.montant = montant;
        this.devise = "EUR";
        this.methode = methode;
        this.statut = "EN_ATTENTE";
        this.date_paiement = Timestamp.valueOf(LocalDateTime.now());
        this.description = description;
        this.email_payeur = email_payeur;
    }

    // Getters et Setters
    public int getId_paiement() { return id_paiement; }
    public void setId_paiement(int id_paiement) { this.id_paiement = id_paiement; }

    public int getId_voyage() { return id_voyage; }
    public void setId_voyage(int id_voyage) { this.id_voyage = id_voyage; }

    public int getId_utilisateur() { return id_utilisateur; }
    public void setId_utilisateur(int id_utilisateur) { this.id_utilisateur = id_utilisateur; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public String getMethode() { return methode; }
    public void setMethode(String methode) { this.methode = methode; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Timestamp getDate_paiement() { return date_paiement; }
    public void setDate_paiement(Timestamp date_paiement) { this.date_paiement = date_paiement; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmail_payeur() { return email_payeur; }
    public void setEmail_payeur(String email_payeur) { this.email_payeur = email_payeur; }
}