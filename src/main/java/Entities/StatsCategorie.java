package Entities;

public class StatsCategorie {
    private CategorieDepense categorie;
    private double total;
    private int nombreDepenses;
    private double pourcentage;

    public StatsCategorie(CategorieDepense categorie) {
        this.categorie = categorie;
        this.total = 0;
        this.nombreDepenses = 0;
        this.pourcentage = 0;
    }

    public void ajouterDepense(double montant) {
        this.total += montant;
        this.nombreDepenses++;
    }

    public CategorieDepense getCategorie() { return categorie; }
    public double getTotal() { return total; }
    public int getNombreDepenses() { return nombreDepenses; }
    public double getPourcentage() { return pourcentage; }
    public void setPourcentage(double pourcentage) { this.pourcentage = pourcentage; }

    public String getIcone() { return categorie.getIcone(); }
    public String getLibelle() { return categorie.getLibelle(); }
    public String getCouleur() { return categorie.getCouleur(); }
}