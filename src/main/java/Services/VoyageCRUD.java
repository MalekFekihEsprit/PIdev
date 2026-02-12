package Services;

import Entities.Voyage;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoyageCRUD implements InterfaceCRUD<Voyage> {
    Connection conn;

    public VoyageCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Voyage voyage) throws SQLException {
        String check = "SELECT COUNT(*) FROM voyage WHERE titre_voyage=? AND date_debut=? AND date_fin=?";
        PreparedStatement pstCheck = conn.prepareStatement(check);
        pstCheck.setString(1, voyage.getTitre_voyage());
        pstCheck.setDate(2, new java.sql.Date(voyage.getDate_debut().getTime()));
        pstCheck.setDate(3, new java.sql.Date(voyage.getDate_fin().getTime()));

        ResultSet rs = pstCheck.executeQuery();

        if (rs.next() && rs.getInt(1) == 0) {
            String req = "INSERT INTO voyage (titre_voyage, date_debut, date_fin, statut, id_destination) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pstInsert = conn.prepareStatement(req);
            pstInsert.setString(1, voyage.getTitre_voyage());
            pstInsert.setDate(2, new java.sql.Date(voyage.getDate_debut().getTime()));
            pstInsert.setDate(3, new java.sql.Date(voyage.getDate_fin().getTime()));
            pstInsert.setString(4, voyage.getStatut() != null ? voyage.getStatut() : "À venir");
            pstInsert.setInt(5, voyage.getId_destination());

            pstInsert.executeUpdate();
            System.out.println("Voyage ajouté !");
        } else {
            System.out.println("Voyage déjà existant, ajout ignoré !");
        }
    }

    @Override
    public void modifier(Voyage voyage) throws SQLException {
        String req = "UPDATE voyage SET titre_voyage=?, date_debut=?, date_fin=?, " +
                "statut=?, id_destination=? WHERE id_voyage=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, voyage.getTitre_voyage());
        pst.setDate(2, new java.sql.Date(voyage.getDate_debut().getTime()));
        pst.setDate(3, new java.sql.Date(voyage.getDate_fin().getTime()));
        pst.setString(4, voyage.getStatut() != null ? voyage.getStatut() : "À venir");
        pst.setInt(5, voyage.getId_destination());
        pst.setInt(6, voyage.getId_voyage());

        int rowsAffected = pst.executeUpdate();
        System.out.println("Voyage modifié, lignes affectées: " + rowsAffected);
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM voyage WHERE id_voyage=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);

        int rowsAffected = pst.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Voyage supprimé ! ID: " + id);
        } else {
            System.out.println("Aucun voyage trouvé avec l'ID: " + id);
        }
    }

    @Override
    public List<Voyage> afficher() throws SQLException {  // AJOUTER throws SQLException
        String req = "SELECT * FROM voyage ORDER BY date_debut DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<Voyage> listevoyages = new ArrayList<>();

        while (rs.next()) {
            Voyage v = new Voyage();
            v.setId_voyage(rs.getInt("id_voyage"));
            v.setTitre_voyage(rs.getString("titre_voyage"));
            v.setDate_debut(rs.getDate("date_debut"));
            v.setDate_fin(rs.getDate("date_fin"));
            v.setStatut(rs.getString("statut"));
            v.setId_destination(rs.getInt("id_destination"));

            listevoyages.add(v);
        }
        return listevoyages;
    }

    public String getNomDestination(int id_destination) throws SQLException {  // AJOUTER throws SQLException
        String req = "SELECT nom_destination FROM destination WHERE id_destination = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id_destination);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getString("nom_destination");
        }
        return "Destination inconnue";
    }
}