package dk.easv.belman.Gui.Model;

import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.BLL.UploadBLL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UploadModel {
    private static final UploadModel instance = new UploadModel();
    private final UploadBLL uploadBLL = new UploadBLL();

    private final List<String> imagePaths = new ArrayList<>();

    private final ObservableList<UploadEntry> pendingUploads  = FXCollections.observableArrayList();

    private UploadModel() {}

    public static UploadModel getInstance() {
        return instance;
    }

    public ObservableList<UploadEntry> getPendingUploads() {
        return pendingUploads ;
    }

    public void loadPendingUploads() {
        try {
            List<UploadEntry> list = uploadBLL.getPendingUploads();
            pendingUploads.setAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateApprovalStatus(int uploadId, String status, String approvedBy) throws SQLException {
        uploadBLL.setUploadDALStatus(uploadId, status, approvedBy);
        loadPendingUploads();
    }

    public void addImagePath(String path){
        imagePaths.add(path);
    }

    public List<String> getImagePaths() {
        return new ArrayList<>(imagePaths);
    }

    public void clearImagePaths() {
        imagePaths.clear();
    }

    public List<UploadEntry> submitImages(String orderNumber, String uploadedBy) {
        List<UploadEntry> entries = new ArrayList<>();
        for (String path : imagePaths) {
            UploadEntry entry = new UploadEntry(
                    0, orderNumber, path, "pending", uploadedBy,
                    LocalDate.now().toString(), null, null
            );
            try {
                uploadBLL.saveUpload(entry);
                entries.add(entry);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        clearImagePaths();
        return entries;
    }


}
