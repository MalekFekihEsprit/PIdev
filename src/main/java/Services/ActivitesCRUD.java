package Services;

import Entites.Activites;
import Entites.Categories;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivitesCRUD implements IntrefaceCRUD<Activites> {

    Connection conn;

    public ActivitesCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Activites activite) throws SQLException {
        String req = "INSERT INTO Activites (nom, description, budget, niveaudifficulte, lieu, agemin, statut, duree, categorie_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, activite.getNom());
        pst.setString(2, activite.getDescription());
        pst.setInt(3, activite.getBudget());
        pst.setString(4, activite.getNiveaudifficulte());
        pst.setString(5, activite.getLieu());
        pst.setInt(6, activite.getAgemin());
        pst.setString(7, activite.getStatut());
        pst.setInt(8, activite.getDuree());
        pst.setInt(9, activite.getCategorieId());

        pst.executeUpdate();

        // Récupérer l'ID généré
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            activite.setId(rs.getInt(1));
        }

        System.out.println("Activité ajoutée !");
    }

    @Override
    public void modifier(Activites activite) throws SQLException {
        String req = "UPDATE Activites SET nom=?, description=?, budget=?, niveaudifficulte=?, " +
                "lieu=?, agemin=?, statut=?, duree=?, categorie_id=? WHERE id=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, activite.getNom());
        pst.setString(2, activite.getDescription());
        pst.setInt(3, activite.getBudget());
        pst.setString(4, activite.getNiveaudifficulte());
        pst.setString(5, activite.getLieu());
        pst.setInt(6, activite.getAgemin());
        pst.setString(7, activite.getStatut());
        pst.setInt(8, activite.getDuree());
        pst.setInt(9, activite.getCategorieId());
        pst.setInt(10, activite.getId());

        pst.executeUpdate();
        System.out.println("Activité modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM Activites WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Activité supprimée");
    }

    @Override
    public List<Activites> afficher() throws SQLException {
        // Requête avec jointure pour récupérer toutes les informations de la catégorie
        String req = "SELECT a.*, c.id as cat_id, c.nom as cat_nom, c.type as cat_type, " +
                "c.saison as cat_saison, c.niveauintensite as cat_intensite, c.publiccible as cat_public " +
                "FROM Activites a " +
                "LEFT JOIN Categories c ON a.categorie_id = c.id";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Activites> listeActivites = new ArrayList<>();

        while(rs.next()){
            Activites p = new Activites();
            p.setId(rs.getInt("id"));
            p.setNom(rs.getString("nom"));
            p.setDescription(rs.getString("description"));
            p.setBudget(rs.getInt("budget"));
            p.setNiveaudifficulte(rs.getString("niveaudifficulte"));
            p.setLieu(rs.getString("lieu"));
            p.setAgemin(rs.getInt("agemin"));
            p.setStatut(rs.getString("statut"));
            p.setDuree(rs.getInt("duree"));
            p.setCategorieId(rs.getInt("categorie_id"));

            // Créer et associer l'objet Catégorie si elle existe
            if (rs.getObject("cat_id") != null) {
                Categories cat = new Categories();
                cat.setId(rs.getInt("cat_id"));
                cat.setNom(rs.getString("cat_nom"));
                cat.setType(rs.getString("cat_type"));
                cat.setSaison(rs.getString("cat_saison"));
                cat.setNiveauintensite(rs.getString("cat_intensite"));
                cat.setPubliccible(rs.getString("cat_public"));
                p.setCategorie(cat); // ← LIGNE IMPORTANTE !
            }

            listeActivites.add(p);
        }
        return listeActivites;
    }

    // Nouvelle méthode : Récupérer les activités par catégorie
    public List<Activites> getActivitesByCategorie(int categorieId) throws SQLException {
        String req = "SELECT * FROM Activites WHERE categorie_id = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, categorieId);
        ResultSet rs = pst.executeQuery();

        List<Activites> listeActivites = new ArrayList<>();

        while(rs.next()){
            Activites p = new Activites();
            p.setId(rs.getInt("id"));
            p.setNom(rs.getString("nom"));
            p.setDescription(rs.getString("description"));
            p.setBudget(rs.getInt("budget"));
            p.setNiveaudifficulte(rs.getString("niveaudifficulte"));
            p.setLieu(rs.getString("lieu"));
            p.setAgemin(rs.getInt("agemin"));
            p.setStatut(rs.getString("statut"));
            p.setDuree(rs.getInt("duree"));
            p.setCategorieId(rs.getInt("categorie_id"));

            listeActivites.add(p);
        }
        return listeActivites;
    }
}