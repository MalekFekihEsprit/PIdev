package Entities;

public class Budget {

    private int idBudget;             // id_budget (PK, auto-increment)
    private String libelleBudget;     // libelle_budget
    private double montantTotal;      // montant_total
    private String deviseBudget;      // devise_budget (TND, EUR, etc.)
    private String statutBudget;      // statut_budget ("ACTIF" ou "INACTIF")
    private String descriptionBudget; // description_budget (optionnel)
    private int id;                   // id utilisateur (FK)
    private int idVoyage;             // id_voyage (0 si non défini)

    // ===== Constructeurs =====

    // Constructeur vide
    public Budget() { }

    // Constructeur pour création (sans idBudget)
    public Budget(String libelleBudget, double montantTotal, String deviseBudget, String statutBudget,
                  String descriptionBudget, int id, int idVoyage) {
        setLibelleBudget(libelleBudget);
        setMontantTotal(montantTotal);
        setDeviseBudget(deviseBudget);
        setStatutBudget(statutBudget);
        setDescriptionBudget(descriptionBudget);
        setId(id);
        setIdVoyage(idVoyage);
    }

    // Constructeur complet (avec idBudget)
    public Budget(int idBudget, String libelleBudget, double montantTotal, String deviseBudget, String statutBudget,
                  String descriptionBudget, int id, int idVoyage) {
        this(libelleBudget, montantTotal, deviseBudget, statutBudget, descriptionBudget, id, idVoyage);
        setIdBudget(idBudget);
    }


    // ===== Getters & Setters avec validation =====

    public int getIdBudget() { return idBudget; }
    public void setIdBudget(int idBudget) {
        if (idBudget < 0) throw new IllegalArgumentException("idBudget doit être >= 0.");
        this.idBudget = idBudget;
    }

    public String getLibelleBudget() { return libelleBudget; }
    public void setLibelleBudget(String libelleBudget) {
        if (libelleBudget == null || libelleBudget.isEmpty())
            throw new IllegalArgumentException("Libellé obligatoire.");
        if (libelleBudget.length() > 100)
            throw new IllegalArgumentException("Libellé ne doit pas dépasser 100 caractères.");
        this.libelleBudget = libelleBudget;
    }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) {
        if (montantTotal <= 0) throw new IllegalArgumentException("Montant total doit être positif.");
        this.montantTotal = montantTotal;
    }

    public String getDeviseBudget() { return deviseBudget; }
    public void setDeviseBudget(String deviseBudget) {
        if (deviseBudget == null || deviseBudget.isEmpty())
            throw new IllegalArgumentException("Devise obligatoire.");
        if (deviseBudget.length() != 3)
            throw new IllegalArgumentException("Devise doit avoir 3 caractères (ex: TND, EUR).");
        this.deviseBudget = deviseBudget.toUpperCase();
    }

    public String getStatutBudget() { return statutBudget; }
    // Dans Entities/Budget.java, modifiez la méthode setStatutBudget :

    public void setStatutBudget(String statutBudget) {
        if (statutBudget == null || statutBudget.isEmpty())
            throw new IllegalArgumentException("Statut obligatoire.");

        // Accepter PLUS de valeurs
        String statutUpper = statutBudget.toUpperCase();
        if (!statutUpper.equals("ACTIF") && !statutUpper.equals("INACTIF") &&
                !statutUpper.equals("TERMINE") && !statutUpper.equals("PLANIFIE") &&
                !statutUpper.equals("ENCOURS")) {
            throw new IllegalArgumentException("Statut doit être 'ACTIF', 'INACTIF', 'TERMINE', 'PLANIFIE' ou 'ENCOURS'.");
        }
        this.statutBudget = statutUpper;
    }

    public String getDescriptionBudget() { return descriptionBudget; }
    public void setDescriptionBudget(String descriptionBudget) {
        this.descriptionBudget = descriptionBudget; // optionnel, peut être null ou vide
    }

    public int getId() { return id; }
    public void setId(int id) {
        if (id <= 0) throw new IllegalArgumentException("id utilisateur doit être positif.");
        this.id = id;
    }

    public int getIdVoyage() { return idVoyage; }
    public void setIdVoyage(int idVoyage) {
        if (idVoyage < 0) throw new IllegalArgumentException("idVoyage doit être >= 0.");
        this.idVoyage = idVoyage;
    }

    // ===== Validation complète de l'objet =====
    public void validate() {
        setLibelleBudget(this.libelleBudget);
        setMontantTotal(this.montantTotal);
        setDeviseBudget(this.deviseBudget);
        setStatutBudget(this.statutBudget);
        setDescriptionBudget(this.descriptionBudget);
        setId(this.id);
        setIdVoyage(this.idVoyage);
    }

    // ===== toString =====
    @Override
    public String toString() {
        return "Budget{" +
                "idBudget=" + idBudget +
                ", libelleBudget='" + libelleBudget + '\'' +
                ", montantTotal=" + montantTotal +
                ", deviseBudget='" + deviseBudget + '\'' +
                ", statutBudget='" + statutBudget + '\'' +
                ", descriptionBudget='" + descriptionBudget + '\'' +
                ", id=" + id +
                ", idVoyage=" + idVoyage +
                '}';
    }
}
