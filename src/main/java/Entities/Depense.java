package Entities;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Depense {

    private Long idDepense;
    private String libelleDepense;
    private BigDecimal montantDepense;
    private String categorieDepense;
    private String descriptionDepense;
    private String deviseDepense;
    private String typePaiement;
    private LocalDate dateCreation;
    private Long idBudget;

    // ✅ Constructeur vide
    public Depense() {
    }

    // ✅ Constructeur sans id (création)
    public Depense(String libelleDepense, BigDecimal montantDepense,
                   String categorieDepense, String descriptionDepense,
                   String deviseDepense, String typePaiement,
                   LocalDate dateCreation, Long idBudget) {

        this.libelleDepense = libelleDepense;
        this.montantDepense = montantDepense;
        this.categorieDepense = categorieDepense;
        this.descriptionDepense = descriptionDepense;
        this.deviseDepense = deviseDepense;
        this.typePaiement = typePaiement;
        this.dateCreation = dateCreation;
        this.idBudget = idBudget;
    }

    // ✅ Constructeur complet
    public Depense(Long idDepense, String libelleDepense, BigDecimal montantDepense,
                   String categorieDepense, String descriptionDepense,
                   String deviseDepense, String typePaiement,
                   LocalDate dateCreation, Long idBudget) {

        this.idDepense = idDepense;
        this.libelleDepense = libelleDepense;
        this.montantDepense = montantDepense;
        this.categorieDepense = categorieDepense;
        this.descriptionDepense = descriptionDepense;
        this.deviseDepense = deviseDepense;
        this.typePaiement = typePaiement;
        this.dateCreation = dateCreation;
        this.idBudget = idBudget;
    }

    // ===== Getters & Setters =====

    public Long getIdDepense() {
        return idDepense;
    }

    public void setIdDepense(Long idDepense) {
        this.idDepense = idDepense;
    }

    public String getLibelleDepense() {
        return libelleDepense;
    }

    public void setLibelleDepense(String libelleDepense) {
        this.libelleDepense = libelleDepense;
    }

    public BigDecimal getMontantDepense() {
        return montantDepense;
    }

    public void setMontantDepense(BigDecimal montantDepense) {
        this.montantDepense = montantDepense;
    }

    public String getCategorieDepense() {
        return categorieDepense;
    }

    public void setCategorieDepense(String categorieDepense) {
        this.categorieDepense = categorieDepense;
    }

    public String getDescriptionDepense() {
        return descriptionDepense;
    }

    public void setDescriptionDepense(String descriptionDepense) {
        this.descriptionDepense = descriptionDepense;
    }

    public String getDeviseDepense() {
        return deviseDepense;
    }

    public void setDeviseDepense(String deviseDepense) {
        this.deviseDepense = deviseDepense;
    }

    public String getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(String typePaiement) {
        this.typePaiement = typePaiement;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Long getIdBudget() {
        return idBudget;
    }

    public void setIdBudget(Long idBudget) {
        this.idBudget = idBudget;
    }

    @Override
    public String toString() {
        return "Depense{" +
                "idDepense=" + idDepense +
                ", libelleDepense='" + libelleDepense + '\'' +
                ", montantDepense=" + montantDepense +
                ", categorieDepense='" + categorieDepense + '\'' +
                ", deviseDepense='" + deviseDepense + '\'' +
                ", typePaiement='" + typePaiement + '\'' +
                ", dateCreation=" + dateCreation +
                ", idBudget=" + idBudget +
                '}';
    }
}
