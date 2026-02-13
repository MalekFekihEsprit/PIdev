package Entities;

import java.sql.Date;

public class Depense {

    private int idDepense;             // id_depense
    private float montantDepense;      // montant_depense
    private String libelleDepense;     // libelle_depense
    private String categorieDepense;   // categorie_depense
    private String descriptionDepense; // description_depense
    private String deviseDepense;      // devise_depense (nullable)
    private String typePaiement;       // type_paiement
    private Date dateCreation;         // date_creation
    private int idBudget;              // id_budget (FK)

    // Constructeur vide
    public Depense() { }

    // Constructeur pour création (sans idDepense)
    public Depense(float montantDepense, String libelleDepense, String categorieDepense,
                   String descriptionDepense, String deviseDepense, String typePaiement,
                   Date dateCreation, int idBudget) {
        setMontantDepense(montantDepense);
        setLibelleDepense(libelleDepense);
        setCategorieDepense(categorieDepense);
        setDescriptionDepense(descriptionDepense);
        setDeviseDepense(deviseDepense);
        setTypePaiement(typePaiement);
        setDateCreation(dateCreation);
        setIdBudget(idBudget);
    }

    // Constructeur complet
    public Depense(int idDepense, float montantDepense, String libelleDepense, String categorieDepense,
                   String descriptionDepense, String deviseDepense, String typePaiement,
                   Date dateCreation, int idBudget) {
        this(montantDepense, libelleDepense, categorieDepense, descriptionDepense,
                deviseDepense, typePaiement, dateCreation, idBudget);
        setIdDepense(idDepense);
    }

    // ===== Getters & Setters avec contrôle =====
    public int getIdDepense() { return idDepense; }
    public void setIdDepense(int idDepense) {
        if (idDepense < 0) throw new IllegalArgumentException("idDepense doit être >= 0.");
        this.idDepense = idDepense;
    }

    public float getMontantDepense() { return montantDepense; }
    public void setMontantDepense(float montantDepense) {
        if (montantDepense <= 0) throw new IllegalArgumentException("Montant dépense doit être positif.");
        this.montantDepense = montantDepense;
    }

    public String getLibelleDepense() { return libelleDepense; }
    public void setLibelleDepense(String libelleDepense) {
        if (libelleDepense == null || libelleDepense.isEmpty())
            throw new IllegalArgumentException("Libellé dépense obligatoire.");
        if (libelleDepense.length() > 100)
            throw new IllegalArgumentException("Libellé trop long (max 100 caractères).");
        this.libelleDepense = libelleDepense;
    }

    public String getCategorieDepense() { return categorieDepense; }
    public void setCategorieDepense(String categorieDepense) {
        if (categorieDepense == null || categorieDepense.isEmpty())
            throw new IllegalArgumentException("Catégorie dépense obligatoire.");
        if (categorieDepense.length() > 50)
            throw new IllegalArgumentException("Catégorie trop longue (max 50 caractères).");
        this.categorieDepense = categorieDepense;
    }

    public String getDescriptionDepense() { return descriptionDepense; }
    public void setDescriptionDepense(String descriptionDepense) {
        if (descriptionDepense == null || descriptionDepense.isEmpty())
            throw new IllegalArgumentException("Description dépense obligatoire.");
        this.descriptionDepense = descriptionDepense;
    }

    public String getDeviseDepense() { return deviseDepense; }
    public void setDeviseDepense(String deviseDepense) {
        if (deviseDepense != null && !deviseDepense.isEmpty() && deviseDepense.length() != 3)
            throw new IllegalArgumentException("Devise doit avoir 3 caractères (ex: TND, EUR).");
        this.deviseDepense = (deviseDepense != null) ? deviseDepense.toUpperCase() : null;
    }

    public String getTypePaiement() { return typePaiement; }
    public void setTypePaiement(String typePaiement) {
        if (typePaiement == null || typePaiement.isEmpty())
            throw new IllegalArgumentException("Type de paiement obligatoire.");
        if (typePaiement.length() > 30)
            throw new IllegalArgumentException("Type paiement trop long (max 30 caractères).");
        this.typePaiement = typePaiement;
    }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) {
        if (dateCreation == null)
            throw new IllegalArgumentException("Date de création obligatoire.");
        Date today = Date.valueOf(java.time.LocalDate.now());
        if (dateCreation.after(today))
            throw new IllegalArgumentException("Date de création ne peut pas être dans le futur.");
        this.dateCreation = dateCreation;
    }

    public int getIdBudget() { return idBudget; }
    public void setIdBudget(int idBudget) {
        if (idBudget <= 0)
            throw new IllegalArgumentException("idBudget doit être un entier positif existant.");
        this.idBudget = idBudget;
    }

    // Méthode pour valider l'objet complet
    public void validate() {
        setMontantDepense(this.montantDepense);
        setLibelleDepense(this.libelleDepense);
        setCategorieDepense(this.categorieDepense);
        setDescriptionDepense(this.descriptionDepense);
        setDeviseDepense(this.deviseDepense);
        setTypePaiement(this.typePaiement);
        setDateCreation(this.dateCreation);
        setIdBudget(this.idBudget);
    }

    @Override
    public String toString() {
        return "Depense{" +
                "idDepense=" + idDepense +
                ", montantDepense=" + montantDepense +
                ", libelleDepense='" + libelleDepense + '\'' +
                ", categorieDepense='" + categorieDepense + '\'' +
                ", descriptionDepense='" + descriptionDepense + '\'' +
                ", deviseDepense='" + deviseDepense + '\'' +
                ", typePaiement='" + typePaiement + '\'' +
                ", dateCreation=" + dateCreation +
                ", idBudget=" + idBudget +
                '}';
    }
}
