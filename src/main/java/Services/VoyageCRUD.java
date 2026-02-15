package Services;

import Entities.Voyage;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoyageCRUD {

    private Connection con;

    public VoyageCRUD() {
        con = MyBD.getInstance().getConn();
        System.out.println("Connected");
    }

    // Ajouter un voyage (avec statut)
    public void ajouter(Voyage v) throws SQLException {
        String req = "INSERT INTO voyage (titre_voyage, date_debut, date_fin, statut, id_destination) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setString(1, v.getTitre_voyage());
        pst.setDate(2, v.getDate_debut());
        pst.setDate(3, v.getDate_fin());
        pst.setString(4, v.getStatut() != null ? v.getStatut() : "a venir");
        pst.setInt(5, v.getId_destination());
        pst.executeUpdate();
    }

    // Afficher tous les voyages
    public List<Voyage> afficher() throws SQLException {
        List<Voyage> voyages = new ArrayList<>();
        String req = "SELECT * FROM voyage ORDER BY date_debut DESC";
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(req);

        while (rs.next()) {
            Voyage v = new Voyage(
                    rs.getInt("id_voyage"),
                    rs.getString("titre_voyage"),
                    rs.getDate("date_debut"),
                    rs.getDate("date_fin"),
                    rs.getString("statut"),
                    rs.getInt("id_destination")
            );
            voyages.add(v);
        }
        return voyages;
    }

    // Modifier un voyage
    public void modifier(Voyage v) throws SQLException {
        String req = "UPDATE voyage SET titre_voyage=?, date_debut=?, date_fin=?, statut=?, id_destination=? WHERE id_voyage=?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setString(1, v.getTitre_voyage());
        pst.setDate(2, v.getDate_debut());
        pst.setDate(3, v.getDate_fin());
        pst.setString(4, v.getStatut());
        pst.setInt(5, v.getId_destination());
        pst.setInt(6, v.getId_voyage());
        pst.executeUpdate();
    }

    // Supprimer un voyage
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM voyage WHERE id_voyage=?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    // Récupérer le nom d'une destination par son ID
    public String getNomDestination(int id) throws SQLException {
        String req = "SELECT nom_destination FROM destination WHERE id_destination=?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getString("nom_destination");  // Changé de "nom" à "nom_destination"
        }
        return "Destination inconnue";
    }
}