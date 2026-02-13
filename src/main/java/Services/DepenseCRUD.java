package Services;

import Entities.Depense;
import Tools.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepenseCRUD implements InterfaceCRUD<Depense> {

    private Connection conn = MyBD.getInstance().getConn();

    // ➕ AJOUTER
    @Override
    public void ajouter(Depense d) throws SQLException {
        String sql = "INSERT INTO depense (montant_depense, libelle_depense, categorie_depense, " +
                "description_depense, devise_depense, type_paiement, date_creation, id_budget) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, d.getMontantDepense());
            ps.setString(2, d.getLibelleDepense());
            ps.setString(3, d.getCategorieDepense());
            ps.setString(4, d.getDescriptionDepense());

            if (d.getDeviseDepense() != null) {
                ps.setString(5, d.getDeviseDepense());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            ps.setString(6, d.getTypePaiement());
            ps.setDate(7, d.getDateCreation());
            ps.setInt(8, d.getIdBudget());

            ps.executeUpdate();
            System.out.println("✅ Dépense ajoutée !");
        }
    }

    // ✏️ MODIFIER
    @Override
    public void modifier(Depense d) throws SQLException {
        String sql = "UPDATE depense SET montant_depense = ?, libelle_depense = ?, categorie_depense = ?, " +
                "description_depense = ?, devise_depense = ?, type_paiement = ?, date_creation = ?, id_budget = ? " +
                "WHERE id_depense = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, d.getMontantDepense());
            ps.setString(2, d.getLibelleDepense());
            ps.setString(3, d.getCategorieDepense());
            ps.setString(4, d.getDescriptionDepense());

            if (d.getDeviseDepense() != null) {
                ps.setString(5, d.getDeviseDepense());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            ps.setString(6, d.getTypePaiement());
            ps.setDate(7, d.getDateCreation());
            ps.setInt(8, d.getIdBudget());
            ps.setInt(9, d.getIdDepense());

            ps.executeUpdate();
            System.out.println("✏️ Dépense modifiée !");
        }
    }

    // ❌ SUPPRIMER
    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM depense WHERE id_depense = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("❌ Dépense supprimée !");
        }
    }

    // 📋 AFFICHER
    @Override
    public List<Depense> afficher() throws SQLException {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depense";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Depense d = new Depense(
                        rs.getInt("id_depense"),
                        rs.getFloat("montant_depense"),
                        rs.getString("libelle_depense"),
                        rs.getString("categorie_depense"),
                        rs.getString("description_depense"),
                        rs.getString("devise_depense"),
                        rs.getString("type_paiement"),
                        rs.getDate("date_creation"),
                        rs.getInt("id_budget")
                );
                depenses.add(d);
            }
        }
        return depenses;
    }

    // 🔍 Récupérer par ID
    public Depense getById(int id) throws SQLException {
        String sql = "SELECT * FROM depense WHERE id_depense = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Depense(
                            rs.getInt("id_depense"),
                            rs.getFloat("montant_depense"),
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
        }
        return null;
    }
}
