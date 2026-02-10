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

        String sql = "INSERT INTO depense (libelle_depense, montant_depense, categorie_depense, " +
                "description_depense, devise_depense, type_paiement, date_creation, id_budget) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, d.getLibelleDepense());
        ps.setBigDecimal(2, d.getMontantDepense());
        ps.setString(3, d.getCategorieDepense());
        ps.setString(4, d.getDescriptionDepense());
        ps.setString(5, d.getDeviseDepense());
        ps.setString(6, d.getTypePaiement());
        ps.setDate(7, Date.valueOf(d.getDateCreation()));
        ps.setLong(8, d.getIdBudget());

        ps.executeUpdate();
        System.out.println("✅ Dépense ajoutée !");
    }

    // ✏️ MODIFIER
    @Override
    public void modifier(Depense d) throws SQLException {

        String sql = "UPDATE depense SET libelle_depense = ?, montant_depense = ?, categorie_depense = ?, " +
                "description_depense = ?, devise_depense = ?, type_paiement = ?, date_creation = ?, id_budget = ? " +
                "WHERE id_depense = ?";

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, d.getLibelleDepense());
        ps.setBigDecimal(2, d.getMontantDepense());
        ps.setString(3, d.getCategorieDepense());
        ps.setString(4, d.getDescriptionDepense());
        ps.setString(5, d.getDeviseDepense());
        ps.setString(6, d.getTypePaiement());
        ps.setDate(7, Date.valueOf(d.getDateCreation()));
        ps.setLong(8, d.getIdBudget());
        ps.setLong(9, d.getIdDepense());

        ps.executeUpdate();
        System.out.println("✏️ Dépense modifiée !");
    }

    // ❌ SUPPRIMER
    @Override
    public void supprimer(int id) throws SQLException {

        String sql = "DELETE FROM depense WHERE id_depense = ?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setLong(1, id);
        ps.executeUpdate();

        System.out.println("❌ Dépense supprimée !");
    }

    // 📋 AFFICHER
    @Override
    public List<Depense> afficher() throws SQLException {

        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depense";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Depense d = new Depense(
                    rs.getLong("id_depense"),
                    rs.getString("libelle_depense"),
                    rs.getBigDecimal("montant_depense"),
                    rs.getString("categorie_depense"),
                    rs.getString("description_depense"),
                    rs.getString("devise_depense"),
                    rs.getString("type_paiement"),
                    rs.getDate("date_creation").toLocalDate(),
                    rs.getLong("id_budget")
            );
            depenses.add(d);
        }
        return depenses;
    }
}

