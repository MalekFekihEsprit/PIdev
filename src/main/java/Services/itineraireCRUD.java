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
        pstCheck.setString(1, itin.getNom_itineraire());  // Corrigé
        pstCheck.setInt(2, itin.getId_voyage());         // Corrigé
        ResultSet rs = pstCheck.executeQuery();

        if (rs.next() && rs.getInt(1) == 0) {
            // Ajout du champ nombre_jour
            String req = "INSERT INTO itineraire (nom_itineraire, description_itineraire, id_voyage, nombre_jour) VALUES (?, ?, ?, ?)";
            PreparedStatement pstInsert = conn.prepareStatement(req);
            pstInsert.setString(1, itin.getNom_itineraire());        // Corrigé
            pstInsert.setString(2, itin.getDescription_itineraire()); // Corrigé
            pstInsert.setInt(3, itin.getId_voyage());               // Corrigé
            pstInsert.setInt(4, itin.getNombre_jour());            // Ajouté
            pstInsert.executeUpdate();

            System.out.println("Itinéraire ajouté !");
        } else {
            System.out.println("Itinéraire déjà existant, ajout ignoré !");
        }
    }

    @Override
    public void modifier(Itineraire itin) throws SQLException {
        // Ajout du champ nombre_jour
        String req = "UPDATE itineraire SET nom_itineraire=?, description_itineraire=?, id_voyage=?, nombre_jour=? WHERE id_itineraire=?";
        PreparedStatement pst = conn.prepareStatement(req);

        pst.setString(1, itin.getNom_itineraire());        // Corrigé
        pst.setString(2, itin.getDescription_itineraire()); // Corrigé
        pst.setInt(3, itin.getId_voyage());               // Corrigé
        pst.setInt(4, itin.getNombre_jour());            // Ajouté
        pst.setInt(5, itin.getId_itineraire());          // Corrigé

        pst.executeUpdate();
        System.out.println("Itinéraire modifié");
    }

    @Override
    public void supprimer(int id_itineraire) throws SQLException {
        // Attention: Vérifier les contraintes de clé étrangère
        String req = "DELETE FROM itineraire WHERE id_itineraire=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id_itineraire);
        pst.executeUpdate();
        System.out.println("Itinéraire supprimé");
    }

    @Override
    public List<Itineraire> afficher() throws SQLException {
        String req = "SELECT * FROM itineraire";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Itineraire> listeItineraire = new ArrayList<>();

        while (rs.next()) {
            Itineraire p = new Itineraire();
            p.setId_itineraire(rs.getInt("id_itineraire"));              // Corrigé
            p.setNom_itineraire(rs.getString("nom_itineraire"));        // Corrigé
            p.setDescription_itineraire(rs.getString("description_itineraire")); // Corrigé
            p.setId_voyage(rs.getInt("id_voyage"));                     // Corrigé
            p.setNombre_jour(rs.getInt("nombre_jour"));                // Ajouté

            listeItineraire.add(p);
        }
        return listeItineraire;
    }

    // Méthode supplémentaire: rechercher par voyage
    public List<Itineraire> getItinerairesByVoyage(int id_voyage) throws SQLException {
        String req = "SELECT * FROM itineraire WHERE id_voyage = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id_voyage);
        ResultSet rs = pst.executeQuery();

        List<Itineraire> listeItineraire = new ArrayList<>();

        while (rs.next()) {
            Itineraire p = new Itineraire();
            p.setId_itineraire(rs.getInt("id_itineraire"));
            p.setNom_itineraire(rs.getString("nom_itineraire"));
            p.setDescription_itineraire(rs.getString("description_itineraire"));
            p.setId_voyage(rs.getInt("id_voyage"));
            p.setNombre_jour(rs.getInt("nombre_jour"));

            listeItineraire.add(p);
        }
        return listeItineraire;
    }
}