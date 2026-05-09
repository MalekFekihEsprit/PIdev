package Services;

import Entities.ParticipationEvenement;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour la table `participation_evenement`.
 *
 * Colonnes gérées : id, user_id, evenement_id, created_at
 */
public class Participationevenementcrud {

    private final Connection cnx;

    public Participationevenementcrud() {
        cnx = MyBD.getInstance().getConn();
    }

    // ─── CREATE ──────────────────────────────────────────────────────

    public void inscrire(ParticipationEvenement participation) throws SQLException {
        if (estDejaInscrit(participation.getUserId(), participation.getEvenementId())) {
            throw new SQLException("L'utilisateur est déjà inscrit à cet événement.");
        }

        String sql = "INSERT INTO participation_evenement (user_id, evenement_id, created_at) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, participation.getUserId());
            ps.setInt(2, participation.getEvenementId());
            ps.setTimestamp(3, participation.getCreatedAt() != null
                    ? Timestamp.valueOf(participation.getCreatedAt())
                    : Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) participation.setId(rs.getInt(1));
            }
        }
    }

    // ─── READ ────────────────────────────────────────────────────────

    public List<ParticipationEvenement> afficherParEvenement(int evenementId) throws SQLException {
        List<ParticipationEvenement> liste = new ArrayList<>();
        String sql = "SELECT * FROM participation_evenement WHERE evenement_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    public List<ParticipationEvenement> afficherParUtilisateur(int userId) throws SQLException {
        List<ParticipationEvenement> liste = new ArrayList<>();
        String sql = "SELECT * FROM participation_evenement WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    public boolean estDejaInscrit(int userId, int evenementId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM participation_evenement "
                + "WHERE user_id = ? AND evenement_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public int compterParticipants(int evenementId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM participation_evenement WHERE evenement_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ─── DELETE ──────────────────────────────────────────────────────

    public void desinscrire(int userId, int evenementId) throws SQLException {
        String sql = "DELETE FROM participation_evenement WHERE user_id = ? AND evenement_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, evenementId);
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM participation_evenement WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── Mapping ─────────────────────────────────────────────────────

    private ParticipationEvenement mapRow(ResultSet rs) throws SQLException {
        ParticipationEvenement p = new ParticipationEvenement();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setEvenementId(rs.getInt("evenement_id"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());

        return p;
    }
}