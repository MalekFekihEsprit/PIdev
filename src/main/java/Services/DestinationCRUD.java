package Services;

import Entities.Destination;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinationCRUD implements InterfaceCRUDDestination<Destination> {
    Connection conn;

    public DestinationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Destination object) throws SQLException {
        String req = "INSERT INTO `destination`(" +
                "`nom_destination`, `pays_destination`, `region_destination`, " +
                "`description_destination`, `climat_destination`, `saison_destination`, " +
                "`latitude_destination`, `longitude_destination`, `score_destination`, " +
                "`currency_destination`, `flag_destination`, `languages_destination`, " +
                "`video_url`, `added_by`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, object.getNom_destination());
            pst.setString(2, object.getPays_destination());
            pst.setString(3, object.getRegion_destination());
            pst.setString(4, object.getDescription_destination());
            pst.setString(5, object.getClimat_destination());
            pst.setString(6, object.getSaison_destination());
            pst.setDouble(7, object.getLatitude_destination());
            pst.setDouble(8, object.getLongitude_destination());
            pst.setDouble(9, object.getScore_destination());
            pst.setString(10, object.getCurrency_destination());
            pst.setString(11, object.getFlag_destination());
            pst.setString(12, object.getLanguages_destination());
            pst.setString(13, object.getVideo_url());
            pst.setInt(14, object.getAdded_by()); // This is the user ID

            pst.executeUpdate();
            System.out.println("Destination ajoutée avec succès!");
        }
    }

    @Override
    public void modifier(Destination object) throws SQLException {
        String req = "UPDATE destination SET " +
                "nom_destination = ?, pays_destination = ?, region_destination = ?, " +
                "description_destination = ?, climat_destination = ?, saison_destination = ?, " +
                "latitude_destination = ?, longitude_destination = ?, score_destination = ?, " +
                "currency_destination = ?, flag_destination = ?, languages_destination = ?, " +
                "video_url = ?, added_by = ? " +
                "WHERE id_destination = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, object.getNom_destination());
            pst.setString(2, object.getPays_destination());
            pst.setString(3, object.getRegion_destination());
            pst.setString(4, object.getDescription_destination());
            pst.setString(5, object.getClimat_destination());
            pst.setString(6, object.getSaison_destination());
            pst.setDouble(7, object.getLatitude_destination());
            pst.setDouble(8, object.getLongitude_destination());
            pst.setDouble(9, object.getScore_destination());
            pst.setString(10, object.getCurrency_destination());
            pst.setString(11, object.getFlag_destination());
            pst.setString(12, object.getLanguages_destination());
            pst.setString(13, object.getVideo_url());
            pst.setInt(14, object.getAdded_by());
            pst.setInt(15, object.getId_destination());

            pst.executeUpdate();
            System.out.println("Destination modifiée avec succès!!");
        }
    }

    @Override
    public void supprimer(Destination object) throws SQLException {
        String req = "DELETE FROM destination WHERE id_destination = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, object.getId_destination());
            pst.executeUpdate();
            System.out.println("Destination supprimée avec succès!!");
        }
    }

    @Override
    public List<Destination> afficher() throws SQLException {
        // Join with user table to get the user's nom and prenom
        String req = "SELECT d.*, u.nom, u.prenom FROM destination d " +
                "LEFT JOIN user u ON d.added_by = u.id";

        List<Destination> destinations = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Destination destination = new Destination();
                destination.setId_destination(rs.getInt("id_destination"));
                destination.setNom_destination(rs.getString("nom_destination"));
                destination.setPays_destination(rs.getString("pays_destination"));
                destination.setRegion_destination(rs.getString("region_destination"));
                destination.setDescription_destination(rs.getString("description_destination"));
                destination.setClimat_destination(rs.getString("climat_destination"));
                destination.setSaison_destination(rs.getString("saison_destination"));
                destination.setLatitude_destination(rs.getDouble("latitude_destination"));
                destination.setLongitude_destination(rs.getDouble("longitude_destination"));
                destination.setScore_destination(rs.getDouble("score_destination"));
                destination.setCurrency_destination(rs.getString("currency_destination"));
                destination.setFlag_destination(rs.getString("flag_destination"));
                destination.setLanguages_destination(rs.getString("languages_destination"));
                destination.setVideo_url(rs.getString("video_url"));
                destination.setAdded_by(rs.getInt("added_by"));

                // Concatenate nom and prenom into added_by_name for display
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                if (nom != null && prenom != null) {
                    destination.setAdded_by_name(nom + " " + prenom);
                } else {
                    destination.setAdded_by_name("Utilisateur inconnu");
                }

                destinations.add(destination);
            }
        }

        return destinations;
    }

    // Method to get destination by ID with user info
    public Destination getDestinationById(int id) throws SQLException {
        String req = "SELECT d.*, u.nom, u.prenom FROM destination d " +
                "LEFT JOIN user u ON d.added_by = u.id " +
                "WHERE d.id_destination = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Destination destination = new Destination();
                destination.setId_destination(rs.getInt("id_destination"));
                destination.setNom_destination(rs.getString("nom_destination"));
                destination.setPays_destination(rs.getString("pays_destination"));
                destination.setRegion_destination(rs.getString("region_destination"));
                destination.setDescription_destination(rs.getString("description_destination"));
                destination.setClimat_destination(rs.getString("climat_destination"));
                destination.setSaison_destination(rs.getString("saison_destination"));
                destination.setLatitude_destination(rs.getDouble("latitude_destination"));
                destination.setLongitude_destination(rs.getDouble("longitude_destination"));
                destination.setScore_destination(rs.getDouble("score_destination"));
                destination.setCurrency_destination(rs.getString("currency_destination"));
                destination.setFlag_destination(rs.getString("flag_destination"));
                destination.setLanguages_destination(rs.getString("languages_destination"));
                destination.setVideo_url(rs.getString("video_url"));
                destination.setAdded_by(rs.getInt("added_by"));

                // Concatenate nom and prenom into added_by_name for display
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                if (nom != null && prenom != null) {
                    destination.setAdded_by_name(nom + " " + prenom);
                } else {
                    destination.setAdded_by_name("Utilisateur inconnu");
                }

                return destination;
            }
        }

        return null;
    }

    public Connection getConnection() {
        return conn;
    }
}