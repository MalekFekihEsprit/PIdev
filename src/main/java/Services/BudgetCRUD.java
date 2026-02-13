package Services;

import Entities.Budget;
import Tools.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetCRUD implements InterfaceCRUD<Budget> {

    private Connection conn = MyBD.getInstance().getConn();

    // ➕ AJOUTER
    @Override
    public void ajouter(Budget b) throws SQLException {
        String sql = "INSERT INTO budget (montant_total, devise_budget, statut_budget, description_budget, id, id_voyage) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, b.getMontantTotal());
            ps.setString(2, b.getDeviseBudget());
            ps.setString(3, b.getStatutBudget());
            ps.setString(4, b.getDescriptionBudget());
            ps.setInt(5, b.getId());

            if (b.getIdVoyage() != 0) {
                ps.setInt(6, b.getIdVoyage());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.executeUpdate();
            System.out.println("✅ Budget ajouté avec succès !");
        }
    }

    // ✏️ MODIFIER
    @Override
    public void modifier(Budget b) throws SQLException {
        String sql = "UPDATE budget SET montant_total = ?, devise_budget = ?, statut_budget = ?, " +
                "description_budget = ?, id = ?, id_voyage = ? WHERE id_budget = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, b.getMontantTotal());
            ps.setString(2, b.getDeviseBudget());
            ps.setString(3, b.getStatutBudget());
            ps.setString(4, b.getDescriptionBudget());
            ps.setInt(5, b.getId());

            if (b.getIdVoyage() != 0) {
                ps.setInt(6, b.getIdVoyage());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setInt(7, b.getIdBudget());

            ps.executeUpdate();
            System.out.println("✏️ Budget modifié !");
        }
    }

    // ❌ SUPPRIMER
    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM budget WHERE id_budget = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("❌ Budget supprimé !");
        }
    }

    // 📋 AFFICHER
    @Override
    public List<Budget> afficher() throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budget";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Budget b = new Budget(
                        rs.getInt("id_budget"),
                        rs.getFloat("montant_total"),
                        rs.getString("devise_budget"),
                        rs.getString("statut_budget"),
                        rs.getString("description_budget"),
                        rs.getInt("id"),
                        rs.getObject("id_voyage") != null ? rs.getInt("id_voyage") : 0
                );
                budgets.add(b);
            }
        }
        return budgets;
    }

    // 🔍 Récupérer un budget par ID
    public Budget getById(int id) throws SQLException {
        String sql = "SELECT * FROM budget WHERE id_budget = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Budget(
                            rs.getInt("id_budget"),
                            rs.getFloat("montant_total"),
                            rs.getString("devise_budget"),
                            rs.getString("statut_budget"),
                            rs.getString("description_budget"),
                            rs.getInt("id"),
                            rs.getObject("id_voyage") != null ? rs.getInt("id_voyage") : 0
                    );
                }
            }
        }
        return null;
    }
}
