package dk.easv.belman.DAL;

import dk.easv.belman.BE.UploadEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UploadDAL {
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

    public void updateApprovalStatus(int uploadId, String status, String approvedBy) throws SQLException {
        String sql = """
        UPDATE Uploads
        SET status = ?, approved_by = ?, approval_date = GETDATE()
        WHERE id = ?
    """;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, approvedBy);
            stmt.setInt(3, uploadId);
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
}
