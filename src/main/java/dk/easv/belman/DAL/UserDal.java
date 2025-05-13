package dk.easv.belman.DAL;

import dk.easv.belman.BE.User;
import dk.easv.belman.Interface.Useri;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDal implements Useri {

    public User addUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new User(id, user.getUsername(), user.getPassword(), user.getRole());
                }
            }
        }
        throw new SQLException("Failed to insert user or retrieve ID.");
    }

    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public void deleteUser(User user) throws SQLException {
        String sql = "DELETE FROM Users WHERE id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> allUsers = new ArrayList<>();
        String sql = "SELECT id, username, password, role FROM Users";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");

                allUsers.add(new User(id, username, password, role));
            }
        }

        return allUsers;
    }

    public User getByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"), //hashing
                        rs.getString("role")
                );
            }
        }
        return null;
    }

}
