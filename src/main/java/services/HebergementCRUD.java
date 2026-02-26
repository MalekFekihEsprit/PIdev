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
                "longitude_hebergement, destination_hebergement) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, object.getNom_hebergement());
            pst.setString(2, object.getType_hebergement());
            pst.setDouble(3, object.getPrixNuit_hebergement());
            pst.setString(4, object.getAdresse_hebergement());
            pst.setDouble(5, object.getNote_hebergement());
            pst.setDouble(6, object.getLatitude_hebergement());
            pst.setDouble(7, object.getLongitude_hebergement());
            pst.setInt(8, object.getDestination().getId_destination());

            pst.executeUpdate();
            System.out.println("Hébergement ajouté !!");
        }
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
                "destination_hebergement = ? " +
                "WHERE id_hebergement = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, object.getNom_hebergement());
            pst.setString(2, object.getType_hebergement());
            pst.setDouble(3, object.getPrixNuit_hebergement());
            pst.setString(4, object.getAdresse_hebergement());
            pst.setDouble(5, object.getNote_hebergement());
            pst.setDouble(6, object.getLatitude_hebergement());
            pst.setDouble(7, object.getLongitude_hebergement());
            pst.setInt(8, object.getDestination().getId_destination());
            pst.setInt(9, object.getId_hebergement());

            pst.executeUpdate();
            System.out.println("Hébergement modifié !!");
        }
    }

    @Override
    public void supprimer(Hebergement object) throws SQLException {
        String req = "DELETE FROM hebergement WHERE id_hebergement = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, object.getId_hebergement());
            pst.executeUpdate();
            System.out.println("Hébergement supprimé !!");
        }
    }

    @Override
    public List<Hebergement> afficher() throws SQLException {
        String req = "SELECT * FROM hebergement";
        List<Hebergement> hebergements = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {

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
        }

        return hebergements;
    }

    public List<Hebergement> getHebergementsByDestination(int destinationId) throws SQLException {
        String req = "SELECT * FROM hebergement WHERE destination_hebergement = ?";
        List<Hebergement> hebergements = new ArrayList<>();

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, destinationId);
            ResultSet rs = pst.executeQuery();

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

                Destination dest = destinationCRUD.afficher().stream()
                        .filter(d -> d.getId_destination() == destinationId)
                        .findFirst()
                        .orElse(null);
                h.setDestination(dest);

                hebergements.add(h);
            }
        }

        return hebergements;
    }

    public Connection getConnection() {
        return conn;
    }
}