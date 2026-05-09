package Services;

import Entities.Reservation;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour la table `reservations`.
 *
 * Colonnes gérées :
 *   id, activite_id, nom, prenom, telephone, email, commentaire,
 *   montant_total, acompte, statut_paiement, methode_confirmation,
 *   code_confirmation, qr_code_path, date_reservation,
 *   date_confirmation, date_paiement, transaction_id
 */
public class Reservationcrud {

    private final Connection cnx;

    public Reservationcrud() {
        cnx = MyBD.getInstance().getConn();
    }

    // ─── CREATE ──────────────────────────────────────────────────────

    public void ajouter(Reservation r) throws SQLException {
        String sql = "INSERT INTO reservations "
                + "(activite_id, nom, prenom, telephone, email, commentaire, "
                + " montant_total, acompte, statut_paiement, methode_confirmation, "
                + " code_confirmation, qr_code_path, date_reservation, "
                + " date_confirmation, date_paiement, transaction_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getActiviteId());
            ps.setString(2, r.getNom());
            ps.setString(3, r.getPrenom());
            ps.setString(4, r.getTelephone());
            ps.setString(5, r.getEmail());
            ps.setString(6, r.getCommentaire());
            ps.setDouble(7, r.getMontantTotal());
            ps.setDouble(8, r.getAcompte());
            ps.setString(9, r.getStatutPaiement() != null ? r.getStatutPaiement() : "en_attente");
            ps.setString(10, r.getMethodeConfirmation());
            ps.setString(11, r.getCodeConfirmation());
            ps.setString(12, r.getQrCodePath());
            ps.setTimestamp(13, r.getDateReservation() != null
                    ? Timestamp.valueOf(r.getDateReservation())
                    : Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(14, r.getDateConfirmation() != null
                    ? Timestamp.valueOf(r.getDateConfirmation()) : null);
            ps.setTimestamp(15, r.getDatePaiement() != null
                    ? Timestamp.valueOf(r.getDatePaiement()) : null);
            ps.setString(16, r.getTransactionId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) r.setId(rs.getInt(1));
            }
        }
    }

    // ─── READ ────────────────────────────────────────────────────────

    public List<Reservation> afficher() throws SQLException {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY date_reservation DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    public List<Reservation> afficherParActivite(int activiteId) throws SQLException {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE activite_id = ? ORDER BY date_reservation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, activiteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    public List<Reservation> afficherParEmail(String email) throws SQLException {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE email = ? ORDER BY date_reservation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    public Reservation getOne(int id) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int compterReservationsConfirmees(int activiteId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations "
                + "WHERE activite_id = ? AND statut_paiement = 'confirme'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, activiteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ─── UPDATE ──────────────────────────────────────────────────────

    public void modifier(Reservation r) throws SQLException {
        String sql = "UPDATE reservations SET "
                + "activite_id=?, nom=?, prenom=?, telephone=?, email=?, commentaire=?, "
                + "montant_total=?, acompte=?, statut_paiement=?, methode_confirmation=?, "
                + "code_confirmation=?, qr_code_path=?, date_confirmation=?, "
                + "date_paiement=?, transaction_id=? "
                + "WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getActiviteId());
            ps.setString(2, r.getNom());
            ps.setString(3, r.getPrenom());
            ps.setString(4, r.getTelephone());
            ps.setString(5, r.getEmail());
            ps.setString(6, r.getCommentaire());
            ps.setDouble(7, r.getMontantTotal());
            ps.setDouble(8, r.getAcompte());
            ps.setString(9, r.getStatutPaiement());
            ps.setString(10, r.getMethodeConfirmation());
            ps.setString(11, r.getCodeConfirmation());
            ps.setString(12, r.getQrCodePath());
            ps.setTimestamp(13, r.getDateConfirmation() != null
                    ? Timestamp.valueOf(r.getDateConfirmation()) : null);
            ps.setTimestamp(14, r.getDatePaiement() != null
                    ? Timestamp.valueOf(r.getDatePaiement()) : null);
            ps.setString(15, r.getTransactionId());
            ps.setInt(16, r.getId());
            ps.executeUpdate();
        }
    }

    public void confirmer(int id) throws SQLException {
        String sql = "UPDATE reservations SET statut_paiement='confirme', "
                + "date_confirmation=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void mettreAJourQrCode(int id, String qrCodePath) throws SQLException {
        String sql = "UPDATE reservations SET qr_code_path=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, qrCodePath);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────────

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── Mapping ─────────────────────────────────────────────────────

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setActiviteId(rs.getInt("activite_id"));
        r.setNom(rs.getString("nom"));
        r.setPrenom(rs.getString("prenom"));
        r.setTelephone(rs.getString("telephone"));
        r.setEmail(rs.getString("email"));
        r.setCommentaire(rs.getString("commentaire"));
        r.setMontantTotal(rs.getDouble("montant_total"));
        r.setAcompte(rs.getDouble("acompte"));
        r.setStatutPaiement(rs.getString("statut_paiement"));
        r.setMethodeConfirmation(rs.getString("methode_confirmation"));
        r.setCodeConfirmation(rs.getString("code_confirmation"));
        r.setQrCodePath(rs.getString("qr_code_path"));

        Timestamp dr = rs.getTimestamp("date_reservation");
        if (dr != null) r.setDateReservation(dr.toLocalDateTime());

        Timestamp dc = rs.getTimestamp("date_confirmation");
        if (dc != null) r.setDateConfirmation(dc.toLocalDateTime());

        Timestamp dp = rs.getTimestamp("date_paiement");
        if (dp != null) r.setDatePaiement(dp.toLocalDateTime());

        r.setTransactionId(rs.getString("transaction_id"));
        return r;
    }
}