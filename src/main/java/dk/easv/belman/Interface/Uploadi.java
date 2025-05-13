package dk.easv.belman.Interface;

import dk.easv.belman.BE.UploadEntry;

import java.sql.SQLException;
import java.util.List;

public interface Uploadi {
    List<UploadEntry> getAllUploads() throws SQLException;
    List<UploadEntry> getPendingUploads() throws SQLException;
    List<UploadEntry> getAllOrderSummaries() throws SQLException;
    void updateStatusByOrder(String orderNumber, String status, String approvedBy) throws SQLException;
    void insertUpload(UploadEntry entry) throws SQLException;
}
