package Services;

import Utils.MyBD;

import java.sql.*;

public class NoteDestinationCRUD {

    private final Connection conn;

    public NoteDestinationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    /**
     * Returns the rating (1-5) the given user gave to this destination,
     * or 0 if the user has not rated it yet.
     */
    public int getUserRating(int userId, int destinationId) throws SQLException {
        String sql = "SELECT note FROM note_destination WHERE id_user = ? AND id_destination = ? LIMIT 1";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setInt(2, destinationId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return (int) Math.round(rs.getDouble("note"));
                }
            }
        }
        return 0;
    }

    /**
     * Inserts or updates the user's rating for a destination.
     * Uses INSERT … ON DUPLICATE KEY UPDATE if a unique key exists,
     * otherwise falls back to delete-then-insert.
     */
    public void saveRating(int userId, int destinationId, int stars) throws SQLException {
        // Remove any existing rating first
        String del = "DELETE FROM note_destination WHERE id_user = ? AND id_destination = ?";
        try (PreparedStatement pst = conn.prepareStatement(del)) {
            pst.setInt(1, userId);
            pst.setInt(2, destinationId);
            pst.executeUpdate();
        }

        // Insert new rating
        String ins = "INSERT INTO note_destination (note, created_at, id_destination, id_user) VALUES (?, NOW(), ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(ins)) {
            pst.setDouble(1, stars);
            pst.setInt(2, destinationId);
            pst.setInt(3, userId);
            pst.executeUpdate();
        }
    }

    /**
     * Recalculates the average of all ratings for a destination
     * and updates score_destination in the destination table.
     */
    public void refreshDestinationScore(int destinationId) throws SQLException {
        String avg = "SELECT AVG(note) AS avg_note FROM note_destination WHERE id_destination = ?";
        try (PreparedStatement pst = conn.prepareStatement(avg)) {
            pst.setInt(1, destinationId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double newScore = rs.getDouble("avg_note");
                    String upd = "UPDATE destination SET score_destination = ? WHERE id_destination = ?";
                    try (PreparedStatement upst = conn.prepareStatement(upd)) {
                        upst.setDouble(1, newScore);
                        upst.setInt(2, destinationId);
                        upst.executeUpdate();
                    }
                }
            }
        }
    }
}
