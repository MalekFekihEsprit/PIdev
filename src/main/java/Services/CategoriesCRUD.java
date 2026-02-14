package Services;

import Entites.Categories;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriesCRUD implements IntrefaceCRUD<Categories> {

    Connection conn;

    public CategoriesCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Categories categorie) throws SQLException {
        String req = "INSERT INTO Categories (nom, description, type, saison, niveauintensite, publiccible) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, categorie.getNom());
        pst.setString(2, categorie.getDescription());
        pst.setString(3, categorie.getType());
        pst.setString(4, categorie.getSaison());
        pst.setString(5, categorie.getNiveauintensite());
        pst.setString(6, categorie.getPubliccible());

        pst.executeUpdate();
        System.out.println("Catégorie ajoutée !");
    }

    @Override
    public void modifier(Categories categorie) throws SQLException {
        String req = "UPDATE Categories SET nom=?, description=?, type=?, saison=?, " +
                "niveauintensite=?, publiccible=? WHERE id=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, categorie.getNom());
        pst.setString(2, categorie.getDescription());
        pst.setString(3, categorie.getType());
        pst.setString(4, categorie.getSaison());
        pst.setString(5, categorie.getNiveauintensite());
        pst.setString(6, categorie.getPubliccible());
        pst.setInt(7, categorie.getId());

        pst.executeUpdate();
        System.out.println("Catégorie modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM Categories WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Catégorie supprimée");
    }

    @Override
    public List<Categories> afficher() throws SQLException {
        String req = "SELECT * FROM Categories";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Categories> listeCategories = new ArrayList<>();

        while (rs.next()) {
            Categories c = new Categories();
            c.setId(rs.getInt("id"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            c.setType(rs.getString("type"));
            c.setSaison(rs.getString("saison"));
            c.setNiveauintensite(rs.getString("niveauintensite"));
            c.setPubliccible(rs.getString("publiccible"));

            listeCategories.add(c);
        }
        return listeCategories;
    }
}