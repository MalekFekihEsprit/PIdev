package Entities;

import java.math.BigDecimal;

public class Budget {

    private Long idBudget;
    private BigDecimal montantTotal;
    private String deviseBudget;
    private String statutBudget;
    private String descriptionBudget;

    // ✅ Constructor vide
    public Budget() {
    }

    // ✅ Constructor sans id (création)
    public Budget(BigDecimal montantTotal, String deviseBudget, String statutBudget,
                  String descriptionBudget) {
        this.montantTotal = montantTotal;
        this.deviseBudget = deviseBudget;
        this.statutBudget = statutBudget;
        this.descriptionBudget = descriptionBudget;
    }

    // ✅ Constructor complet
    public Budget(Long idBudget, BigDecimal montantTotal, String deviseBudget,
                  String statutBudget, String descriptionBudget) {
        this.idBudget = idBudget;
        this.montantTotal = montantTotal;
        this.deviseBudget = deviseBudget;
        this.statutBudget = statutBudget;
        this.descriptionBudget = descriptionBudget;
    }

    // ===== Getters & Setters =====

    public Long getIdBudget() {
        return idBudget;
    }

    public void setIdBudget(Long idBudget) {
        this.idBudget = idBudget;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getDeviseBudget() {
        return deviseBudget;
    }

    public void setDeviseBudget(String deviseBudget) {
        this.deviseBudget = deviseBudget;
    }

    public String getStatutBudget() {
        return statutBudget;
    }

    public void setStatutBudget(String statutBudget) {
        this.statutBudget = statutBudget;
    }

    public String getDescriptionBudget() {
        return descriptionBudget;
    }

    public void setDescriptionBudget(String descriptionBudget) {
        this.descriptionBudget = descriptionBudget;
    }


    @Override
    public String toString() {
        return "Budget{" +
                "idBudget=" + idBudget +
                ", montantTotal=" + montantTotal +
                ", deviseBudget='" + deviseBudget + '\'' +
                ", statutBudget='" + statutBudget + '\'' +
                ", descriptionBudget='" + descriptionBudget + '\'' +
                '}';
    }
}
