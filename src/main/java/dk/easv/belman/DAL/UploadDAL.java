package dk.easv.belman.DAL;

import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.Interface.Uploadi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UploadDAL implements Uploadi {
    public List<UploadEntry> getPendingUploads() throws SQLException {
        List<UploadEntry> entries = new ArrayList<>();
        String sql = """
                SELECT * FROM Uploads
                WHERE status = 'pending'
                ORDER BY upload_date DESC
                """;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UploadEntry entry = new UploadEntry(
                        rs.getInt("id"),
                        rs.getString("order_number"),
                        rs.getString("image_path"),
                        rs.getString("status"),
                        rs.getString("uploaded_by"),
                        rs.getString("upload_date"),
                        rs.getString("approved_by"),
                        rs.getString("approval_date")
                );
                entries.add(entry);
            }
        }

        return entries;
    }

    public List<UploadEntry> getAllUploads() throws SQLException {
        List<UploadEntry> uploads = new ArrayList<>();
        String sql = "SELECT * FROM Uploads";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UploadEntry entry = new UploadEntry(
                        rs.getInt("id"),
                        rs.getString("order_number"),
                        rs.getString("image_path"),
                        rs.getString("status"),
                        rs.getString("uploaded_by"),
                        rs.getString("upload_date"),
                        rs.getString("approved_by"),
                        rs.getString("approval_date")
                );
                uploads.add(entry);
            }
        }
        return uploads;
    }

    public void updateStatusByOrder(String orderNumber, String status, String approvedBy) throws SQLException {
        String sql = "UPDATE Uploads SET status = ?, approved_by = ? WHERE order_number = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, approvedBy);
            stmt.setString(3, orderNumber);
            stmt.executeUpdate();
        }
    }

    public void insertUpload(UploadEntry entry) throws SQLException {
        String sql = "INSERT INTO Uploads (order_number, image_path, status, uploaded_by, upload_date) VALUES (?, ?, ?, ?, GETDATE())";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getOrderNumber());
            ps.setString(2, entry.getImagePath());
            ps.setString(3, entry.getStatus());
            ps.setString(4, entry.getUploadedBy());
            ps.executeUpdate();
        }
    }

    public List<UploadEntry> getAllOrderSummaries() throws SQLException {
        List<UploadEntry> uploads = new ArrayList<>();
        String sql = """
        SELECT 
            order_number,
            MAX(upload_date) AS upload_date,
            MAX(uploaded_by) AS uploaded_by,
            MAX(status) AS status,
            MAX(approved_by) AS approved_by,
            MAX(approval_date) AS approval_date
        FROM Uploads
        WHERE status IN ('approved', 'rejected')
        GROUP BY order_number
        ORDER BY MAX(upload_date) DESC
    """;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                uploads.add(new UploadEntry(
                        0,
                        rs.getString("order_number"),
                        null,
                        rs.getString("status"),
                        rs.getString("uploaded_by"),
                        rs.getString("upload_date"),
                        rs.getString("approved_by"),
                        rs.getString("approval_date")
                ));
            }
        }

        return uploads;
    }
}
