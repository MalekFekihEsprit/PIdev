package Services;

import Entities.DeleteNotification;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeleteNotificationCRUD {
    Connection conn;

    public DeleteNotificationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    public void ajouter(DeleteNotification notification) throws SQLException {
        String req = "INSERT INTO delete_notifications (" +
                "user_id, user_name, admin_id, admin_name, item_type, " +
                "item_id, item_name, reason, custom_reason, deleted_at, is_read) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, notification.getUser_id());
            pst.setString(2, notification.getUser_name());
            pst.setInt(3, notification.getAdmin_id());
            pst.setString(4, notification.getAdmin_name());
            pst.setString(5, notification.getItem_type());
            pst.setInt(6, notification.getItem_id());
            pst.setString(7, notification.getItem_name());
            pst.setString(8, notification.getReason());
            pst.setString(9, notification.getCustom_reason());
            pst.setTimestamp(10, Timestamp.valueOf(notification.getDeleted_at()));
            pst.setBoolean(11, notification.isIs_read());

            pst.executeUpdate();
            System.out.println("Notification de suppression ajoutée");
        }
    }

    public List<DeleteNotification> getNotificationsForUser(int userId) throws SQLException {
        String req = "SELECT * FROM delete_notifications WHERE user_id = ? ORDER BY deleted_at DESC";
        List<DeleteNotification> notifications = new ArrayList<>();

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                DeleteNotification n = new DeleteNotification();
                n.setId_notification(rs.getInt("id_notification"));
                n.setUser_id(rs.getInt("user_id"));
                n.setUser_name(rs.getString("user_name"));
                n.setAdmin_id(rs.getInt("admin_id"));
                n.setAdmin_name(rs.getString("admin_name"));
                n.setItem_type(rs.getString("item_type"));
                n.setItem_id(rs.getInt("item_id"));
                n.setItem_name(rs.getString("item_name"));
                n.setReason(rs.getString("reason"));
                n.setCustom_reason(rs.getString("custom_reason"));
                n.setDeleted_at(rs.getTimestamp("deleted_at").toLocalDateTime());
                n.setIs_read(rs.getBoolean("is_read"));
                notifications.add(n);
            }
        }
        return notifications;
    }

    public List<DeleteNotification> getUnreadNotificationsForUser(int userId) throws SQLException {
        String req = "SELECT * FROM delete_notifications WHERE user_id = ? AND is_read = false ORDER BY deleted_at DESC";
        List<DeleteNotification> notifications = new ArrayList<>();

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                DeleteNotification n = new DeleteNotification();
                n.setId_notification(rs.getInt("id_notification"));
                n.setUser_id(rs.getInt("user_id"));
                n.setUser_name(rs.getString("user_name"));
                n.setAdmin_id(rs.getInt("admin_id"));
                n.setAdmin_name(rs.getString("admin_name"));
                n.setItem_type(rs.getString("item_type"));
                n.setItem_id(rs.getInt("item_id"));
                n.setItem_name(rs.getString("item_name"));
                n.setReason(rs.getString("reason"));
                n.setCustom_reason(rs.getString("custom_reason"));
                n.setDeleted_at(rs.getTimestamp("deleted_at").toLocalDateTime());
                n.setIs_read(rs.getBoolean("is_read"));
                notifications.add(n);
            }
        }
        return notifications;
    }

    public void markAsRead(int notificationId) throws SQLException {
        String req = "UPDATE delete_notifications SET is_read = true WHERE id_notification = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, notificationId);
            pst.executeUpdate();
        }
    }

    public void markAllAsReadForUser(int userId) throws SQLException {
        String req = "UPDATE delete_notifications SET is_read = true WHERE user_id = ? AND is_read = false";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }
}