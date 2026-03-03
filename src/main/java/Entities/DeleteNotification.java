package Entities;

import java.time.LocalDateTime;

public class DeleteNotification {
    private int id_notification;
    private int user_id; // User who will receive the notification (the one who added the item)
    private String user_name; // Name of the user who added the item
    private int admin_id; // Admin who deleted the item
    private String admin_name; // Name of admin who deleted
    private String item_type; // "Destination" or "Hebergement"
    private int item_id; // ID of deleted item
    private String item_name; // Name of deleted item
    private String reason; // Why it was deleted
    private String custom_reason; // Custom reason if provided
    private LocalDateTime deleted_at; // When it was deleted
    private boolean is_read; // Whether the user has seen the notification

    public DeleteNotification() {
        this.deleted_at = LocalDateTime.now();
        this.is_read = false;
    }

    // Getters and Setters
    public int getId_notification() { return id_notification; }
    public void setId_notification(int id_notification) { this.id_notification = id_notification; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public String getUser_name() { return user_name; }
    public void setUser_name(String user_name) { this.user_name = user_name; }

    public int getAdmin_id() { return admin_id; }
    public void setAdmin_id(int admin_id) { this.admin_id = admin_id; }

    public String getAdmin_name() { return admin_name; }
    public void setAdmin_name(String admin_name) { this.admin_name = admin_name; }

    public String getItem_type() { return item_type; }
    public void setItem_type(String item_type) { this.item_type = item_type; }

    public int getItem_id() { return item_id; }
    public void setItem_id(int item_id) { this.item_id = item_id; }

    public String getItem_name() { return item_name; }
    public void setItem_name(String item_name) { this.item_name = item_name; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getCustom_reason() { return custom_reason; }
    public void setCustom_reason(String custom_reason) { this.custom_reason = custom_reason; }

    public LocalDateTime getDeleted_at() { return deleted_at; }
    public void setDeleted_at(LocalDateTime deleted_at) { this.deleted_at = deleted_at; }

    public boolean isIs_read() { return is_read; }
    public void setIs_read(boolean is_read) { this.is_read = is_read; }

    public String getFullReason() {
        if (reason != null && !reason.isEmpty() && reason.equals("Autre")) {
            return custom_reason != null ? custom_reason : "Raison non spécifiée";
        }
        return reason != null ? reason : "Raison non spécifiée";
    }
}