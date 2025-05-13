package dk.easv.belman.BLL;

import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.Interface.Uploadi;
import java.sql.SQLException;
import java.util.List;

public class UploadBLL {
    private final Uploadi uploadDAL;

    public UploadBLL(Uploadi uploadDAL) {
        this.uploadDAL = uploadDAL;
    }

    public List<UploadEntry> getPendingUploads() throws SQLException {
        return uploadDAL.getPendingUploads();
    }

    public void setUploadStatusByOrder(String orderNumber, String status, String approvedBy) throws SQLException {
        uploadDAL.updateStatusByOrder(orderNumber, status, approvedBy);
    }
    public List<UploadEntry> getAllOrderSummaries() throws SQLException {
        return uploadDAL.getAllOrderSummaries();
    }

    public void saveUpload(UploadEntry entry) throws SQLException {
        uploadDAL.insertUpload(entry);
    }

    public List<UploadEntry> getAllUploads() throws SQLException {
        return uploadDAL.getAllUploads();
    }
}
