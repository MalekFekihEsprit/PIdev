package Services;

import Entities.etape;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FIX : la version précédente stockait la connexion dans this.conn au moment
 * du constructeur. Quand MyBD se reconnectait, etapeCRUD gardait l'ancienne
 * connexion fermée → "No operations allowed after connection closed".
 *
 * CORRECTION : on ne stocke PLUS la connexion. Chaque méthode appelle
 * MyBD.getInstance().getConn() juste avant de l'utiliser, ce qui garantit
 * toujours d'avoir la connexion courante et valide.
 * On ne ferme jamais la Connection (singleton) — uniquement Statement/ResultSet.
 */
public class etapeCRUD implements interfaceCRUDItine<etape> {

    // ✅ Plus de champ "Connection conn" — on appelle getConn() à chaque fois.

    public etapeCRUD() {
        // Rien à stocker
    }

    /** Raccourci pour obtenir la connexion active. */
    private Connection conn() {
        return MyBD.getInstance().getConn();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CRUD DE BASE
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void ajouter(etape etape) throws SQLException {
        String req = "INSERT INTO etape (heure, description_etape, id_itineraire, id_activite, numero_jour) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = conn().prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setTime(1, etape.getHeure());
        pst.setString(2, etape.getDescription_etape());
        pst.setInt(3, etape.getId_itineraire());
        pst.setInt(4, etape.getId_activite());
        pst.setInt(5, etape.getNumeroJour());
        pst.executeUpdate();

        try (ResultSet rs = pst.getGeneratedKeys()) {
            if (rs.next()) etape.setId_etape(rs.getInt(1));
        }
        pst.close();
        System.out.println("Etape ajoutée avec ID: " + etape.getId_etape() + " pour le jour " + etape.getNumeroJour());
    }

    @Override
    public void modifier(etape etape) throws SQLException {
        String req = "UPDATE etape SET heure=?, description_etape=?, id_activite=?, numero_jour=? WHERE id_etape=?";
        try (PreparedStatement pst = conn().prepareStatement(req)) {
            pst.setTime(1, etape.getHeure());
            pst.setString(2, etape.getDescription_etape());
            pst.setInt(3, etape.getId_activite());
            pst.setInt(4, etape.getNumeroJour());
            pst.setInt(5, etape.getId_etape());
            pst.executeUpdate();
        }
        System.out.println("Etape modifiée pour le jour " + etape.getNumeroJour());
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement pst = conn().prepareStatement("DELETE FROM etape WHERE id_etape=?")) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
        System.out.println("Etape supprimée");
    }

