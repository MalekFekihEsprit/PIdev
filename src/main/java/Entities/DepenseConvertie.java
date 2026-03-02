package Entities;

/**
 * Entities/DepenseConvertie.java
 * Représente une dépense avec son montant converti dans la devise cible.
 */
public class DepenseConvertie {

    private Depense depense;
    private double montantConverti;
    private String deviseCible;
    private double tauxUtilise;

    public DepenseConvertie(Depense depense, double montantConverti, String deviseCible, double tauxUtilise) {
        this.depense         = depense;
        this.montantConverti = montantConverti;
        this.deviseCible     = deviseCible;
        this.tauxUtilise     = tauxUtilise;
    }

    public Depense getDepense()          { return depense; }
    public double getMontantConverti()   { return montantConverti; }
    public String getDeviseCible()       { return deviseCible; }
    public double getTauxUtilise()       { return tauxUtilise; }

    public String getLibelle()           { return depense.getLibelleDepense(); }
    public double getMontantOriginal()   { return depense.getMontantDepense(); }
    public String getDeviseOriginale()   { return depense.getDeviseDepense(); }

    @Override
    public String toString() {
        return String.format("%s: %.2f %s → %.2f %s (taux: %.4f)",
                getLibelle(), getMontantOriginal(), getDeviseOriginale(),
                montantConverti, deviseCible, tauxUtilise);
    }
}