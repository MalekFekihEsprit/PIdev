package Services;

import Entites.Itineraire;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class itineraireCRUD implements interfaceCRUD<Itineraire> {

    public itineraireCRUD() {
        // Pas besoin de stocker la connexion ici
    }

    @Override
    public void ajouter(Itineraire itin) throws SQLException {
        if (itin.getNom_itineraire().length() > 10) {
            throw new SQLException("Le nom de l'itinéraire ne doit pas dépasser 10 caractères");
        }

        Connection conn = null;
        PreparedStatement pstCheck = null;
        PreparedStatement pstInsert = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();

            String check = "SELECT COUNT(*) FROM itineraire WHERE nom_itineraire=? AND id_voyage=?";
            pstCheck = conn.prepareStatement(check);
            pstCheck.setString(1, itin.getNom_itineraire());
            pstCheck.setInt(2, itin.getId_voyage());
            rs = pstCheck.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                String req = "INSERT INTO itineraire (nom_itineraire, description_itineraire, id_voyage) VALUES (?, ?, ?)";
                pstInsert = conn.prepareStatement(req);
                pstInsert.setString(1, itin.getNom_itineraire());
                pstInsert.setString(2, itin.getDescription_itineraire());
                pstInsert.setInt(3, itin.getId_voyage());
                pstInsert.executeUpdate();

                System.out.println("Itinéraire ajouté !");
            } else {
                System.out.println("Itinéraire déjà existant pour ce voyage, ajout ignoré !");
            }
        } finally {
            // Fermer les ressources mais pas la connexion
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pstCheck != null) try { pstCheck.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pstInsert != null) try { pstInsert.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public void modifier(Itineraire itin) throws SQLException {
        if (itin.getNom_itineraire().length() > 10) {
            throw new SQLException("Le nom de l'itinéraire ne doit pas dépasser 10 caractères");
        }

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "UPDATE itineraire SET nom_itineraire=?, description_itineraire=?, id_voyage=? WHERE id_itineraire=?";
            pst = conn.prepareStatement(req);

            pst.setString(1, itin.getNom_itineraire());
            pst.setString(2, itin.getDescription_itineraire());
            pst.setInt(3, itin.getId_voyage());
            pst.setInt(4, itin.getId_itineraire());

            pst.executeUpdate();
            System.out.println("Itinéraire modifié");
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public void supprimer(int id_itineraire) throws SQLException {
        Connection conn = null;
        PreparedStatement pst = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "DELETE FROM itineraire WHERE id_itineraire=?";
            pst = conn.prepareStatement(req);
            pst.setInt(1, id_itineraire);
            pst.executeUpdate();
            System.out.println("Itinéraire supprimé");
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public List<Itineraire> afficher() throws SQLException {
        List<Itineraire> listeItineraire = new ArrayList<>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "SELECT * FROM itineraire";
            st = conn.createStatement();
            rs = st.executeQuery(req);

            while (rs.next()) {
                Itineraire itineraire = new Itineraire();
                itineraire.setId_itineraire(rs.getInt("id_itineraire"));
                itineraire.setNom_itineraire(rs.getString("nom_itineraire"));
                itineraire.setDescription_itineraire(rs.getString("description_itineraire"));
                itineraire.setId_voyage(rs.getInt("id_voyage"));

                listeItineraire.add(itineraire);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return listeItineraire;
    }

    public List<Itineraire> getItinerairesByVoyage(int id_voyage) throws SQLException {
        List<Itineraire> listeItineraire = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "SELECT * FROM itineraire WHERE id_voyage = ?";
            pst = conn.prepareStatement(req);
            pst.setInt(1, id_voyage);
            rs = pst.executeQuery();

            while (rs.next()) {
                Itineraire itineraire = new Itineraire();
                itineraire.setId_itineraire(rs.getInt("id_itineraire"));
                itineraire.setNom_itineraire(rs.getString("nom_itineraire"));
                itineraire.setDescription_itineraire(rs.getString("description_itineraire"));
                itineraire.setId_voyage(rs.getInt("id_voyage"));
                listeItineraire.add(itineraire);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pst != null) try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return listeItineraire;
    }
}