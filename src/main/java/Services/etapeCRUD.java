package Services;

import Entites.etape;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class etapeCRUD implements interfaceCRUD<etape> {
    Connection conn;
    public etapeCRUD() {
        conn = MyBD.getInstance().getConn();
    }
    @Override
    public void ajouter(etape etape) throws SQLException {
        String check = "SELECT COUNT(*) FROM etape WHERE jour=? AND lieu=? AND id_itineraire=?";
        PreparedStatement pstCheck = conn.prepareStatement(check);
        pstCheck.setInt(1, etape.getJour());
        pstCheck.setString(2, etape.getLieu());
        pstCheck.setInt(3, etape.getId_itineraire());
        ResultSet rs = pstCheck.executeQuery();

        if (rs.next() && rs.getInt(1) == 0) {

            String req = "INSERT INTO etape (jour, heure, lieu, description_etape, id_activite, id_itineraire) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstInsert = conn.prepareStatement(req);
            pstInsert.setInt(1, etape.getJour());
            pstInsert.setDate(2, etape.getHeure());
            pstInsert.setString(3, etape.getLieu());
            pstInsert.setString(4, etape.getDescription_etape());
            pstInsert.setInt(5, etape.getId_activite());
            pstInsert.setInt(6, etape.getId_itineraire());
            pstInsert.executeUpdate();

            System.out.println("Etape ajoutée");
        } else {
            System.out.println("Etape déjà existante, ajout ignoré");
        }
    }

    @Override
    public void modifier(etape etape) throws SQLException {
        String req = "update etape set jour=?, heure=?, lieu=?, description_etape=?, id_activite=?, id_itineraire=? where id_etape=?";
        PreparedStatement pst = conn.prepareStatement(req);

        pst.setInt(1, etape.getJour());
        pst.setDate(2, etape.getHeure());
        pst.setString(3, etape.getLieu());
        pst.setString(4, etape.getDescription_etape());
        pst.setInt(5, etape.getId_activite());
        pst.setInt(6, etape.getId_itineraire());
        pst.setInt(7, etape.getId_etape());
        pst.executeUpdate();
        System.out.println("Etape modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "delete from etape where id_etape=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Etape supprimée");
    }

    @Override
    public List<etape> afficher() throws SQLException {

        String req = "select * from etape";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<etape> listeEtape = new ArrayList<>();

        while (rs.next()) {
            etape e = new etape();
            e.setId_etape(rs.getInt("id_etape"));
            e.setJour(rs.getInt("jour"));
            e.setHeure(rs.getDate("heure"));
            e.setLieu(rs.getString("lieu"));
            e.setDescription_etape(rs.getString("description_etape"));
            e.setId_activite(rs.getInt("id_activite"));
            e.setId_itineraire(rs.getInt("id_itineraire"));

            listeEtape.add(e);
        }
        return listeEtape;
    }

    //trier
    public List<etape> trierParJour() throws SQLException {

        String req = "SELECT * FROM etape ORDER BY jour ASC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<etape> listeEtape = new ArrayList<>();

        while (rs.next()) {
            etape e = new etape();
            e.setId_etape(rs.getInt("id_etape"));
            e.setJour(rs.getInt("jour"));
            e.setHeure(rs.getDate("heure"));
            e.setLieu(rs.getString("lieu"));
            e.setDescription_etape(rs.getString("description_etape"));
            e.setId_activite((Integer) rs.getObject("id_activite"));
            e.setId_itineraire((Integer) rs.getObject("id_itineraire"));

            listeEtape.add(e);
        }
        return listeEtape;
    }

}