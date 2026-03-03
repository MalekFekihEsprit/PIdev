package Services;

import Entities.Destination;
import Entities.Hebergement;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HebergementCRUD implements InterfaceCRUDDestination<Hebergement> {

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
                "longitude_hebergement, destination_hebergement, added_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, object.getNom_hebergement());
            pst.setString(2, object.getType_hebergement());
            pst.setDouble(3, object.getPrixNuit_hebergement());
            pst.setString(4, object.getAdresse_hebergement());
            pst.setDouble(5, object.getNote_hebergement());
            pst.setDouble(6, object.getLatitude_hebergement());
            pst.setDouble(7, object.getLongitude_hebergement());
            pst.setInt(8, object.getDestination().getId_destination());
            pst.setInt(9, object.getAdded_by()); // User ID

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
                "destination_hebergement = ?, " +
                "added_by = ? " +
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
            pst.setInt(9, object.getAdded_by());
            pst.setInt(10, object.getId_hebergement());

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
        // Join with user table to get the user's nom and prenom
        String req = "SELECT h.*, u.nom, u.prenom FROM hebergement h " +
                "LEFT JOIN user u ON h.added_by = u.id";

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
                h.setAdded_by(rs.getInt("added_by"));

                int destId = rs.getInt("destination_hebergement");
                Destination dest = destinationCRUD.getDestinationById(destId);
                h.setDestination(dest);

                // Concatenate nom and prenom into added_by_name for display
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                if (nom != null && prenom != null) {
                    h.setAdded_by_name(nom + " " + prenom);
                } else {
                    h.setAdded_by_name("Utilisateur inconnu");
                }

                hebergements.add(h);
            }
        }

        return hebergements;
    }

    public List<Hebergement> getHebergementsByDestination(int destinationId) throws SQLException {
        String req = "SELECT h.*, u.nom, u.prenom FROM hebergement h " +
                "LEFT JOIN user u ON h.added_by = u.id " +
                "WHERE h.destination_hebergement = ?";

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
                h.setAdded_by(rs.getInt("added_by"));

                Destination dest = destinationCRUD.getDestinationById(destinationId);
                h.setDestination(dest);

                // Concatenate nom and prenom into added_by_name for display
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                if (nom != null && prenom != null) {
                    h.setAdded_by_name(nom + " " + prenom);
                } else {
                    h.setAdded_by_name("Utilisateur inconnu");
                }

                hebergements.add(h);
            }
        }

        return hebergements;
    }

    public Hebergement getHebergementById(int id) throws SQLException {
        String req = "SELECT h.*, u.nom, u.prenom FROM hebergement h " +
                "LEFT JOIN user u ON h.added_by = u.id " +
                "WHERE h.id_hebergement = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Hebergement h = new Hebergement();
                h.setId_hebergement(rs.getInt("id_hebergement"));
                h.setNom_hebergement(rs.getString("nom_hebergement"));
                h.setType_hebergement(rs.getString("type_hebergement"));
                h.setPrixNuit_hebergement(rs.getDouble("prixNuit_hebergement"));
                h.setAdresse_hebergement(rs.getString("adresse_hebergement"));
                h.setNote_hebergement(rs.getDouble("note_hebergement"));
                h.setLatitude_hebergement(rs.getDouble("latitude_hebergement"));
                h.setLongitude_hebergement(rs.getDouble("longitude_hebergement"));
                h.setAdded_by(rs.getInt("added_by"));

                int destId = rs.getInt("destination_hebergement");
                Destination dest = destinationCRUD.getDestinationById(destId);
                h.setDestination(dest);

                // Concatenate nom and prenom into added_by_name for display
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                if (nom != null && prenom != null) {
                    h.setAdded_by_name(nom + " " + prenom);
                } else {
                    h.setAdded_by_name("Utilisateur inconnu");
                }

                return h;
            }
        }

        return null;
    }

    public Connection getConnection() {
        return conn;
    }
}