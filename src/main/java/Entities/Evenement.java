package Entities;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entité Evenement — correspond à la table `evenement` de la base commune.
 *
 * Colonnes BD :
 *   id, titre, description, date, heure, lieu,
 *   nb_places, lien_groupe, image_path,
 *   latitude, longitude, telegram_group_id
 *
 * Table créée dans le sprint web Symfony ; ajoutée ici pour l'intégration finale.
 */
public class Evenement {

    private int id;
    private String titre;
    private String description;

    /** Date de l'événement (type DATE en BD). */
    private LocalDate date;

    /** Heure de l'événement (type TIME en BD). */
    private LocalTime heure;

    private String lieu;

    /** Nombre maximum de places disponibles. */
    private int nbPlaces;

    /**
     * Lien d'invitation Telegram (ou autre groupe).
     * Nullable — tous les événements n'ont pas de groupe.
     */
    private String lienGroupe;

    /** Chemin relatif vers l'image de l'événement (nullable). */
    private String imagePath;

    /** Latitude WGS84 (nullable). */
    private Double latitude;

    /** Longitude WGS84 (nullable). */
    private Double longitude;

    /**
     * Identifiant interne du groupe Telegram (ex: -100123456789).
     * Nullable.
     */
    private String telegramGroupId;

    // ─── Constructeurs ────────────────────────────────────────────────

    public Evenement() {}

    public Evenement(String titre, String description, LocalDate date, LocalTime heure,
                     String lieu, int nbPlaces) {
        this.titre       = titre;
        this.description = description;
        this.date        = date;
        this.heure       = heure;
        this.lieu        = lieu;
        this.nbPlaces    = nbPlaces;
    }

    // ─── Getters / Setters ────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHeure() { return heure; }
    public void setHeure(LocalTime heure) { this.heure = heure; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }

    public String getLienGroupe() { return lienGroupe; }
    public void setLienGroupe(String lienGroupe) { this.lienGroupe = lienGroupe; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getTelegramGroupId() { return telegramGroupId; }
    public void setTelegramGroupId(String telegramGroupId) { this.telegramGroupId = telegramGroupId; }

    // ─── Méthodes utilitaires ─────────────────────────────────────────

    /**
     * Calcule le nombre de places restantes en soustrayant le nombre
     * de participations passé en paramètre.
     *
     * @param nbParticipations nombre actuel de participants inscrits
     * @return places restantes (minimum 0)
     */
    public int getPlacesRestantes(int nbParticipations) {
        return Math.max(0, nbPlaces - nbParticipations);
    }

    /**
     * Indique si l'événement est complet.
     *
     * @param nbParticipations nombre actuel de participants
     */
    public boolean isComplet(int nbParticipations) {
        return getPlacesRestantes(nbParticipations) == 0;
    }

    @Override
    public String toString() {
        return titre != null ? titre : "Evenement#" + id;
    }
}