    @Override
    public List<etape> afficher() throws SQLException {
        String req = "SELECT e.*, " +
                "a.nom as nom_activite, a.lieu, a.description as description_activite, " +
                "a.duree, a.budget, a.niveaudifficulte, a.agemin, a.statut, " +
                "i.nom_itineraire " +
                "FROM etape e " +
                "LEFT JOIN activites a ON e.id_activite = a.id " +
                "LEFT JOIN itineraire i ON e.id_itineraire = i.id_itineraire " +
                "ORDER BY e.heure ASC";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(req)) {
            return getEtapesFromResultSet(rs);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  REQUÊTES SPÉCIFIQUES
    // ════════════════════════════════════════════════════════════════════════

    public List<etape> getEtapesByItineraire(int id_itineraire) throws SQLException {
        String req = "SELECT e.*, " +
                "a.nom as nom_activite, a.lieu, a.description as description_activite, " +
                "a.duree, a.budget, a.niveaudifficulte, a.agemin, a.statut, " +
                "i.nom_itineraire " +
                "FROM etape e " +
                "LEFT JOIN activites a ON e.id_activite = a.id " +
                "LEFT JOIN itineraire i ON e.id_itineraire = i.id_itineraire " +
                "WHERE e.id_itineraire = ? " +
                "ORDER BY e.numero_jour, e.heure ASC";
        try (PreparedStatement pst = conn().prepareStatement(req)) {
            pst.setInt(1, id_itineraire);
            try (ResultSet rs = pst.executeQuery()) {
                List<etape> etapes = new ArrayList<>();
                while (rs.next()) etapes.add(extractEtapeFromResultSet(rs));
                return etapes;
            }
        }
    }

    public List<etape> getEtapesByItineraireEtJour(int id_itineraire, int numeroJour) throws SQLException {
        String req = "SELECT e.*, " +
                "a.nom as nom_activite, a.lieu, a.description as description_activite, " +
                "a.duree, a.budget, a.niveaudifficulte, a.agemin, a.statut, " +
                "i.nom_itineraire " +
                "FROM etape e " +
                "LEFT JOIN activites a ON e.id_activite = a.id " +
                "LEFT JOIN itineraire i ON e.id_itineraire = i.id_itineraire " +
                "WHERE e.id_itineraire = ? AND e.numero_jour = ? " +
                "ORDER BY e.heure ASC";
        try (PreparedStatement pst = conn().prepareStatement(req)) {
            pst.setInt(1, id_itineraire);
            pst.setInt(2, numeroJour);
            try (ResultSet rs = pst.executeQuery()) {
                List<etape> etapes = new ArrayList<>();
                while (rs.next()) etapes.add(extractEtapeFromResultSet(rs));
                return etapes;
            }
        }
    }

    public int getVoyageIdFromItineraire(int idItineraire) throws SQLException {
        try (PreparedStatement pst = conn().prepareStatement(
                "SELECT id_voyage FROM itineraire WHERE id_itineraire = ?")) {
            pst.setInt(1, idItineraire);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("id_voyage");
            }
        }
        return -1;
    }

