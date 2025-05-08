package dk.easv.belman.BLL;

import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.DAL.UploadDAL;

import java.sql.SQLException;
import java.util.List;

public class UploadBLL {
    private final UploadDAL uploadDAL = new UploadDAL();

    public List<UploadEntry> getPendingUploads() throws SQLException {
        return uploadDAL.getPendingUploads();
    }

    public void setUploadDALStatus (int uploadId, String status, String approvedBy) throws SQLException {
        uploadDAL.updateApprovalStatus(uploadId, status, approvedBy);
    }
    public void saveUpload(UploadEntry entry) throws SQLException {
        uploadDAL.insertUpload(entry);
    }

    public List<UploadEntry> getAllUploads() throws SQLException {
        return uploadDAL.getAllUploads();
    }
}
