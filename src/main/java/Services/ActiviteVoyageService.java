package Services;

import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteVoyageService {

    public ActiviteVoyageService() {
    }

    /**
     * Vérifie si la connexion est valide, sinon la reconnecte
     */
    private Connection getValidConnection() throws SQLException {
        Connection con = MyBD.getInstance().getConn();
        try {
            // Vérifier si la connexion est fermée ou invalide
            if (con == null || con.isClosed() || !con.isValid(2)) {
                System.out.println("Connexion fermée, tentative de reconnexion...");
                // Forcer la création d'une nouvelle connexion
                MyBD.getInstance().setConn(null);
                con = MyBD.getInstance().getConn();
            }
        } catch (SQLException e) {
            System.out.println("Erreur de validation de connexion: " + e.getMessage());
            // Forcer une nouvelle connexion
            MyBD.getInstance().setConn(null);
            con = MyBD.getInstance().getConn();
        }
        return con;
    }

    public void associerActivitesAVoyage(int idVoyage, List<Integer> idsActivites) throws SQLException {
        if (idsActivites == null || idsActivites.isEmpty()) {
            System.out.println("Aucune activité à associer");
            return;
        }

        // Vérifier qu'aucun ID n'est null
        for (Integer id : idsActivites) {
            if (id == null) {
                System.err.println("ERREUR: ID d'activité null détecté!");
                throw new SQLException("ID d'activité invalide (null)");
            }
        }

        String query = "INSERT INTO liste_activite (id_voyage, id) VALUES (?, ?)";

        Connection con = getValidConnection();
        try (PreparedStatement pst = con.prepareStatement(query)) {
            for (int idActivite : idsActivites) {
                pst.setInt(1, idVoyage);
                pst.setInt(2, idActivite);
                pst.addBatch();
                System.out.println("Préparation insertion: voyage=" + idVoyage + ", activité=" + idActivite);
            }
            int[] results = pst.executeBatch();
            System.out.println(idsActivites.size() + " activité(s) associée(s) au voyage ID: " + idVoyage);
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'association: " + e.getMessage());
            throw e;
        }
    }

    public List<Integer> getActivitesIdsParVoyage(int idVoyage) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id FROM liste_activite WHERE id_voyage = ?";

        Connection con = getValidConnection();
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, idVoyage);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
            System.out.println("Activités trouvées pour le voyage " + idVoyage + " : " + ids);
        }
        return ids;
    }

    public void supprimerAssociationsParVoyage(int idVoyage) throws SQLException {
        String query = "DELETE FROM liste_activite WHERE id_voyage = ?";

        Connection con = getValidConnection();
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, idVoyage);
            int deleted = pst.executeUpdate();
            System.out.println(deleted + " association(s) supprimée(s) pour le voyage ID: " + idVoyage);
        }
    }

    public int compterActivitesParVoyage(int idVoyage) throws SQLException {
        String query = "SELECT COUNT(*) FROM liste_activite WHERE id_voyage = ?";

        Connection con = getValidConnection();
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, idVoyage);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}