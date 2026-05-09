package Services;

import Entities.Evenement;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour la table `evenement`.
 *
 * Colonnes gérées :
 *   id, titre, description, date, heure, lieu, nb_places,
 *   lien_groupe, image_path, latitude, longitude, telegram_group_id
 */
public class Evenementcrud {

    private final Connection cnx;

    public Evenementcrud() {
        cnx = MyBD.getInstance().getConn();
    }

    // ─── CREATE ──────────────────────────────────────────────────────

    public void ajouter(Evenement ev) throws SQLException {
        String sql = "INSERT INTO evenement "
                + "(titre, description, date, heure, lieu, nb_places, "
                + " lien_groupe, image_path, latitude, longitude, telegram_group_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ev.getTitre());
            ps.setString(2, ev.getDescription());
            ps.setDate(3, ev.getDate() != null ? Date.valueOf(ev.getDate()) : null);
            ps.setTime(4, ev.getHeure() != null ? Time.valueOf(ev.getHeure()) : null);
            ps.setString(5, ev.getLieu());
            ps.setInt(6, ev.getNbPlaces());
            ps.setString(7, ev.getLienGroupe());
            ps.setString(8, ev.getImagePath());
            setNullableDouble(ps, 9, ev.getLatitude());
            setNullableDouble(ps, 10, ev.getLongitude());
            ps.setString(11, ev.getTelegramGroupId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) ev.setId(rs.getInt(1));
            }
        }
    }

    // ─── READ ────────────────────────────────────────────────────────

    public List<Evenement> afficher() throws SQLException {
        List<Evenement> liste = new ArrayList<>();
        String sql = "SELECT * FROM evenement ORDER BY date ASC, heure ASC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    public List<Evenement> afficherAVenir() throws SQLException {
        List<Evenement> liste = new ArrayList<>();
        String sql = "SELECT * FROM evenement WHERE date >= CURDATE() ORDER BY date ASC, heure ASC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    public Evenement getOne(int id) throws SQLException {
        String sql = "SELECT * FROM evenement WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int getNbParticipants(int evenementId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM participation_evenement WHERE evenement_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ─── UPDATE ──────────────────────────────────────────────────────

    public void modifier(Evenement ev) throws SQLException {
        String sql = "UPDATE evenement SET titre=?, description=?, date=?, heure=?, lieu=?, "
                + "nb_places=?, lien_groupe=?, image_path=?, latitude=?, longitude=?, "
                + "telegram_group_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, ev.getTitre());
            ps.setString(2, ev.getDescription());
            ps.setDate(3, ev.getDate() != null ? Date.valueOf(ev.getDate()) : null);
            ps.setTime(4, ev.getHeure() != null ? Time.valueOf(ev.getHeure()) : null);
            ps.setString(5, ev.getLieu());
            ps.setInt(6, ev.getNbPlaces());
            ps.setString(7, ev.getLienGroupe());
            ps.setString(8, ev.getImagePath());
            setNullableDouble(ps, 9, ev.getLatitude());
            setNullableDouble(ps, 10, ev.getLongitude());
            ps.setString(11, ev.getTelegramGroupId());
            ps.setInt(12, ev.getId());
            ps.executeUpdate();
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────────

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM evenement WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── Mapping ─────────────────────────────────────────────────────

    private Evenement mapRow(ResultSet rs) throws SQLException {
        Evenement ev = new Evenement();
        ev.setId(rs.getInt("id"));
        ev.setTitre(rs.getString("titre"));
        ev.setDescription(rs.getString("description"));

        Date d = rs.getDate("date");
        if (d != null) ev.setDate(d.toLocalDate());

        Time t = rs.getTime("heure");
        if (t != null) ev.setHeure(t.toLocalTime());

        ev.setLieu(rs.getString("lieu"));
        ev.setNbPlaces(rs.getInt("nb_places"));
        ev.setLienGroupe(rs.getString("lien_groupe"));
        ev.setImagePath(rs.getString("image_path"));

        double lat = rs.getDouble("latitude");
        ev.setLatitude(rs.wasNull() ? null : lat);

        double lon = rs.getDouble("longitude");
        ev.setLongitude(rs.wasNull() ? null : lon);

        ev.setTelegramGroupId(rs.getString("telegram_group_id"));
        return ev;
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) ps.setNull(index, Types.DOUBLE);
        else ps.setDouble(index, value);
    }
}