package Entites;

public class Activites {
    private int id;
    private String nom;
    private String description;
    private int budget;
    private String niveaudifficulte;
    private String lieu;
    private int agemin;
    private String statut;
    private int duree;

    // Nouveaux attributs pour la catégorie
    private int categorieId;
    private Categories categorie;

    public Activites() {
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public String getNiveaudifficulte() {
        return niveaudifficulte;
    }

    public void setNiveaudifficulte(String niveaudifficulte) {
        this.niveaudifficulte = niveaudifficulte;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public int getAgemin() {
        return agemin;
    }

    public void setAgemin(int agemin) {
        this.agemin = agemin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    public Categories getCategorie() {
        return categorie;
    }

    public void setCategorie(Categories categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return nom;
    }
}