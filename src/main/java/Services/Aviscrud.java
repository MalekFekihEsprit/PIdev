package Services;

import Entities.Avis;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour la table `avis`.
 *
 * Colonnes gérées :
 *   id, note, commentaire, created_at, user_id, activite_id, is_flagged
 */
public class Aviscrud {

    private final Connection cnx;

    public Aviscrud() {
        cnx = MyBD.getInstance().getConn();
    }

    // ─── CREATE ──────────────────────────────────────────────────────

    public void ajouter(Avis avis) throws SQLException {
        String sql = "INSERT INTO avis (note, commentaire, created_at, user_id, activite_id, is_flagged) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, avis.getNote());
            ps.setString(2, avis.getCommentaire());
            ps.setTimestamp(3, avis.getCreatedAt() != null
                    ? Timestamp.valueOf(avis.getCreatedAt())
                    : Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, avis.getUserId());
            ps.setInt(5, avis.getActiviteId());
            ps.setBoolean(6, avis.isFlagged());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) avis.setId(rs.getInt(1));
            }
        }
    }

    // ─── READ ────────────────────────────────────────────────────────

    public List<Avis> afficher() throws SQLException {
        List<Avis> liste = new ArrayList<>();
        String sql = "SELECT * FROM avis ORDER BY created_at DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    public List<Avis> afficherParActivite(int activiteId, boolean includeFlagged) throws SQLException {
        List<Avis> liste = new ArrayList<>();
        String sql = "SELECT * FROM avis WHERE activite_id = ?"
                + (includeFlagged ? "" : " AND is_flagged = 0")
                + " ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, activiteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    public double getMoyenneNote(int activiteId) throws SQLException {
        String sql = "SELECT AVG(note) FROM avis WHERE activite_id = ? AND is_flagged = 0";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, activiteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return Math.round(avg * 10.0) / 10.0;
                }
            }
        }
        return 0.0;
    }

    // ─── UPDATE ──────────────────────────────────────────────────────

    public void modifier(Avis avis) throws SQLException {
        String sql = "UPDATE avis SET note = ?, commentaire = ?, is_flagged = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, avis.getNote());
            ps.setString(2, avis.getCommentaire());
            ps.setBoolean(3, avis.isFlagged());
            ps.setInt(4, avis.getId());
            ps.executeUpdate();
        }
    }

    public void toggleFlag(int avisId, boolean flagged) throws SQLException {
        String sql = "UPDATE avis SET is_flagged = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBoolean(1, flagged);
            ps.setInt(2, avisId);
            ps.executeUpdate();
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────────

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM avis WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── Mapping ─────────────────────────────────────────────────────

    private Avis mapRow(ResultSet rs) throws SQLException {
        Avis a = new Avis();
        a.setId(rs.getInt("id"));
        a.setNote(rs.getInt("note"));
        a.setCommentaire(rs.getString("commentaire"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());

        a.setUserId(rs.getInt("user_id"));
        a.setActiviteId(rs.getInt("activite_id"));
        a.setFlagged(rs.getBoolean("is_flagged"));
        return a;
    }
}