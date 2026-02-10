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

        String sql = "INSERT INTO budget (montant_total, devise_budget, statut_budget, description_budget) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setBigDecimal(1, b.getMontantTotal());
        ps.setString(2, b.getDeviseBudget());
        ps.setString(3, b.getStatutBudget());
        ps.setString(4, b.getDescriptionBudget());
        ps.executeUpdate();
        System.out.println("✅ Budget ajouté avec succès !");
    }

    // ✏️ MODIFIER
    @Override
    public void modifier(Budget b) throws SQLException {

        String sql = "UPDATE budget SET montant_total = ?, devise_budget = ?, " +
                "statut_budget = ?, description_budget = ?, id_voyage = ? " +
                "WHERE id_budget = ?";

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setBigDecimal(1, b.getMontantTotal());
        ps.setString(2, b.getDeviseBudget());
        ps.setString(3, b.getStatutBudget());
        ps.setString(4, b.getDescriptionBudget());
        ps.setLong(6, b.getIdBudget());

        ps.executeUpdate();
        System.out.println("✏️ Budget modifié !");
    }

    // ❌ SUPPRIMER
    @Override
    public void supprimer(int id) throws SQLException {

        String sql = "DELETE FROM budget WHERE id_budget = ?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setLong(1, id);
        ps.executeUpdate();

        System.out.println("❌ Budget supprimé !");
    }

    // 📋 AFFICHER
    @Override
    public List<Budget> afficher() throws SQLException {

        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budget";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Budget b = new Budget(
                    rs.getLong("id_budget"),
                    rs.getBigDecimal("montant_total"),
                    rs.getString("devise_budget"),
                    rs.getString("statut_budget"),
                    rs.getString("description_budget")
            );
            budgets.add(b);
        }
        return budgets;
    }
}
