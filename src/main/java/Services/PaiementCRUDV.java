package Services;

import Entities.Paiement;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementCRUDV implements InterfaceCRUDV<Paiement> {

    private Connection con;

    public PaiementCRUDV() {
        con = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Paiement paiement) throws SQLException {
        String req = "INSERT INTO paiement (id_voyage, id_utilisateur, montant, devise, methode, statut, transaction_id, date_paiement, description, email_payeur) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setInt(1, paiement.getId_voyage());
        pst.setInt(2, paiement.getId_utilisateur());
        pst.setDouble(3, paiement.getMontant());
        pst.setString(4, paiement.getDevise());
        pst.setString(5, paiement.getMethode());
        pst.setString(6, paiement.getStatut());
        pst.setString(7, paiement.getTransactionId());
        pst.setTimestamp(8, paiement.getDate_paiement());
        pst.setString(9, paiement.getDescription());
        pst.setString(10, paiement.getEmail_payeur());

        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            paiement.setId_paiement(rs.getInt(1));
        }

        System.out.println("Paiement ajouté avec succès !");
    }

    @Override
    public void modifier(Paiement paiement) throws SQLException {
        String req = "UPDATE paiement SET id_voyage=?, id_utilisateur=?, montant=?, devise=?, methode=?, " +
                "statut=?, transaction_id=?, description=?, email_payeur=? WHERE id_paiement=?";

        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, paiement.getId_voyage());
        pst.setInt(2, paiement.getId_utilisateur());
        pst.setDouble(3, paiement.getMontant());
        pst.setString(4, paiement.getDevise());
        pst.setString(5, paiement.getMethode());
        pst.setString(6, paiement.getStatut());
        pst.setString(7, paiement.getTransactionId());
        pst.setString(8, paiement.getDescription());
        pst.setString(9, paiement.getEmail_payeur());
        pst.setInt(10, paiement.getId_paiement());

        pst.executeUpdate();
        System.out.println("Paiement modifié avec succès !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM paiement WHERE id_paiement=?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Paiement supprimé avec succès !");
    }

    @Override
    public List<Paiement> afficher() throws SQLException {
        List<Paiement> paiements = new ArrayList<>();
        String req = "SELECT * FROM paiement ORDER BY date_paiement DESC";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Paiement p = new Paiement();
            p.setId_paiement(rs.getInt("id_paiement"));
            p.setId_voyage(rs.getInt("id_voyage"));
            p.setId_utilisateur(rs.getInt("id_utilisateur"));
            p.setMontant(rs.getDouble("montant"));
            p.setDevise(rs.getString("devise"));
            p.setMethode(rs.getString("methode"));
            p.setStatut(rs.getString("statut"));
            p.setTransactionId(rs.getString("transaction_id"));
            p.setDate_paiement(rs.getTimestamp("date_paiement"));
            p.setDescription(rs.getString("description"));
            p.setEmail_payeur(rs.getString("email_payeur"));

            paiements.add(p);
        }

        return paiements;
    }

    public List<Paiement> getPaiementsByVoyage(int idVoyage) throws SQLException {
        List<Paiement> paiements = new ArrayList<>();
        String req = "SELECT * FROM paiement WHERE id_voyage = ? ORDER BY date_paiement DESC";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, idVoyage);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Paiement p = new Paiement();
            p.setId_paiement(rs.getInt("id_paiement"));
            p.setId_voyage(rs.getInt("id_voyage"));
            p.setId_utilisateur(rs.getInt("id_utilisateur"));
            p.setMontant(rs.getDouble("montant"));
            p.setDevise(rs.getString("devise"));
            p.setMethode(rs.getString("methode"));
            p.setStatut(rs.getString("statut"));
            p.setTransactionId(rs.getString("transaction_id"));
            p.setDate_paiement(rs.getTimestamp("date_paiement"));
            p.setDescription(rs.getString("description"));
            p.setEmail_payeur(rs.getString("email_payeur"));

            paiements.add(p);
        }

        return paiements;
    }

    public Paiement getPaiementByTransactionId(String transactionId) throws SQLException {
        String req = "SELECT * FROM paiement WHERE transaction_id = ?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setString(1, transactionId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            Paiement p = new Paiement();
            p.setId_paiement(rs.getInt("id_paiement"));
            p.setId_voyage(rs.getInt("id_voyage"));
            p.setId_utilisateur(rs.getInt("id_utilisateur"));
            p.setMontant(rs.getDouble("montant"));
            p.setDevise(rs.getString("devise"));
            p.setMethode(rs.getString("methode"));
            p.setStatut(rs.getString("statut"));
            p.setTransactionId(rs.getString("transaction_id"));
            p.setDate_paiement(rs.getTimestamp("date_paiement"));
            p.setDescription(rs.getString("description"));
            p.setEmail_payeur(rs.getString("email_payeur"));
            return p;
        }

        return null;
    }
}