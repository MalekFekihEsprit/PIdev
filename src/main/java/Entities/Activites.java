package Entities;

import java.time.LocalDate;

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

    // Attribut pour l'image
    private String imagePath;

    // NOUVEL ATTRIBUT POUR LA DATE
    private LocalDate datePrevue;

    public Activites() {
    }

    // Constructeur avec tous les champs
    public Activites(String nom, String description, int budget, String niveaudifficulte,
                     String lieu, int agemin, String statut, int duree, int categorieId,
                     String imagePath, LocalDate datePrevue) {
        this.nom = nom;
        this.description = description;
        this.budget = budget;
        this.niveaudifficulte = niveaudifficulte;
        this.lieu = lieu;
        this.agemin = agemin;
        this.statut = statut;
        this.duree = duree;
        this.categorieId = categorieId;
        this.imagePath = imagePath;
        this.datePrevue = datePrevue;
    }

    // Getters et Setters existants
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // NOUVEAUX GETTER/SETTER POUR LA DATE
    public LocalDate getDatePrevue() {
        return datePrevue;
    }

    public void setDatePrevue(LocalDate datePrevue) {
        this.datePrevue = datePrevue;
    }

    @Override
    public String toString() {
        return nom;
    }
}