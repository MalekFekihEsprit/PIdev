package Entities;

import java.sql.Date;
import java.time.LocalDate;

public class TimeCapsule {
    private int idCapsule;
    private int idBudget;
    private String libelleCapsule;
    private double montantInitial;
    private double montantAjuste;
    private String devise;
    private String destination;
    private String paysCode; // Code pays pour FRED (FR, US, etc.)
    private Date dateCreation;
    private Date dateReouverture;
    private boolean estReouverte;
    private String emailNotification;
    private double tauxInflationCalcule;

    // Constructeur
    public TimeCapsule(int idBudget, String libelleCapsule, double montantInitial,
                       String devise, String destination, String paysCode, String email) {
        this.idBudget = idBudget;
        this.libelleCapsule = libelleCapsule;
        this.montantInitial = montantInitial;
        this.devise = devise;
        this.destination = destination;
        this.paysCode = paysCode;
        this.emailNotification = email;
        this.dateCreation = Date.valueOf(LocalDate.now());
        this.dateReouverture = Date.valueOf(LocalDate.now().plusYears(1));
        this.estReouverte = false;
    }

    // Getters et Setters
    public int getIdCapsule() { return idCapsule; }
    public void setIdCapsule(int idCapsule) { this.idCapsule = idCapsule; }

    public int getIdBudget() { return idBudget; }
    public void setIdBudget(int idBudget) { this.idBudget = idBudget; }

    public String getLibelleCapsule() { return libelleCapsule; }
    public void setLibelleCapsule(String libelleCapsule) { this.libelleCapsule = libelleCapsule; }

    public double getMontantInitial() { return montantInitial; }
    public void setMontantInitial(double montantInitial) { this.montantInitial = montantInitial; }

    public double getMontantAjuste() { return montantAjuste; }
    public void setMontantAjuste(double montantAjuste) { this.montantAjuste = montantAjuste; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getPaysCode() { return paysCode; }
    public void setPaysCode(String paysCode) { this.paysCode = paysCode; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public Date getDateReouverture() { return dateReouverture; }
    public void setDateReouverture(Date dateReouverture) { this.dateReouverture = dateReouverture; }

    public boolean isEstReouverte() { return estReouverte; }
    public void setEstReouverte(boolean estReouverte) { this.estReouverte = estReouverte; }

    public String getEmailNotification() { return emailNotification; }
    public void setEmailNotification(String emailNotification) { this.emailNotification = emailNotification; }

    public double getTauxInflationCalcule() { return tauxInflationCalcule; }
    public void setTauxInflationCalcule(double taux) { this.tauxInflationCalcule = taux; }
}