package services;

import tools.MyBD;
import entities.Destination;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinationCRUD implements InterfaceCRUD<Destination> {
    Connection conn;

    public DestinationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Destination object) throws SQLException {
        String req = "INSERT INTO `destination`(`nom_destination`, `pays_destination`, " +
                "`description_destination`, `climat_destination`, `saison_destination`, " +
                "`latitude_destination`, `longitude_destination`, `score_destination`, " +
                "`currency_destination`, `flag_destination`, `languages_destination`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, object.getNom_destination());
            pst.setString(2, object.getPays_destination());
            pst.setString(3, object.getDescription_destination());
            pst.setString(4, object.getClimat_destination());
            pst.setString(5, object.getSaison_destination());
            pst.setDouble(6, object.getLatitude_destination());
            pst.setDouble(7, object.getLongitude_destination());
            pst.setDouble(8, object.getScore_destination());
            pst.setString(9, object.getCurrency_destination());
            pst.setString(10, object.getFlag_destination());
            pst.setString(11, object.getLanguages_destination());

            pst.executeUpdate();
            System.out.println("Destination ajoutée avec succès!");
        }
    }

    @Override
    public void modifier(Destination object) throws SQLException {
        String req = "UPDATE destination SET nom_destination = ?, pays_destination = ?, " +
                "description_destination = ?, climat_destination = ?, saison_destination = ?, " +
                "latitude_destination = ?, longitude_destination = ?, score_destination = ?, " +
                "currency_destination = ?, flag_destination = ?, languages_destination = ? " +
                "WHERE id_destination = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, object.getNom_destination());
            pst.setString(2, object.getPays_destination());
            pst.setString(3, object.getDescription_destination());
            pst.setString(4, object.getClimat_destination());
            pst.setString(5, object.getSaison_destination());
            pst.setDouble(6, object.getLatitude_destination());
            pst.setDouble(7, object.getLongitude_destination());
            pst.setDouble(8, object.getScore_destination());
            pst.setString(9, object.getCurrency_destination());
            pst.setString(10, object.getFlag_destination());
            pst.setString(11, object.getLanguages_destination());
            pst.setInt(12, object.getId_destination());

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
        String req = "SELECT * FROM destination";
        List<Destination> destinations = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Destination destination = new Destination();
                destination.setId_destination(rs.getInt("id_destination"));
                destination.setNom_destination(rs.getString("nom_destination"));
                destination.setPays_destination(rs.getString("pays_destination"));
                destination.setDescription_destination(rs.getString("description_destination"));
                destination.setClimat_destination(rs.getString("climat_destination"));
                destination.setSaison_destination(rs.getString("saison_destination"));
                destination.setLatitude_destination(rs.getDouble("latitude_destination"));
                destination.setLongitude_destination(rs.getDouble("longitude_destination"));
                destination.setScore_destination(rs.getDouble("score_destination"));

                // New fields
                destination.setCurrency_destination(rs.getString("currency_destination"));
                destination.setFlag_destination(rs.getString("flag_destination"));
                destination.setLanguages_destination(rs.getString("languages_destination"));

                destinations.add(destination);
            }
        }

        return destinations;
    }

    // Optional: Method to get connection for other operations
    public Connection getConnection() {
        return conn;
    }
}