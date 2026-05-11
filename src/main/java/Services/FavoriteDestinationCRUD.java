package Services;

import Utils.MyBD;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class FavoriteDestinationCRUD {

    private final Connection conn;

    public FavoriteDestinationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    /**
     * Returns all destination IDs that the given user has favourited.
     */
    public Set<Integer> getFavoriteDestinationIds(int userId) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT id_destination FROM favorite_destination WHERE id_user = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_destination"));
                }
            }
        }
        return ids;
    }

    /**
     * Adds a destination to the user's favourites.
     * Silently ignores duplicate inserts (already favourited).
     */
    public void addFavorite(int userId, int destinationId) throws SQLException {
        // Guard: don't insert if already exists
        if (isFavorite(userId, destinationId)) return;

        String sql = "INSERT INTO favorite_destination (created_at, id_destination, id_user) VALUES (NOW(), ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, destinationId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        }
    }

    /**
     * Removes a destination from the user's favourites.
     */
    public void removeFavorite(int userId, int destinationId) throws SQLException {
        String sql = "DELETE FROM favorite_destination WHERE id_user = ? AND id_destination = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setInt(2, destinationId);
            pst.executeUpdate();
        }
    }

    /**
     * Returns true if the user has already favourited this destination.
     */
    public boolean isFavorite(int userId, int destinationId) throws SQLException {
        String sql = "SELECT 1 FROM favorite_destination WHERE id_user = ? AND id_destination = ? LIMIT 1";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setInt(2, destinationId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }
}
