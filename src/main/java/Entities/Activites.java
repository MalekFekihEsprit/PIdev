package Entities;

import java.time.LocalDate;

/**
 * Entité Activites — correspond à la table `activites` de la base commune.
 *
 * Colonnes originales (sprint 1) :
 *   id, nom, description, budget, niveaudifficulte, lieu, agemin, statut,
 *   duree, categorie_id, image_path
 *
 * Colonnes ajoutées lors du sprint web (sprint 2) :
 *   latitude  (DOUBLE, nullable)
 *   longitude (DOUBLE, nullable)
 *
 * Note : date_prevue n'existe pas dans la BD commune — elle était utilisée
 * uniquement côté Java pour l'affichage. Elle est conservée comme champ
 * transient (non persisté) pour ne pas casser le code existant.
 */
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

    // Catégorie
    private int categorieId;
    private Categories categorie;

    // Image
    private String imagePath;

    // ── NOUVEAUX CHAMPS (sprint web) ─────────────────────────────────

    /**
     * Latitude WGS84 de l'activité (nullable).
     * Permet le filtre « alentours » sur la carte front-office.
     */
    private Double latitude;

    /**
     * Longitude WGS84 de l'activité (nullable).
     */
    private Double longitude;

    // ── Champ local Java uniquement (non persisté en BD commune) ─────

    /**
     * Date prévue — utilisée uniquement côté Java pour l'affichage /
     * la logique de l'application. Ce champ n'a PAS de colonne en BD.
     * Il doit être exclu des requêtes SQL INSERT / UPDATE.
     */
    private LocalDate datePrevue;

    // ─── Constructeurs ────────────────────────────────────────────────

    public Activites() {}

    /**
     * Constructeur complet incluant les nouveaux champs géographiques.
     */
    public Activites(String nom, String description, int budget, String niveaudifficulte,
                     String lieu, int agemin, String statut, int duree, int categorieId,
                     String imagePath, Double latitude, Double longitude) {
        this.nom              = nom;
        this.description      = description;
        this.budget           = budget;
        this.niveaudifficulte = niveaudifficulte;
        this.lieu             = lieu;
        this.agemin           = agemin;
        this.statut           = statut;
        this.duree            = duree;
        this.categorieId      = categorieId;
        this.imagePath        = imagePath;
        this.latitude         = latitude;
        this.longitude        = longitude;
    }

    // Ancien constructeur conservé pour compatibilité ascendante
    public Activites(String nom, String description, int budget, String niveaudifficulte,
                     String lieu, int agemin, String statut, int duree, int categorieId,
                     String imagePath, LocalDate datePrevue) {
        this.nom              = nom;
        this.description      = description;
        this.budget           = budget;
        this.niveaudifficulte = niveaudifficulte;
        this.lieu             = lieu;
        this.agemin           = agemin;
        this.statut           = statut;
        this.duree            = duree;
        this.categorieId      = categorieId;
        this.imagePath        = imagePath;
        this.datePrevue       = datePrevue;
    }

    // ─── Getters / Setters ────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getBudget() { return budget; }
    public void setBudget(int budget) { this.budget = budget; }

    public String getNiveaudifficulte() { return niveaudifficulte; }
    public void setNiveaudifficulte(String niveaudifficulte) { this.niveaudifficulte = niveaudifficulte; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public int getAgemin() { return agemin; }
    public void setAgemin(int agemin) { this.agemin = agemin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public int getCategorieId() { return categorieId; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    public Categories getCategorie() { return categorie; }
    public void setCategorie(Categories categorie) { this.categorie = categorie; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // ── Géolocalisation (ajout sprint web) ───────────────────────────

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    // ── Date prévue (champ local Java uniquement) ────────────────────

    public LocalDate getDatePrevue() { return datePrevue; }
    public void setDatePrevue(LocalDate datePrevue) { this.datePrevue = datePrevue; }

    @Override
    public String toString() { return nom; }
}