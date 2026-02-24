package Services;

import Entites.Activites;
import Entites.Categories;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActivitesCRUD implements IntrefaceCRUD<Activites> {

    Connection conn;

    public ActivitesCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Activites activite) throws SQLException {
        String req = "INSERT INTO Activites (nom, description, budget, niveaudifficulte, lieu, agemin, statut, duree, categorie_id, image_path, date_prevue) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

        // Gestion de la date
        if (activite.getDatePrevue() != null) {
            pst.setDate(11, Date.valueOf(activite.getDatePrevue()));
        } else {
            pst.setNull(11, Types.DATE);
        }

        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            activite.setId(rs.getInt(1));
        }

        System.out.println("✅ Activité ajoutée !");
    }

    @Override
    public void modifier(Activites activite) throws SQLException {
        String req = "UPDATE Activites SET nom=?, description=?, budget=?, niveaudifficulte=?, " +
                "lieu=?, agemin=?, statut=?, duree=?, categorie_id=?, image_path=?, date_prevue=? WHERE id=?";

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

        // Gestion de la date
        if (activite.getDatePrevue() != null) {
            pst.setDate(11, Date.valueOf(activite.getDatePrevue()));
        } else {
            pst.setNull(11, Types.DATE);
        }

        pst.setInt(12, activite.getId());

        pst.executeUpdate();
        System.out.println("✅ Activité modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String selectReq = "SELECT image_path FROM Activites WHERE id=?";
        PreparedStatement selectPst = conn.prepareStatement(selectReq);
        selectPst.setInt(1, id);
        ResultSet rs = selectPst.executeQuery();
        if (rs.next()) {
            String imagePath = rs.getString("image_path");
            if (imagePath != null && !imagePath.isEmpty()) {
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

        String req = "DELETE FROM Activites WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("✅ Activité supprimée");
    }

    @Override
    public List<Activites> afficher() throws SQLException {
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

            // Récupération de la date
            Date dateSql = rs.getDate("date_prevue");
            if (dateSql != null) {
                p.setDatePrevue(dateSql.toLocalDate());
            }

            if (rs.getObject("cat_id") != null) {
                Categories cat = new Categories();
                cat.setId(rs.getInt("cat_id"));
                cat.setNom(rs.getString("cat_nom"));
                cat.setType(rs.getString("cat_type"));
                cat.setSaison(rs.getString("cat_saison"));
                cat.setNiveauintensite(rs.getString("cat_intensite"));
                cat.setPubliccible(rs.getString("cat_public"));
                p.setCategorie(cat);
            }

            listeActivites.add(p);
        }
        return listeActivites;
    }

    /**
     * Récupère les activités d'une catégorie spécifique par son NOM
     */
    public List<Activites> afficherParCategorie(String nomCategorie) throws SQLException {
        String req = "SELECT a.*, c.id as cat_id, c.nom as cat_nom, c.type as cat_type, " +
                "c.saison as cat_saison, c.niveauintensite as cat_intensite, c.publiccible as cat_public " +
                "FROM Activites a " +
                "LEFT JOIN Categories c ON a.categorie_id = c.id " +
                "WHERE c.nom = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, nomCategorie);
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

            // Récupération de la date
            Date dateSql = rs.getDate("date_prevue");
            if (dateSql != null) {
                p.setDatePrevue(dateSql.toLocalDate());
            }

            if (rs.getObject("cat_id") != null) {
                Categories cat = new Categories();
                cat.setId(rs.getInt("cat_id"));
                cat.setNom(rs.getString("cat_nom"));
                cat.setType(rs.getString("cat_type"));
                cat.setSaison(rs.getString("cat_saison"));
                cat.setNiveauintensite(rs.getString("cat_intensite"));
                cat.setPubliccible(rs.getString("cat_public"));
                p.setCategorie(cat);
            }

            listeActivites.add(p);
        }
        return listeActivites;
    }

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

            // Récupération de la date
            Date dateSql = rs.getDate("date_prevue");
            if (dateSql != null) {
                p.setDatePrevue(dateSql.toLocalDate());
            }

            listeActivites.add(p);
        }
        return listeActivites;
    }

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

            // Récupération de la date
            Date dateSql = rs.getDate("date_prevue");
            if (dateSql != null) {
                a.setDatePrevue(dateSql.toLocalDate());
            }

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