package Entities;

public class Categories {
    private int id;
    private String nom;
    private String description;
    private String type;
    private String saison;
    private String niveauintensite;
    private String publiccible;

    public Categories() {}
    public Categories(String nom, String  description, String type , String saison , String niveauintensite , String publiccible) {
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.saison = saison;
        this.niveauintensite = niveauintensite;
        this.publiccible = publiccible;
    }
    public Categories(int id ,String nom, String  description, String type , String saison , String niveauintensite , String publiccible) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.saison = saison;
        this.niveauintensite = niveauintensite;
        this.publiccible = publiccible;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSaison() {
        return saison;
    }

    public void setSaison(String saison) {
        this.saison = saison;
    }

    public String getNiveauintensite() {
        return niveauintensite;
    }

    public void setNiveauintensite(String niveauintensite) {
        this.niveauintensite = niveauintensite;
    }

    public String getPubliccible() {
        return publiccible;
    }

    public void setPubliccible(String publiccible) {
        this.publiccible = publiccible;
    }

    @Override
    public String toString() {
        return "Categories{" +
                "id='" + id  + '\'' +
                "nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", saison='" + saison + '\'' +
                ", niveauintensite='" + niveauintensite + '\'' +
                ", publiccible='" + publiccible + '\'' +
                '}';
    }
}
