package Services;

import Entities.VoyageNotification;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoyageNotificationCRUD {

    private final Connection conn;

    public VoyageNotificationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    /**
     * Returns all non-dismissed voyage notifications for a user,
     * joined with voyage and destination for display info.
     */
    public List<VoyageNotification> getUndismissedForUser(int userId) throws SQLException {
        String sql =
            "SELECT n.id_notification, n.id_user, n.id_voyage, n.created_at, n.is_dismissed, " +
            "       v.titre_voyage, v.statut, v.date_debut, v.date_fin, " +
            "       d.nom_destination " +
            "FROM destination_voyage_notification n " +
            "JOIN voyage v ON n.id_voyage = v.id_voyage " +
            "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
            "WHERE n.id_user = ? AND n.is_dismissed = 0 " +
            "ORDER BY n.created_at DESC";

        List<VoyageNotification> list = new ArrayList<>();
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    VoyageNotification n = new VoyageNotification();
                    n.setId_notification(rs.getInt("id_notification"));
                    n.setId_user(rs.getInt("id_user"));
                    n.setId_voyage(rs.getInt("id_voyage"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) n.setCreated_at(ts.toLocalDateTime());
                    n.setIs_dismissed(rs.getBoolean("is_dismissed"));
                    n.setTitre_voyage(rs.getString("titre_voyage"));
                    n.setStatut_voyage(rs.getString("statut"));
                    n.setDate_debut(rs.getDate("date_debut"));
                    n.setDate_fin(rs.getDate("date_fin"));
                    n.setNom_destination(rs.getString("nom_destination"));
                    list.add(n);
                }
            }
        }
        return list;
    }

    /** Marks a single notification as dismissed. */
    public void dismiss(int notificationId) throws SQLException {
        String sql = "UPDATE destination_voyage_notification SET is_dismissed = 1 WHERE id_notification = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, notificationId);
            pst.executeUpdate();
        }
    }

    /** Dismisses all voyage notifications for a user. */
    public void dismissAll(int userId) throws SQLException {
        String sql = "UPDATE destination_voyage_notification SET is_dismissed = 1 WHERE id_user = ? AND is_dismissed = 0";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }
}
