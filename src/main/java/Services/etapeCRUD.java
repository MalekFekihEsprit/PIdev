package Services;

import Entites.etape;  // Corrigé: Majuscule
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
        // Vérification si l'étape existe déjà (basée sur lieu, heure et id_itineraire)
        String check = "SELECT COUNT(*) FROM etape WHERE heure=? AND lieu=? AND id_itineraire=?";
        PreparedStatement pstCheck = conn.prepareStatement(check);
        pstCheck.setTime(1, etape.getHeure());  // Corrigé: Time au lieu de Date
        pstCheck.setString(2, etape.getLieu());
        pstCheck.setInt(3, etape.getId_itineraire());
        ResultSet rs = pstCheck.executeQuery();

        if (rs.next() && rs.getInt(1) == 0) {
            // Suppression du champ 'jour' qui n'existe pas
            String req = "INSERT INTO etape (heure, lieu, description_etape, id_activite, id_itineraire) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstInsert = conn.prepareStatement(req);
            pstInsert.setTime(1, etape.getHeure());  // Corrigé: Time
            pstInsert.setString(2, etape.getLieu());
            pstInsert.setString(3, etape.getDescription_etape());
            pstInsert.setInt(4, etape.getId_activite());
            pstInsert.setInt(5, etape.getId_itineraire());
            pstInsert.executeUpdate();

            System.out.println("Etape ajoutée");
        } else {
            System.out.println("Etape déjà existante, ajout ignoré");
        }
    }

    @Override
    public void modifier(etape etape) throws SQLException {
        // Suppression du champ 'jour'
        String req = "UPDATE etape SET heure=?, lieu=?, description_etape=?, id_activite=?, id_itineraire=? WHERE id_etape=?";
        PreparedStatement pst = conn.prepareStatement(req);

        pst.setTime(1, etape.getHeure());  // Corrigé: Time
        pst.setString(2, etape.getLieu());
        pst.setString(3, etape.getDescription_etape());
        pst.setInt(4, etape.getId_activite());
        pst.setInt(5, etape.getId_itineraire());
        pst.setInt(6, etape.getId_etape());
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
        String req = "SELECT * FROM etape";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<etape> listeEtape = new ArrayList<>();

        while (rs.next()) {
            etape e = new etape();
            e.setId_etape(rs.getInt("id_etape"));
            e.setHeure(rs.getTime("heure"));  // Corrigé: getTime()
            e.setLieu(rs.getString("lieu"));
            e.setDescription_etape(rs.getString("description_etape"));
            e.setId_activite(rs.getInt("id_activite"));
            e.setId_itineraire(rs.getInt("id_itineraire"));

            listeEtape.add(e);
        }
        return listeEtape;
    }

    // Trier par heure
    public List<etape> trierParHeure() throws SQLException {
        String req = "SELECT * FROM etape ORDER BY heure ASC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<etape> listeEtape = new ArrayList<>();

        while (rs.next()) {
            etape e = new etape();
            e.setId_etape(rs.getInt("id_etape"));
            e.setHeure(rs.getTime("heure"));
            e.setLieu(rs.getString("lieu"));
            e.setDescription_etape(rs.getString("description_etape"));
            e.setId_activite(rs.getInt("id_activite"));
            e.setId_itineraire(rs.getInt("id_itineraire"));

            listeEtape.add(e);
        }
        return listeEtape;
    }

    // Méthode pour rechercher les étapes d'un itinéraire
    public List<etape> getEtapesByItineraire(int id_itineraire) throws SQLException {
        String req = "SELECT * FROM etape WHERE id_itineraire = ? ORDER BY heure ASC";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id_itineraire);
        ResultSet rs = pst.executeQuery();

        List<etape> listeEtape = new ArrayList<>();

        while (rs.next()) {
            etape e = new etape();
            e.setId_etape(rs.getInt("id_etape"));
            e.setHeure(rs.getTime("heure"));
            e.setLieu(rs.getString("lieu"));
            e.setDescription_etape(rs.getString("description_etape"));
            e.setId_activite(rs.getInt("id_activite"));
            e.setId_itineraire(rs.getInt("id_itineraire"));

            listeEtape.add(e);
        }
        return listeEtape;
    }
}