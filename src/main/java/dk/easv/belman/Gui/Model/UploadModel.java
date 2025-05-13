package dk.easv.belman.Gui.Model;

import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.BLL.UploadBLL;
import dk.easv.belman.DAL.UploadDAL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UploadModel {


    private static final UploadModel instance = new UploadModel();
    UploadBLL uploadBLL = new UploadBLL(new UploadDAL());

    private final List<String> imagePaths = new ArrayList<>();

    private final ObservableList<UploadEntry> pendingUploads  = FXCollections.observableArrayList();

    private final ObservableList<UploadEntry> allUploads = FXCollections.observableArrayList();

    private UploadModel() {}

    public static UploadModel getInstance() {
        return instance;
    }

    public ObservableList<UploadEntry> getPendingUploads() {
        return pendingUploads ;
    }
    private final Map<String, Image> iconCache = new HashMap<>();

    public void loadPendingUploads() {
        try {
            List<UploadEntry> list = uploadBLL.getPendingUploads();
            pendingUploads.setAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadAllUploads() {
        try {
            List<UploadEntry> list = uploadBLL.getAllUploads();
            allUploads.setAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<UploadEntry> getAllUploads() {
        return allUploads;
    }

    public void updateApprovalStatusByOrder(String orderNumber, String status, String approvedBy) throws SQLException {
        uploadBLL.setUploadStatusByOrder(orderNumber, status, approvedBy);
        loadPendingUploads();
    }
    public void loadAllOrderSummaries() {
        try {
            List<UploadEntry> list = uploadBLL.getAllOrderSummaries();
            allUploads.setAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private Node createIcon(String path, Runnable onClick, String tooltipText) {
        Image image = iconCache.computeIfAbsent(path, p -> {
            InputStream is = getClass().getResourceAsStream(p);
            if (is == null) {
                System.err.println("Missing icon resource: " + p);
                return new Image(getClass().getResourceAsStream("/dk/easv/belman/Icon/fallback.png")); // optional fallback
            }
            return new Image(is);
        });

        ImageView icon = new ImageView(image);
        icon.setFitWidth(36);
        icon.setFitHeight(36);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);

        StackPane wrapper = new StackPane(icon);
        wrapper.setPadding(new Insets(6));
        wrapper.setCursor(Cursor.HAND);
        wrapper.getStyleClass().add("icon-button");
        wrapper.setOnMouseClicked(e -> onClick.run());

        Tooltip.install(wrapper, new Tooltip(tooltipText));
        return wrapper;
    }


    public ObservableList<HBox> getPendingOrderBoxes(
            Consumer<String> onView,
            Consumer<String> onPreview,
            Consumer<String> onApprove,
            Consumer<String> onReject,
            Consumer<String> onEmail
    ) {
        ObservableList<HBox> boxes = FXCollections.observableArrayList();

        List<String> orderNumbers = pendingUploads.stream()
                .map(UploadEntry::getOrderNumber)
                .distinct()
                .collect(Collectors.toList());

        for (String orderNumber : orderNumbers) {
            Label orderLabel = new Label(orderNumber);
            orderLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            HBox iconBox = new HBox(16,
                    createIcon("/dk/easv/belman/Icon/ImageIcon.png", () -> onView.accept(orderNumber), "View Images"),
                    createIcon("/dk/easv/belman/Icon/MagGlass2.png", () -> onPreview.accept(orderNumber), "Preview Report"),
                    createIcon("/dk/easv/belman/Icon/greenCheckMark.png", () -> onApprove.accept(orderNumber), "Approve"),
                    createIcon("/dk/easv/belman/Icon/RedCrossMark.png", () -> onReject.accept(orderNumber), "Reject"),
                    createIcon("/dk/easv/belman/Icon/sendEmail2.png", () -> onEmail.accept(orderNumber), "Send Email")
            );

            iconBox.setAlignment(Pos.CENTER_RIGHT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(10, orderLabel, spacer, iconBox);
            row.setAlignment(Pos.CENTER_LEFT);
            boxes.add(row);
        }

        return boxes;
    }
}
