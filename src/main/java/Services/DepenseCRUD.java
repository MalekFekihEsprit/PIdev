package Services;

import Entities.Depense;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepenseCRUD {

    private Connection conn = MyBD.getInstance().getConn();
    private BudgetCRUD budgetCRUD = new BudgetCRUD();

    // ===== AJOUT =====
    public void ajouter(Depense d) throws SQLException {
        if (!budgetCRUD.budgetExists(d.getIdBudget())) {
            throw new SQLException("Budget introuvable !");
        }

        if (depenseExistePourBudget(d.getLibelleDepense(), d.getIdBudget())) {
            throw new SQLException("Dépense déjà existante pour ce budget !");
        }

        double budgetTotal = budgetCRUD.getMontantTotalByBudgetId(d.getIdBudget());
        double depensesTotal = getTotalDepensesByBudgetId(d.getIdBudget());
        double reste = budgetTotal - depensesTotal;
        if (d.getMontantDepense() > reste) {
            throw new SQLException("Montant dépasse le budget restant ! (" + reste + ")");
        }

        String sql = "INSERT INTO depense (montant_depense, libelle_depense, categorie_depense, description_depense, " +
                "devise_depense, type_paiement, date_creation, id_budget) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, d.getMontantDepense());
            ps.setString(2, d.getLibelleDepense());
            ps.setString(3, d.getCategorieDepense());
            ps.setString(4, d.getDescriptionDepense());
            ps.setString(5, d.getDeviseDepense());
            ps.setString(6, d.getTypePaiement());
            ps.setDate(7, d.getDateCreation());
            ps.setInt(8, d.getIdBudget());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) d.setIdDepense(rs.getInt(1));
            }
        }
    }

    // ===== MODIFIER =====
    public void modifier(Depense d) throws SQLException {
        if (!depenseExists(d.getIdDepense()))
            throw new SQLException("Dépense introuvable !");

        double ancienMontant = getById(d.getIdDepense()).getMontantDepense();
        double budgetTotal = budgetCRUD.getMontantTotalByBudgetId(d.getIdBudget());
        double depensesTotal = getTotalDepensesByBudgetId(d.getIdBudget()) - ancienMontant;
        double reste = budgetTotal - depensesTotal;
        if (d.getMontantDepense() > reste)
            throw new SQLException("Montant dépasse le budget restant ! (" + reste + ")");

        String sql = "UPDATE depense SET montant_depense=?, libelle_depense=?, categorie_depense=?, " +
                "description_depense=?, devise_depense=?, type_paiement=?, date_creation=?, id_budget=? " +
                "WHERE id_depense=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, d.getMontantDepense());
            ps.setString(2, d.getLibelleDepense());
            ps.setString(3, d.getCategorieDepense());
            ps.setString(4, d.getDescriptionDepense());
            ps.setString(5, d.getDeviseDepense());
            ps.setString(6, d.getTypePaiement());
            ps.setDate(7, d.getDateCreation());
            ps.setInt(8, d.getIdBudget());
            ps.setInt(9, d.getIdDepense());
            ps.executeUpdate();
        }
    }

    // ===== SUPPRIMER =====
    public void supprimer(int idDepense) throws SQLException {
        if (!depenseExists(idDepense))
            throw new SQLException("Dépense introuvable !");
        String sql = "DELETE FROM depense WHERE id_depense=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDepense);
            ps.executeUpdate();
        }
    }

    // ===== AFFICHER =====
    public List<Depense> afficher() throws SQLException {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depense ORDER BY date_creation DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) depenses.add(extractDepense(rs));
        }
        return depenses;
    }

    // Dans Services/DepenseCRUD.java, ajoutez ces méthodes :

    /**
     * Récupère toutes les dépenses d'un budget spécifique
     */
    public List<Depense> getDepensesByBudgetId(int idBudget) throws SQLException {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depense WHERE id_budget = ? ORDER BY date_creation DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBudget);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    depenses.add(extractDepense(rs));
                }
            }
        }
        return depenses;
    }

    /**
     * Récupère toutes les dépenses (sans filtre)
     */
    public List<Depense> getAllDepenses() throws SQLException {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depense ORDER BY date_creation DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                depenses.add(extractDepense(rs));
            }
        }
        return depenses;
    }

    // ===== GET BY ID =====
    public Depense getById(int id) throws SQLException {
        String sql = "SELECT * FROM depense WHERE id_depense=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractDepense(rs);
            }
        }
        return null;
    }

    // ===== TOTAL =====
    public double getTotalDepensesByBudgetId(int idBudget) throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant_depense),0) FROM depense WHERE id_budget=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBudget);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    // ===== UTILITAIRES =====
    public boolean depenseExists(int idDepense) throws SQLException {
        String sql = "SELECT COUNT(*) FROM depense WHERE id_depense=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDepense);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean depenseExistePourBudget(String libelle, int idBudget) throws SQLException {
        String sql = "SELECT COUNT(*) FROM depense WHERE libelle_depense=? AND id_budget=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libelle);
            ps.setInt(2, idBudget);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    private Budgetnotificationservice notifier =
            new Budgetnotificationservice("budget-alerts-Yosr");

    // ===== EXTRACTION =====
    private Depense extractDepense(ResultSet rs) throws SQLException {
        return new Depense(
                rs.getInt("id_depense"),
                rs.getDouble("montant_depense"),
                rs.getString("libelle_depense"),
                rs.getString("categorie_depense"),
                rs.getString("description_depense"),
                rs.getString("devise_depense"),
                rs.getString("type_paiement"),
                rs.getDate("date_creation"),
                rs.getInt("id_budget")
        );
    }
}
