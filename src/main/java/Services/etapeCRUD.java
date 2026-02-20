package Services;

import Entites.etape;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class etapeCRUD implements interfaceCRUD<etape> {
    Connection conn;

    public etapeCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(etape etape) throws SQLException {
        String req = "INSERT INTO etape (heure, description_etape, id_itineraire, id_activite) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setTime(1, etape.getHeure());
        pst.setString(2, etape.getDescription_etape());
        pst.setInt(3, etape.getId_itineraire());
        pst.setInt(4, etape.getId_activite());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            etape.setId_etape(rs.getInt(1));
        }
        System.out.println("Etape ajoutée avec ID: " + etape.getId_etape());
    }

    @Override
    public void modifier(etape etape) throws SQLException {
        String req = "UPDATE etape SET heure=?, description_etape=?, id_activite=? WHERE id_etape=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setTime(1, etape.getHeure());
        pst.setString(2, etape.getDescription_etape());
        pst.setInt(3, etape.getId_activite());
        pst.setInt(4, etape.getId_etape());
        pst.executeUpdate();
        System.out.println("Etape modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM etape WHERE id_etape=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
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
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        return getEtapesFromResultSet(rs);
    }

    public List<etape> getEtapesByItineraire(int id_itineraire) throws SQLException {
        String req = "SELECT e.*, " +
                "a.nom as nom_activite, a.lieu, a.description as description_activite, " +
                "a.duree, a.budget, a.niveaudifficulte, a.agemin, a.statut, " +
                "i.nom_itineraire " +
                "FROM etape e " +
                "LEFT JOIN activites a ON e.id_activite = a.id " +
                "LEFT JOIN itineraire i ON e.id_itineraire = i.id_itineraire " +
                "WHERE e.id_itineraire = ? " +
                "ORDER BY e.heure ASC";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id_itineraire);
        ResultSet rs = pst.executeQuery();
        return getEtapesFromResultSet(rs);
    }

    private List<etape> getEtapesFromResultSet(ResultSet rs) throws SQLException {
        List<etape> listeEtape = new ArrayList<>();
        while (rs.next()) {
            listeEtape.add(extractEtapeFromResultSet(rs));
        }
        return listeEtape;
    }

    private etape extractEtapeFromResultSet(ResultSet rs) throws SQLException {
        etape e = new etape();
        e.setId_etape(rs.getInt("id_etape"));
        e.setHeure(rs.getTime("heure"));
        e.setDescription_etape(rs.getString("description_etape"));
        e.setId_itineraire(rs.getInt("id_itineraire"));
        e.setId_activite(rs.getInt("id_activite"));

        e.setNomActivite(rs.getString("nom_activite"));
        e.setLieuActivite(rs.getString("lieu"));
        e.setDescriptionActivite(rs.getString("description_activite"));
        e.setDureeActivite(rs.getFloat("duree"));
        e.setBudgetActivite(rs.getFloat("budget"));
        e.setNiveauDifficulteActivite(rs.getInt("niveaudifficulte"));
        e.setAgeMinActivite(rs.getInt("agemin"));
        e.setStatutActivite(rs.getString("statut"));
        e.setNomItineraire(rs.getString("nom_itineraire"));

        return e;
    }

    public int getVoyageIdFromItineraire(int idItineraire) throws SQLException {
        String req = "SELECT id_voyage FROM itineraire WHERE id_itineraire = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, idItineraire);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("id_voyage");
        }
        return -1;
    }

    public List<ActiviteItem> getActivitesByVoyage(int idVoyage) throws SQLException {
        List<ActiviteItem> activites = new ArrayList<>();

        String req = "SELECT a.id, a.nom, a.lieu, a.description, a.duree, a.budget, a.niveaudifficulte " +
                "FROM activites a " +
                "JOIN liste_activite la ON a.id = la.id " +
                "WHERE la.id_voyage = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, idVoyage);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            activites.add(new ActiviteItem(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("lieu"),
                    rs.getString("description"),
                    rs.getFloat("duree"),
                    rs.getFloat("budget"),
                    rs.getInt("niveaudifficulte")
            ));
        }

        return activites;
    }

    public int getNombreJoursItineraire(int idItineraire) throws SQLException {
        Connection conn = MyBD.getInstance().getConn();
        String query = "SELECT v.date_debut, v.date_fin FROM itineraire i " +
                "JOIN voyage v ON i.id_voyage = v.id_voyage " +
                "WHERE i.id_itineraire = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, idItineraire);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Date dateDebut = rs.getDate("date_debut");
            Date dateFin = rs.getDate("date_fin");
            if (dateDebut != null && dateFin != null) {
                long diff = dateFin.getTime() - dateDebut.getTime();
                return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
            }
        }
        return 5;
    }

    // NOUVELLE MÉTHODE: Trier les étapes par heure
    public List<etape> trierParHeure(List<etape> etapes, boolean ascendant) {
        Comparator<etape> comparator = Comparator.comparing(e -> {
            if (e.getHeure() == null) return Time.valueOf("00:00:00");
            return e.getHeure();
        });

        if (!ascendant) {
            comparator = comparator.reversed();
        }

        return etapes.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    // NOUVELLE MÉTHODE: Trier les étapes par type d'activité (nom)
    public List<etape> trierParTypeActivite(List<etape> etapes, boolean ascendant) {
        Comparator<etape> comparator = Comparator.comparing(
                e -> e.getNomActivite() != null ? e.getNomActivite() : "",
                String.CASE_INSENSITIVE_ORDER
        );

        if (!ascendant) {
            comparator = comparator.reversed();
        }

        return etapes.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    // NOUVELLE MÉTHODE: Trier les étapes par durée
    public List<etape> trierParDuree(List<etape> etapes, boolean ascendant) {
        Comparator<etape> comparator = Comparator.comparing(
                e -> e.getDureeActivite() != null ? e.getDureeActivite() : 0f
        );

        if (!ascendant) {
            comparator = comparator.reversed();
        }

        return etapes.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public static class ActiviteItem {
        private int id;
        private String nom;
        private String lieu;
        private String description;
        private float duree;
        private float budget;
        private int niveauDifficulte;

        public ActiviteItem(int id, String nom, String lieu, String description, float duree, float budget, int niveauDifficulte) {
            this.id = id;
            this.nom = nom;
            this.lieu = lieu;
            this.description = description;
            this.duree = duree;
            this.budget = budget;
            this.niveauDifficulte = niveauDifficulte;
        }

        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getLieu() { return lieu; }
        public String getDescription() { return description; }
        public float getDuree() { return duree; }
        public float getBudget() { return budget; }
        public int getNiveauDifficulte() { return niveauDifficulte; }

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