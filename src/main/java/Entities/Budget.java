package Entities;

public class Budget {

    private int idBudget;          // id_budget
    private float montantTotal;    // montant_total
    private String deviseBudget;   // devise_budget
    private String statutBudget;   // statut_budget
    private String descriptionBudget; // description_budget
    private int id;                // id (index utilisateur)
    private int idVoyage;          // id_voyage (nullable, 0 si non défini)

    // Constructeur vide
    public Budget() { }

    // Constructeur pour création (sans idBudget)
    public Budget(float montantTotal, String deviseBudget, String statutBudget,
                  String descriptionBudget, int id, int idVoyage) {
        setMontantTotal(montantTotal);
        setDeviseBudget(deviseBudget);
        setStatutBudget(statutBudget);
        setDescriptionBudget(descriptionBudget);
        setId(id);
        setIdVoyage(idVoyage);
    }

    // Constructeur complet
    public Budget(int idBudget, float montantTotal, String deviseBudget, String statutBudget,
                  String descriptionBudget, int id, int idVoyage) {
        this(montantTotal, deviseBudget, statutBudget, descriptionBudget, id, idVoyage);
        setIdBudget(idBudget);
    }

    // ===== Getters & Setters avec contrôle de saisie =====

    public int getIdBudget() { return idBudget; }
    public void setIdBudget(int idBudget) {
        if (idBudget < 0) throw new IllegalArgumentException("idBudget doit être >= 0.");
        this.idBudget = idBudget;
    }

    public float getMontantTotal() { return montantTotal; }
    public void setMontantTotal(float montantTotal) {
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
    public void setStatutBudget(String statutBudget) {
        if (statutBudget == null || statutBudget.isEmpty())
            throw new IllegalArgumentException("Statut obligatoire.");
        if (!statutBudget.equalsIgnoreCase("ACTIF") && !statutBudget.equalsIgnoreCase("INACTIF"))
            throw new IllegalArgumentException("Statut doit être 'ACTIF' ou 'INACTIF'.");
        this.statutBudget = statutBudget.toUpperCase();
    }

    public String getDescriptionBudget() { return descriptionBudget; }
    public void setDescriptionBudget(String descriptionBudget) {
        // description peut être null ou vide
        this.descriptionBudget = descriptionBudget;
    }

    public int getId() { return id; }
    public void setId(int id) {
        if (id <= 0) throw new IllegalArgumentException("id utilisateur doit être positif.");
        this.id = id;
    }

    public int getIdVoyage() { return idVoyage; }
    public void setIdVoyage(int idVoyage) {
        // peut être null/0
        if (idVoyage < 0) throw new IllegalArgumentException("idVoyage doit être >= 0 si défini.");
        this.idVoyage = idVoyage;
    }

    // Méthode pour valider complètement l'objet avant insertion si besoin
    public void validate() {
        setMontantTotal(this.montantTotal);
        setDeviseBudget(this.deviseBudget);
        setStatutBudget(this.statutBudget);
        setId(this.id);
        setIdVoyage(this.idVoyage);
    }

    @Override
    public String toString() {
        return "Budget{" +
                "idBudget=" + idBudget +
                ", montantTotal=" + montantTotal +
                ", deviseBudget='" + deviseBudget + '\'' +
                ", statutBudget='" + statutBudget + '\'' +
                ", descriptionBudget='" + descriptionBudget + '\'' +
                ", id=" + id +
                ", idVoyage=" + idVoyage +
                '}';
    }
}
