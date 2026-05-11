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

    // Toutes les colonnes nommées explicitement pour éviter les problèmes de casse avec h.*
    private static final String SELECT_BASE =
            "SELECT h.id_hebergement, " +
                    "       h.nom_hebergement, " +
                    "       h.type_hebergement, " +
                    "       h.prix_nuit_hebergement, " +
                    "       h.adresse_hebergement, " +
                    "       h.note_hebergement, " +
                    "       h.latitude_hebergement, " +
                    "       h.longitude_hebergement, " +
                    "       h.destination_hebergement, " +
                    "       h.added_by, " +
                    "       h.image_name, " +
                    "       u.nom, u.prenom " +
                    "FROM hebergement h " +
                    "LEFT JOIN user u ON h.added_by = u.id";

    public HebergementCRUD() {
        conn = MyBD.getInstance().getConn();
        destinationCRUD = new DestinationCRUD();
    }

    @Override
    public void ajouter(Hebergement object) throws SQLException {
        String req = "INSERT INTO hebergement (" +
                "nom_hebergement, type_hebergement, prix_nuit_hebergement, " +
                "adresse_hebergement, note_hebergement, latitude_hebergement, " +
                "longitude_hebergement, destination_hebergement, added_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, object.getNom_hebergement());
            pst.setString(2, object.getType_hebergement());

            if (object.getPrix_nuit_hebergement() != null)
                pst.setDouble(3, object.getPrix_nuit_hebergement());
            else
                pst.setNull(3, Types.DOUBLE);

            pst.setString(4, object.getAdresse_hebergement());

            if (object.getNote_hebergement() != null)
                pst.setDouble(5, object.getNote_hebergement());
            else
                pst.setNull(5, Types.DOUBLE);

            if (object.getLatitude_hebergement() != null)
                pst.setDouble(6, object.getLatitude_hebergement());
            else
                pst.setNull(6, Types.DOUBLE);

            if (object.getLongitude_hebergement() != null)
                pst.setDouble(7, object.getLongitude_hebergement());
            else
                pst.setNull(7, Types.DOUBLE);

            pst.setInt(8, object.getDestination().getId_destination());

            if (object.getAdded_by() != null)
                pst.setInt(9, object.getAdded_by());
            else
                pst.setNull(9, Types.INTEGER);

            pst.executeUpdate();
            System.out.println("Hébergement ajouté !");
        }
    }

    @Override
    public void modifier(Hebergement object) throws SQLException {
        String req = "UPDATE hebergement SET " +
                "nom_hebergement = ?, " +
                "type_hebergement = ?, " +
                "prix_nuit_hebergement = ?, " +
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

            if (object.getPrix_nuit_hebergement() != null)
                pst.setDouble(3, object.getPrix_nuit_hebergement());
            else
                pst.setNull(3, Types.DOUBLE);

            pst.setString(4, object.getAdresse_hebergement());

            if (object.getNote_hebergement() != null)
                pst.setDouble(5, object.getNote_hebergement());
            else
                pst.setNull(5, Types.DOUBLE);

            if (object.getLatitude_hebergement() != null)
                pst.setDouble(6, object.getLatitude_hebergement());
            else
                pst.setNull(6, Types.DOUBLE);

            if (object.getLongitude_hebergement() != null)
                pst.setDouble(7, object.getLongitude_hebergement());
            else
                pst.setNull(7, Types.DOUBLE);

            pst.setInt(8, object.getDestination().getId_destination());

            if (object.getAdded_by() != null)
                pst.setInt(9, object.getAdded_by());
            else
                pst.setNull(9, Types.INTEGER);

            pst.setInt(10, object.getId_hebergement());

            pst.executeUpdate();
            System.out.println("Hébergement modifié !");
        }
    }

    @Override
    public void supprimer(Hebergement object) throws SQLException {
        String req = "DELETE FROM hebergement WHERE id_hebergement = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, object.getId_hebergement());
            pst.executeUpdate();
            System.out.println("Hébergement supprimé !");
        }
    }

    @Override
    public List<Hebergement> afficher() throws SQLException {
        List<Hebergement> hebergements = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE)) {
            while (rs.next()) {
                hebergements.add(mapResultSet(rs));
            }
        }
        return hebergements;
    }

    public List<Hebergement> getHebergementsByDestination(int destinationId) throws SQLException {
        String req = SELECT_BASE + " WHERE h.destination_hebergement = ?";
        List<Hebergement> hebergements = new ArrayList<>();
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, destinationId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    hebergements.add(mapResultSet(rs));
                }
            }
        }
        return hebergements;
    }

    public Hebergement getHebergementById(int id) throws SQLException {
        String req = SELECT_BASE + " WHERE h.id_hebergement = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        }
        return null;
    }

    /**
     * Mappe un ResultSet vers un objet Hebergement.
     * Gère correctement les valeurs NULL pour added_by et les champs Double.
     */
    private Hebergement mapResultSet(ResultSet rs) throws SQLException {
        Hebergement h = new Hebergement();

        h.setId_hebergement(rs.getInt("id_hebergement"));
        h.setNom_hebergement(rs.getString("nom_hebergement"));
        h.setType_hebergement(rs.getString("type_hebergement"));
        h.setAdresse_hebergement(rs.getString("adresse_hebergement"));

        // Double nullable : wasNull() doit être appelé juste après getDouble()
        double prix = rs.getDouble("prix_nuit_hebergement");
        h.setPrix_nuit_hebergement(rs.wasNull() ? null : prix);

        double note = rs.getDouble("note_hebergement");
        h.setNote_hebergement(rs.wasNull() ? null : note);

        double lat = rs.getDouble("latitude_hebergement");
        h.setLatitude_hebergement(rs.wasNull() ? null : lat);

        double lon = rs.getDouble("longitude_hebergement");
        h.setLongitude_hebergement(rs.wasNull() ? null : lon);

        // Integer nullable (ON DELETE SET NULL)
        int addedBy = rs.getInt("added_by");
        h.setAdded_by(rs.wasNull() ? null : addedBy);

        // Destination
        int destId = rs.getInt("destination_hebergement");
        Destination dest = destinationCRUD.getDestinationById(destId);
        h.setDestination(dest);

        // Nom affiché de l'auteur
        String nom    = rs.getString("nom");
        String prenom = rs.getString("prenom");
        if (nom != null && prenom != null)
            h.setAdded_by_name(prenom + " " + nom);
        else
            h.setAdded_by_name("Utilisateur inconnu");

        h.setImage_name(rs.getString("image_name"));

        return h;
    }

    public Connection getConnection() {
        return conn;
    }
}