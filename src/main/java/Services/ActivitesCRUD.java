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
        String req = "INSERT INTO Activites (nom, description, budget, niveaudifficulte, lieu, agemin, statut, duree, categorie_id, image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        pst.setString(10, activite.getImagePath());

        pst.executeUpdate();

        // Récupérer l'ID généré
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            activite.setId(rs.getInt(1));
        }

        System.out.println("✅ Activité ajoutée !");
    }

    @Override
    public void modifier(Activites activite) throws SQLException {
        String req = "UPDATE Activites SET nom=?, description=?, budget=?, niveaudifficulte=?, " +
                "lieu=?, agemin=?, statut=?, duree=?, categorie_id=?, image_path=? WHERE id=?";

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
        pst.setString(10, activite.getImagePath());
        pst.setInt(11, activite.getId());

        pst.executeUpdate();
        System.out.println("✅ Activité modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // D'abord récupérer le chemin de l'image pour pouvoir la supprimer du disque
        String selectReq = "SELECT image_path FROM Activites WHERE id=?";
        PreparedStatement selectPst = conn.prepareStatement(selectReq);
        selectPst.setInt(1, id);
        ResultSet rs = selectPst.executeQuery();
        if (rs.next()) {
            String imagePath = rs.getString("image_path");
            if (imagePath != null && !imagePath.isEmpty()) {
                // Supprimer le fichier image du disque
                try {
                    java.io.File file = new java.io.File(imagePath);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la suppression du fichier image: " + e.getMessage());
                }
            }
        }

        // Ensuite supprimer l'activité
        String req = "DELETE FROM Activites WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("✅ Activité supprimée");
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
            p.setImagePath(rs.getString("image_path"));

            // LOG POUR DÉBOGUER - À AJOUTER TEMPORAIREMENT
            System.out.println("🔍 Activité chargée: " + p.getNom());
            System.out.println("   Budget: " + p.getBudget());
            System.out.println("   Durée: " + p.getDuree());
            System.out.println("   Difficulté: " + p.getNiveaudifficulte());
            System.out.println("   Âge min: " + p.getAgemin());
            System.out.println("   Statut: " + p.getStatut());
            System.out.println("   Catégorie ID: " + p.getCategorieId());

            // Créer et associer l'objet Catégorie si elle existe
            if (rs.getObject("cat_id") != null) {
                Categories cat = new Categories();
                cat.setId(rs.getInt("cat_id"));
                cat.setNom(rs.getString("cat_nom"));
                cat.setType(rs.getString("cat_type"));
                cat.setSaison(rs.getString("cat_saison"));
                cat.setNiveauintensite(rs.getString("cat_intensite"));
                cat.setPubliccible(rs.getString("cat_public"));
                p.setCategorie(cat);
                System.out.println("   Catégorie: " + cat.getNom());
            } else {
                System.out.println("   ⚠️ Pas de catégorie associée");
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
            p.setImagePath(rs.getString("image_path"));

            listeActivites.add(p);
        }
        return listeActivites;
    }

    // NOUVELLE METHODE : Récupérer une activité par ID (pour modification)
    public Activites getOne(int id) throws SQLException {
        String req = "SELECT a.*, c.id as cat_id, c.nom as cat_nom, c.type as cat_type " +
                "FROM Activites a " +
                "LEFT JOIN Categories c ON a.categorie_id = c.id " +
                "WHERE a.id = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            Activites a = new Activites();
            a.setId(rs.getInt("id"));
            a.setNom(rs.getString("nom"));
            a.setDescription(rs.getString("description"));
            a.setBudget(rs.getInt("budget"));
            a.setNiveaudifficulte(rs.getString("niveaudifficulte"));
            a.setLieu(rs.getString("lieu"));
            a.setAgemin(rs.getInt("agemin"));
            a.setStatut(rs.getString("statut"));
            a.setDuree(rs.getInt("duree"));
            a.setCategorieId(rs.getInt("categorie_id"));
            a.setImagePath(rs.getString("image_path"));

            // Charger la catégorie si elle existe
            if (rs.getObject("cat_id") != null) {
                Categories cat = new Categories();
                cat.setId(rs.getInt("cat_id"));
                cat.setNom(rs.getString("cat_nom"));
                cat.setType(rs.getString("cat_type"));
                a.setCategorie(cat);
            }

            return a;
        }
        return null;
    }
}