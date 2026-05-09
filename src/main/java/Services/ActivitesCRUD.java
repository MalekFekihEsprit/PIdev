package Services;

import Entities.Activites;
import Entities.Categories;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class  ActivitesCRUD implements IntrefaceCRUDActivites<Activites> {

    Connection conn;

    public ActivitesCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Activites activite) throws SQLException {
        String req = "INSERT INTO activites (nom, description, budget, niveaudifficulte, lieu, agemin, statut, duree, categorie_id, image_path, latitude, longitude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

        // latitude et longitude (nullable)
        if (activite.getLatitude() != null) {
            pst.setDouble(11, activite.getLatitude());
        } else {
            pst.setNull(11, Types.DOUBLE);
        }
        if (activite.getLongitude() != null) {
            pst.setDouble(12, activite.getLongitude());
        } else {
            pst.setNull(12, Types.DOUBLE);
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
        String req = "UPDATE activites SET nom=?, description=?, budget=?, niveaudifficulte=?, " +
                "lieu=?, agemin=?, statut=?, duree=?, categorie_id=?, image_path=?, " +
                "latitude=?, longitude=? WHERE id=?";

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

        // latitude et longitude (nullable)
        if (activite.getLatitude() != null) {
            pst.setDouble(11, activite.getLatitude());
        } else {
            pst.setNull(11, Types.DOUBLE);
        }
        if (activite.getLongitude() != null) {
            pst.setDouble(12, activite.getLongitude());
        } else {
            pst.setNull(12, Types.DOUBLE);
        }

        pst.setInt(13, activite.getId());

        pst.executeUpdate();
        System.out.println("✅ Activité modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String selectReq = "SELECT image_path FROM activites WHERE id=?";
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

        String req = "DELETE FROM activites WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("✅ Activité supprimée");
    }

    @Override
    public List<Activites> afficher() throws SQLException {
        String req = "SELECT a.*, c.id as cat_id, c.nom as cat_nom, c.type as cat_type, " +
                "c.saison as cat_saison, c.niveauintensite as cat_intensite, c.publiccible as cat_public " +
                "FROM activites a " +
                "LEFT JOIN categories c ON a.categorie_id = c.id";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Activites> listeActivites = new ArrayList<>();

        while (rs.next()) {
            listeActivites.add(mapRow(rs));
        }
        return listeActivites;
    }

    /**
     * Récupère les activités d'une catégorie spécifique par son NOM
     */
    public List<Activites> afficherParCategorie(String nomCategorie) throws SQLException {
        String req = "SELECT a.*, c.id as cat_id, c.nom as cat_nom, c.type as cat_type, " +
                "c.saison as cat_saison, c.niveauintensite as cat_intensite, c.publiccible as cat_public " +
                "FROM activites a " +
                "LEFT JOIN categories c ON a.categorie_id = c.id " +
                "WHERE c.nom = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, nomCategorie);
        ResultSet rs = pst.executeQuery();

        List<Activites> listeActivites = new ArrayList<>();
        while (rs.next()) {
            listeActivites.add(mapRow(rs));
        }
        return listeActivites;
    }

    public List<Activites> getActivitesByCategorie(int categorieId) throws SQLException {
        String req = "SELECT a.*, c.id as cat_id, c.nom as cat_nom, c.type as cat_type, " +
                "c.saison as cat_saison, c.niveauintensite as cat_intensite, c.publiccible as cat_public " +
                "FROM activites a " +
                "LEFT JOIN categories c ON a.categorie_id = c.id " +
                "WHERE a.categorie_id = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, categorieId);
        ResultSet rs = pst.executeQuery();

        List<Activites> listeActivites = new ArrayList<>();
        while (rs.next()) {
            listeActivites.add(mapRow(rs));
        }
        return listeActivites;
    }

    public Activites getOne(int id) throws SQLException {
        String req = "SELECT a.*, c.id as cat_id, c.nom as cat_nom, c.type as cat_type, " +
                "c.saison as cat_saison, c.niveauintensite as cat_intensite, c.publiccible as cat_public " +
                "FROM activites a " +
                "LEFT JOIN categories c ON a.categorie_id = c.id " +
                "WHERE a.id = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }

    // ─── Méthode de mapping centralisée ──────────────────────────────

    private Activites mapRow(ResultSet rs) throws SQLException {
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

        // latitude et longitude (colonnes ajoutées sprint web)
        double lat = rs.getDouble("latitude");
        p.setLatitude(rs.wasNull() ? null : lat);

        double lon = rs.getDouble("longitude");
        p.setLongitude(rs.wasNull() ? null : lon);

        // date_prevue : champ local Java uniquement, non persisté en BD commune
        // conservé pour compatibilité avec l'interface existante
        p.setDatePrevue(null);

        // Catégorie associée
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

        return p;
    }
}