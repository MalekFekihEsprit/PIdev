package Services;

import Entities.Voyage;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoyageCRUDV implements InterfaceCRUDV<Voyage> {

    public VoyageCRUDV() {
    }

    private Voyage mapResultSetToVoyage(ResultSet rs) throws SQLException {
        return new Voyage(
                rs.getInt("id_voyage"),
                rs.getString("titre_voyage"),
                rs.getDate("date_debut"),
                rs.getDate("date_fin"),
                rs.getString("statut"),
                rs.getInt("id_destination")
        );
    }

    /**
     * Vérifie si la connexion est valide, sinon force une nouvelle connexion
     */
    private Connection getValidConnection() throws SQLException {
        Connection con = null;
        try {
            con = MyBD.getInstance().getConn();

            // Vérifier si la connexion est null
            if (con == null) {
                System.out.println("Connexion null, création d'une nouvelle connexion...");
                MyBD.getInstance().setConn(null);
                con = MyBD.getInstance().getConn();
                if (con == null) {
                    throw new SQLException("Impossible d'établir une connexion à la base de données");
                }
                return con;
            }

            // Vérifier si la connexion est fermée
            if (con.isClosed()) {
                System.out.println("Connexion fermée, création d'une nouvelle connexion...");
                MyBD.getInstance().setConn(null);
                con = MyBD.getInstance().getConn();
                if (con == null) {
                    throw new SQLException("Impossible d'établir une nouvelle connexion");
                }
                return con;
            }

            // Vérifier si la connexion est valide avec un timeout de 2 secondes
            if (!con.isValid(2)) {
                System.out.println("Connexion invalide, création d'une nouvelle connexion...");
                MyBD.getInstance().setConn(null);
                con = MyBD.getInstance().getConn();
                if (con == null) {
                    throw new SQLException("Impossible d'établir une connexion valide");
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur de validation de connexion: " + e.getMessage());
            // Forcer une nouvelle connexion
            try {
                MyBD.getInstance().setConn(null);
                con = MyBD.getInstance().getConn();
                if (con == null) {
                    throw new SQLException("Échec de la reconnexion");
                }
            } catch (Exception ex) {
                throw new SQLException("Impossible de rétablir la connexion: " + ex.getMessage());
            }
        }
        return con;
    }

    private List<Voyage> executeVoyageQuery(String sql, Object... params) throws SQLException {
        List<Voyage> voyages = new ArrayList<>();
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = getValidConnection();
            pst = con.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                pst.setObject(i + 1, params[i]);
            }

            rs = pst.executeQuery();
            while (rs.next()) {
                voyages.add(mapResultSetToVoyage(rs));
            }

        } finally {
            // Fermer les ressources mais PAS la connexion
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (pst != null) {
                try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            // NE PAS fermer la connexion ici - elle est gérée par MyBD
        }

        return voyages;
    }

    @Override
    public void ajouter(Voyage v) throws SQLException {
        String req = "INSERT INTO voyage (titre_voyage, date_debut, date_fin, statut, id_destination) VALUES (?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = getValidConnection();
            pst = con.prepareStatement(req);

            pst.setString(1, v.getTitre_voyage());
            pst.setDate(2, v.getDate_debut());
            pst.setDate(3, v.getDate_fin());
            pst.setString(4, v.getStatut() != null ? v.getStatut() : "a venir");
            pst.setInt(5, v.getId_destination());

            pst.executeUpdate();

        } finally {
            if (pst != null) {
                try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public int ajouterEtRetournerId(Voyage v) throws SQLException {
        String req = "INSERT INTO voyage (titre_voyage, date_debut, date_fin, statut, id_destination) VALUES (?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = getValidConnection();
            pst = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

            pst.setString(1, v.getTitre_voyage());
            pst.setDate(2, v.getDate_debut());
            pst.setDate(3, v.getDate_fin());
            pst.setString(4, v.getStatut() != null ? v.getStatut() : "a venir");
            pst.setInt(5, v.getId_destination());

            pst.executeUpdate();

            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Échec de la récupération de l'ID généré");
            }

        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (pst != null) {
                try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public List<Voyage> afficher() throws SQLException {
        return executeVoyageQuery("SELECT * FROM voyage ORDER BY date_debut DESC");
    }

    @Override
    public void modifier(Voyage v) throws SQLException {
        String req = "UPDATE voyage SET titre_voyage=?, date_debut=?, date_fin=?, statut=?, id_destination=? WHERE id_voyage=?";
        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = getValidConnection();
            pst = con.prepareStatement(req);

            pst.setString(1, v.getTitre_voyage());
            pst.setDate(2, v.getDate_debut());
            pst.setDate(3, v.getDate_fin());
            pst.setString(4, v.getStatut());
            pst.setInt(5, v.getId_destination());
            pst.setInt(6, v.getId_voyage());

            pst.executeUpdate();

        } finally {
            if (pst != null) {
                try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM voyage WHERE id_voyage=?";
        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = getValidConnection();
            pst = con.prepareStatement(req);

            pst.setInt(1, id);
            pst.executeUpdate();

        } finally {
            if (pst != null) {
                try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public String getNomDestination(int id) throws SQLException {
        String req = "SELECT nom_destination FROM destination WHERE id_destination=?";
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = getValidConnection();
            pst = con.prepareStatement(req);

            pst.setInt(1, id);
            rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getString("nom_destination");
            }

        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (pst != null) {
                try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }

        return "Destination inconnue";
    }

    public List<Voyage> rechercherParTitre(String titre) throws SQLException {
        return executeVoyageQuery(
                "SELECT * FROM voyage WHERE titre_voyage LIKE ? ORDER BY date_debut DESC",
                "%" + titre + "%"
        );
    }

    public List<Voyage> rechercherParId(int id) throws SQLException {
        return executeVoyageQuery(
                "SELECT * FROM voyage WHERE id_voyage = ? ORDER BY date_debut DESC",
                id
        );
    }

    public List<Voyage> rechercherParStatut(String statut) throws SQLException {
        return executeVoyageQuery(
                "SELECT * FROM voyage WHERE statut = ? ORDER BY date_debut DESC",
                statut
        );
    }

    public List<Voyage> rechercherAvancee(String titre, String statut) throws SQLException {
        StringBuilder req = new StringBuilder("SELECT * FROM voyage WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (titre != null && !titre.trim().isEmpty()) {
            req.append(" AND titre_voyage LIKE ?");
            params.add("%" + titre.trim() + "%");
        }
        if (statut != null && !statut.trim().isEmpty() && !statut.equals("Tous")) {
            req.append(" AND statut = ?");
            params.add(statut);
        }
        req.append(" ORDER BY date_debut DESC");

        return executeVoyageQuery(req.toString(), params.toArray());
    }

    public int compterParStatut(String statut) throws SQLException {
        String req = "SELECT COUNT(*) FROM voyage WHERE statut = ?";
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = getValidConnection();
            pst = con.prepareStatement(req);

            pst.setString(1, statut);
            rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (pst != null) {
                try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }

        return 0;
    }

    public boolean destinationExiste(int idDestination) throws SQLException {
        String req = "SELECT COUNT(*) FROM destination WHERE id_destination = ?";
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = getValidConnection();
            pst = con.prepareStatement(req);

            pst.setInt(1, idDestination);
            rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (pst != null) {
                try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }

        return false;
    }
}