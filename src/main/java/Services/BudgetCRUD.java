package Services;

import Entities.Budget;
import Tools.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetCRUD {

    private Connection conn = MyBD.getInstance().getConn();

    // ===== AJOUTER =====
    public void ajouter(Budget b) throws SQLException {
        // UNIQUEMENT vérifier le libellé en double (optionnel)
        // if (budgetExistePourVoyageUtilisateur(b.getLibelleBudget(), b.getIdVoyage(), b.getId())) {
        //     throw new SQLException("❌ Un budget avec ce libellé existe déjà pour ce voyage !");
        // }

        // PLUS DE VÉRIFICATION D'UNICITÉ DU VOYAGE !!!
        // La ligne suivante est SUPPRIMÉE :
        // if (b.getIdVoyage() != 0 && voyageADejaUnBudget(b.getIdVoyage())) {
        //     throw new SQLException("❌ Ce voyage a déjà un budget associé !");
        // }

        String sql = "INSERT INTO budget (libelle_budget, montant_total, devise_budget, statut_budget, " +
                "description_budget, id, id_voyage) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getLibelleBudget());
            ps.setDouble(2, b.getMontantTotal());
            ps.setString(3, b.getDeviseBudget());
            ps.setString(4, b.getStatutBudget());
            ps.setString(5, b.getDescriptionBudget());
            ps.setInt(6, b.getId());
            ps.setInt(7, b.getIdVoyage());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    b.setIdBudget(generatedKeys.getInt(1));
                }
            }

            System.out.println("✅ Budget ajouté avec succès !");
        }
    }

    // ===== MODIFIER =====
    public void modifier(Budget b) throws SQLException {
        if (!budgetExists(b.getIdBudget())) {
            throw new SQLException("❌ Budget introuvable !");
        }

        // Optionnel : vérifier le libellé en double
        // if (budgetExistePourVoyageExclusionUtilisateur(b.getLibelleBudget(), b.getIdVoyage(), b.getId(), b.getIdBudget())) {
        //     throw new SQLException("❌ Un autre budget avec ce libellé existe déjà !");
        // }

        // PLUS DE VÉRIFICATION D'UNICITÉ DU VOYAGE !!!
        // La section suivante est SUPPRIMÉE :
        /*
        if (b.getIdVoyage() != 0) {
            Budget budgetExistant = getBudgetByVoyageId(b.getIdVoyage());
            if (budgetExistant != null && budgetExistant.getIdBudget() != b.getIdBudget()) {
                throw new SQLException("❌ Ce voyage est déjà associé au budget \"" +
                                      budgetExistant.getLibelleBudget() + "\" !");
            }
        }
        */

        String sql = "UPDATE budget SET libelle_budget = ?, montant_total = ?, devise_budget = ?, " +
                "statut_budget = ?, description_budget = ?, id_voyage = ? WHERE id_budget = ? AND id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getLibelleBudget());
            ps.setDouble(2, b.getMontantTotal());
            ps.setString(3, b.getDeviseBudget());
            ps.setString(4, b.getStatutBudget());
            ps.setString(5, b.getDescriptionBudget());
            ps.setInt(6, b.getIdVoyage());
            ps.setInt(7, b.getIdBudget());
            ps.setInt(8, b.getId());

            ps.executeUpdate();
            System.out.println("✏️ Budget modifié avec succès !");
        }
    }

    // ===== SUPPRIMER =====
    public void supprimer(int idBudget) throws SQLException {
        if (!budgetExists(idBudget)) {
            throw new SQLException("❌ Budget introuvable !");
        }

        // Supprimer les dépenses liées
        String sqlDepense = "DELETE FROM depense WHERE id_budget = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlDepense)) {
            ps.setInt(1, idBudget);
            ps.executeUpdate();
        }

        String sql = "DELETE FROM budget WHERE id_budget = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBudget);
            ps.executeUpdate();
            System.out.println("🗑 Budget supprimé !");
        }
    }

    // ===== AFFICHER =====
    public List<Budget> afficher() throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budget ORDER BY id_budget DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                budgets.add(extractBudgetFromResultSet(rs));
            }
        }

        return budgets;
    }

    // ===== GET BY ID =====
    public Budget getById(int idBudget) throws SQLException {
        String sql = "SELECT * FROM budget WHERE id_budget = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBudget);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractBudgetFromResultSet(rs);
            }
        }
        return null;
    }

    // ===== GET BY USER =====
    public List<Budget> getBudgetsByUserId(int userId) throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budget WHERE id = ? ORDER BY id_budget DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) budgets.add(extractBudgetFromResultSet(rs));
            }
        }
        return budgets;
    }

    // ===== GET BY VOYAGE =====
    public List<Budget> getBudgetsByVoyageId(int idVoyage) throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budget WHERE id_voyage = ? ORDER BY id_budget DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVoyage);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    budgets.add(extractBudgetFromResultSet(rs));
                }
            }
        }
        return budgets;
    }

    // ===== MÉTHODES UTILES =====
    public boolean budgetExists(int idBudget) throws SQLException {
        String sql = "SELECT COUNT(*) FROM budget WHERE id_budget = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBudget);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Cette méthode n'est plus utilisée mais on la garde
    public boolean voyageADejaUnBudget(int idVoyage) throws SQLException {
        return false; // Toujours retourner false car on permet plusieurs budgets par voyage
    }

    public boolean budgetExistePourVoyageUtilisateur(String libelle, int idVoyage, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM budget WHERE libelle_budget = ? AND id_voyage = ? AND id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libelle);
            ps.setInt(2, idVoyage);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean budgetExistePourVoyageExclusionUtilisateur(String libelle, int idVoyage, int userId, int idBudgetExclu) throws SQLException {
        String sql = "SELECT COUNT(*) FROM budget WHERE libelle_budget = ? AND id_voyage = ? AND id = ? AND id_budget != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libelle);
            ps.setInt(2, idVoyage);
            ps.setInt(3, userId);
            ps.setInt(4, idBudgetExclu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public double getMontantTotalByBudgetId(int idBudget) throws SQLException {
        String sql = "SELECT montant_total FROM budget WHERE id_budget = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBudget);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("montant_total");
            }
        }
        return 0;
    }

    // ===== EXTRACTION UTILE =====
    private Budget extractBudgetFromResultSet(ResultSet rs) throws SQLException {
        return new Budget(
                rs.getInt("id_budget"),
                rs.getString("libelle_budget"),
                rs.getDouble("montant_total"),
                rs.getString("devise_budget"),
                rs.getString("statut_budget"),
                rs.getString("description_budget"),
                rs.getInt("id"),
                rs.getInt("id_voyage")
        );
    }
    // Dans Services/BudgetCRUD.java, ajoutez/modifiez ces méthodes

    /**
     * Vérifie si un budget avec le même libellé existe déjà pour le même voyage et utilisateur
     */
    public boolean budgetExiste(String libelle, int idVoyage, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM budget WHERE libelle_budget = ? AND id_voyage = ? AND id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libelle);
            ps.setInt(2, idVoyage);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Vérifie si un budget avec le même libellé existe déjà (excluant un ID spécifique pour la modification)
     */
    public boolean budgetExisteExclusion(String libelle, int idVoyage, int userId, int idBudgetExclu) throws SQLException {
        String sql = "SELECT COUNT(*) FROM budget WHERE libelle_budget = ? AND id_voyage = ? AND id = ? AND id_budget != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libelle);
            ps.setInt(2, idVoyage);
            ps.setInt(3, userId);
            ps.setInt(4, idBudgetExclu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}