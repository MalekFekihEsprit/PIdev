package services;

import tools.MyBD;
import entities.Hebergement;
import entities.Destination;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HebergementCRUD implements InterfaceCRUD<Hebergement> {

    Connection conn;
    DestinationCRUD destinationCRUD;

    public HebergementCRUD() {
        conn = MyBD.getInstance().getConn();
        destinationCRUD = new DestinationCRUD();
    }

    @Override
    public void ajouter(Hebergement object) throws SQLException {

        String req = "INSERT INTO hebergement (" +
                "nom_hebergement, type_hebergement, prixNuit_hebergement, " +
                "adresse_hebergement, note_hebergement, latitude_hebergement, " +
                "longitude_hebergement, score_hebergement, destination_hebergement) " +
                "VALUES ('" + object.getNom_hebergement() + "', '"
                + object.getType_hebergement() + "', "
                + object.getPrixNuit_hebergement() + ", '"
                + object.getAdresse_hebergement() + "', "
                + object.getNote_hebergement() + ", "
                + object.getLatitude_hebergement() + ", "
                + object.getLongitude_hebergement() + ", "
                + object.getScore_hebergement() + ", "
                + object.getDestination().getId_destination() + ")";

        Statement st = conn.createStatement();
        st.executeUpdate(req);
        System.out.println("hébergement ajouté !!");
    }

    @Override
    public void modifier(Hebergement object) throws SQLException {

        String req = "UPDATE hebergement SET " +
                "nom_hebergement = ?, " +
                "type_hebergement = ?, " +
                "prixNuit_hebergement = ?, " +
                "adresse_hebergement = ?, " +
                "note_hebergement = ?, " +
                "latitude_hebergement = ?, " +
                "longitude_hebergement = ?, " +
                "score_hebergement = ?, " +
                "destination_hebergement = ? " +
                "WHERE id_hebergement = ?";

        PreparedStatement st = conn.prepareStatement(req);
        st.setString(1, object.getNom_hebergement());
        st.setString(2, object.getType_hebergement());
        st.setDouble(3, object.getPrixNuit_hebergement());
        st.setString(4, object.getAdresse_hebergement());
        st.setDouble(5, object.getNote_hebergement());
        st.setDouble(6, object.getLatitude_hebergement());
        st.setDouble(7, object.getLongitude_hebergement());
        st.setDouble(8, object.getScore_hebergement());
        st.setInt(9, object.getDestination().getId_destination());
        st.setInt(10, object.getId_hebergement());

        st.executeUpdate();
        System.out.println("hébergement modifié !!");
    }

    @Override
    public void supprimer(Hebergement object) throws SQLException {

        String req = "DELETE FROM hebergement WHERE id_hebergement = ?";
        PreparedStatement st = conn.prepareStatement(req);
        st.setInt(1, object.getId_hebergement());
        st.executeUpdate();

        System.out.println("hébergement supprimé !!");
    }

    @Override
    public List<Hebergement> afficher() throws SQLException {

        String req = "SELECT * FROM hebergement";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<Hebergement> hebergements = new ArrayList<>();

        while (rs.next()) {

            Hebergement h = new Hebergement();

            h.setId_hebergement(rs.getInt("id_hebergement"));
            h.setNom_hebergement(rs.getString("nom_hebergement"));
            h.setType_hebergement(rs.getString("type_hebergement"));
            h.setPrixNuit_hebergement(rs.getDouble("prixNuit_hebergement"));
            h.setAdresse_hebergement(rs.getString("adresse_hebergement"));
            h.setNote_hebergement(rs.getDouble("note_hebergement"));
            h.setLatitude_hebergement(rs.getDouble("latitude_hebergement"));
            h.setLongitude_hebergement(rs.getDouble("longitude_hebergement"));
            h.setScore_hebergement(rs.getDouble("score_hebergement"));

            int destId = rs.getInt("destination_hebergement");
            Destination dest = null;
            List<Destination> allDest = destinationCRUD.afficher();
            for (Destination d : allDest) {
                if (d.getId_destination() == destId) {
                    dest = d;
                    break;
                }
            }
            h.setDestination(dest);

            hebergements.add(h);
        }

        return hebergements;
    }
}