    public List<ActiviteItem> getActivitesByVoyage(int idVoyage) throws SQLException {
        List<ActiviteItem> activites = new ArrayList<>();
        String req = "SELECT a.id, a.nom, a.lieu, a.description, a.duree, a.budget, a.niveaudifficulte " +
                "FROM activites a " +
                "JOIN liste_activite la ON a.id = la.id " +
                "WHERE la.id_voyage = ?";
        try (PreparedStatement pst = conn().prepareStatement(req)) {
            pst.setInt(1, idVoyage);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String niveauDifficulte = rs.getString("niveaudifficulte");
                    int niveauInt = 0;
                    if (niveauDifficulte != null) {
                        switch (niveauDifficulte) {
                            case "Facile":   niveauInt = 1; break;
                            case "Moyen":    niveauInt = 2; break;
                            case "Difficile":niveauInt = 3; break;
                            case "Expert":   niveauInt = 4; break;
                        }
                    }
                    activites.add(new ActiviteItem(
                            rs.getInt("id"), rs.getString("nom"), rs.getString("lieu"),
                            rs.getString("description"), rs.getFloat("duree"),
                            rs.getFloat("budget"), niveauInt));
                }
            }
        }
        return activites;
    }

    public int getNombreJoursItineraire(int idItineraire) throws SQLException {
        String query = "SELECT v.date_debut, v.date_fin FROM itineraire i " +
                "JOIN voyage v ON i.id_voyage = v.id_voyage " +
                "WHERE i.id_itineraire = ?";
        try (PreparedStatement stmt = conn().prepareStatement(query)) {
            stmt.setInt(1, idItineraire);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date dateDebut = rs.getDate("date_debut");
                    Date dateFin   = rs.getDate("date_fin");
                    if (dateDebut != null && dateFin != null) {
                        long diff = dateFin.getTime() - dateDebut.getTime();
                        return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
                    }
                }
            }
        }
        return 5;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EXTRACTION & TRI
    // ════════════════════════════════════════════════════════════════════════

    private List<etape> getEtapesFromResultSet(ResultSet rs) throws SQLException {
        List<etape> liste = new ArrayList<>();
        while (rs.next()) liste.add(extractEtapeFromResultSet(rs));
        return liste;
    }

    private etape extractEtapeFromResultSet(ResultSet rs) throws SQLException {
        etape e = new etape();
        e.setId_etape(rs.getInt("id_etape"));
        e.setHeure(rs.getTime("heure"));
        e.setDescription_etape(rs.getString("description_etape"));
        e.setId_itineraire(rs.getInt("id_itineraire"));
        e.setId_activite(rs.getInt("id_activite"));

        try { e.setNumeroJour(rs.getInt("numero_jour")); }
        catch (SQLException ex) { e.setNumeroJour(1); }

        e.setNomActivite(rs.getString("nom_activite"));
        e.setLieuActivite(rs.getString("lieu"));
        e.setDescriptionActivite(rs.getString("description_activite"));
        e.setDureeActivite(rs.getFloat("duree"));
        e.setBudgetActivite(rs.getFloat("budget"));

        String niveauTexte = rs.getString("niveaudifficulte");
        int niveauInt = 0;
        if (niveauTexte != null) {
            switch (niveauTexte) {
                case "Facile":    niveauInt = 1; break;
                case "Moyen":     niveauInt = 2; break;
                case "Difficile": niveauInt = 3; break;
                case "Expert":    niveauInt = 4; break;
            }
        }
        e.setNiveauDifficulteActivite(niveauInt);
        e.setAgeMinActivite(rs.getInt("agemin"));
        e.setStatutActivite(rs.getString("statut"));
        e.setNomItineraire(rs.getString("nom_itineraire"));
        return e;
    }

    public List<etape> trierParHeure(List<etape> etapes, boolean ascendant) {
        Comparator<etape> cmp = Comparator.comparing(
                e -> e.getHeure() != null ? e.getHeure() : Time.valueOf("00:00:00"));
        if (!ascendant) cmp = cmp.reversed();
        return etapes.stream().sorted(cmp).collect(Collectors.toList());
    }

    public List<etape> trierParTypeActivite(List<etape> etapes, boolean ascendant) {
        Comparator<etape> cmp = Comparator.comparing(
                e -> e.getNomActivite() != null ? e.getNomActivite() : "",
                String.CASE_INSENSITIVE_ORDER);
        if (!ascendant) cmp = cmp.reversed();
        return etapes.stream().sorted(cmp).collect(Collectors.toList());
    }

    public List<etape> trierParDuree(List<etape> etapes, boolean ascendant) {
        Comparator<etape> cmp = Comparator.comparing(
                e -> e.getDureeActivite() != null ? e.getDureeActivite() : 0f);
        if (!ascendant) cmp = cmp.reversed();
        return etapes.stream().sorted(cmp).collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INNER CLASS ActiviteItem
    // ════════════════════════════════════════════════════════════════════════

    public static class ActiviteItem {
        private final int    id;
        private final String nom;
        private final String lieu;
        private final String description;
        private final float  duree;
        private final float  budget;
        private final int    niveauDifficulte;

        public ActiviteItem(int id, String nom, String lieu, String description,
                            float duree, float budget, int niveauDifficulte) {
            this.id = id; this.nom = nom; this.lieu = lieu;
            this.description = description; this.duree = duree;
            this.budget = budget; this.niveauDifficulte = niveauDifficulte;
        }

        public int    getId()               { return id; }
        public String getNom()              { return nom; }
        public String getLieu()             { return lieu; }
        public String getDescription()      { return description; }
        public float  getDuree()            { return duree; }
        public float  getBudget()           { return budget; }
        public int    getNiveauDifficulte() { return niveauDifficulte; }

        public String getNiveauTexte() {
            switch (niveauDifficulte) {
                case 1: return "Facile";
                case 2: return "Moyen";
                case 3: return "Difficile";
                case 4: return "Expert";
                default: return "Niveau " + niveauDifficulte;
            }
        }

        @Override
        public String toString() {
            return nom + " (" + lieu + ") - " + getNiveauTexte();
        }
    }
}