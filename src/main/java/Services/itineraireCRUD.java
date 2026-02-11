package Services;

import Entites.Itineraire;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class itineraireCRUD implements interfaceCRUD<Itineraire> {

    Connection conn;
    public itineraireCRUD() {
        conn = MyBD.getInstance().getConn();
    }
    @Override
    public void ajouter(Itineraire itin) throws SQLException {

        String check = "SELECT COUNT(*) FROM itineraire WHERE nom_itineraire=? AND id_voyage=?";
        PreparedStatement pstCheck = conn.prepareStatement(check);
        pstCheck.setString(1, itin.getNom());
        pstCheck.setInt(2, itin.getVoyage_id());
        ResultSet rs = pstCheck.executeQuery();

        if (rs.next() && rs.getInt(1) == 0) {
            String req = "INSERT INTO itineraire (nom_itineraire, description_itineraire, id_voyage) VALUES (?, ?, ?)";
            PreparedStatement pstInsert = conn.prepareStatement(req);
            pstInsert.setString(1, itin.getNom());
            pstInsert.setString(2, itin.getDescription());
            pstInsert.setInt(3, itin.getVoyage_id());
            pstInsert.executeUpdate();

            System.out.println("Itineraire ajoutée !");
        } else {
            System.out.println("Itineraire déjà existante, ajout ignoré !");
        }
    }

    @Override
    public void modifier(Itineraire itin) throws SQLException {
        String req = "update itineraire set nom_itineraire=?, description_itineraire=?, id_voyage=? where id_itineraire=?";
        PreparedStatement pst = conn.prepareStatement(req);

        pst.setString(1, itin.getNom());
        pst.setString(2, itin.getDescription());
        pst.setInt(3, itin.getVoyage_id());
        pst.setInt(4, itin.getId());

        pst.executeUpdate();
        System.out.println("Itineraire modifiée");
    }

    @Override
    public void supprimer(int id_itineraire) throws SQLException {
        String req = "delete from itineraire where id_itineraire=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id_itineraire);
        pst.executeUpdate();
        System.out.println("Itineraire supprimée");
    }

    @Override
    public List<Itineraire> afficher() throws SQLException {
        String req = "select * from itineraire";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Itineraire> listeItineraire = new ArrayList<>();

        while (rs.next()) {
            Itineraire p = new Itineraire();
            p.setId(rs.getInt("id_itineraire"));
            p.setNom(rs.getString("nom_itineraire"));
            p.setDescription(rs.getString("description_itineraire"));
            p.setVoyage_id(rs.getInt("id_voyage"));

            listeItineraire.add(p);
        }
        return listeItineraire;
    }
}