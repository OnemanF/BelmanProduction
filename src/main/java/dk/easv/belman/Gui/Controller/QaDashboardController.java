package dk.easv.belman.Gui.Controller;

import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.BE.User;
import dk.easv.belman.Gui.Model.UploadModel;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.stage.Window;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class QaDashboardController {

    @FXML private Label currentUserLabel;
    @FXML private ListView<HBox> pendingOrdersList;
    @FXML private TableView<UploadEntry> uploadTable;
    @FXML private TableColumn<UploadEntry, String> orderNumberCol;
    @FXML private TableColumn<UploadEntry, String> uploadedByCol;
    @FXML private TableColumn<UploadEntry, String> statusCol;
    @FXML private TextField pendingSearchField;
    @FXML private TextField allOrdersSearchField;

    private FilteredList<UploadEntry> filteredReviewedUploads;

    private User currentUser;
    private final UploadModel uploadModel = UploadModel.getInstance();


    @FXML
    private void initialize() {
        uploadModel.loadPendingUploads();
        uploadModel.loadAllUploads();

        setupUploadTable();
        loadPendingOrderNumbers();
        loadOrders();
    }

    private void loadPendingOrderNumbers() {
        List<String> allOrders = uploadModel.getPendingUploads().stream()
                .map(UploadEntry::getOrderNumber)
                .distinct()
                .collect(Collectors.toList());

        pendingSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<String> filtered = allOrders.stream()
                    .filter(order -> order.toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());

            updatePendingOrderList(filtered);
        });

        updatePendingOrderList(allOrders);
    }

    private void updatePendingOrderList(List<String> orderNumbers) {
        ObservableList<HBox> boxes = uploadModel.getPendingOrderBoxes(
                this::handleViewImages,
                this::handlePreviewReport,
                this::handleApprove,
                this::handleReject,
                this::handleSendEmail
        ).filtered(box -> {
            Label label = (Label) box.getChildren().get(0);
            return orderNumbers.contains(label.getText());
        });

        pendingOrdersList.setItems(boxes);
    }

    private void setupUploadTable() {
        orderNumberCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderNumber"));
        uploadedByCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("uploadedBy"));
        statusCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
    }

    private void loadOrders() {
        filteredReviewedUploads = new FilteredList<>(
                uploadModel.getAllUploads(),
                upload -> "approved".equalsIgnoreCase(upload.getStatus()) || "rejected".equalsIgnoreCase(upload.getStatus())
        );

        allOrdersSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal.toLowerCase();
            filteredReviewedUploads.setPredicate(upload ->
                    ("approved".equalsIgnoreCase(upload.getStatus()) || "rejected".equalsIgnoreCase(upload.getStatus())) &&
                            (upload.getOrderNumber().toLowerCase().contains(filter) ||
                                    upload.getUploadedBy().toLowerCase().contains(filter) ||
                                    upload.getStatus().toLowerCase().contains(filter))
            );
        });

        SortedList<UploadEntry> sorted = new SortedList<>(filteredReviewedUploads);
        sorted.comparatorProperty().bind(uploadTable.comparatorProperty());
        uploadTable.setItems(sorted);
    }

    public void handleLogout(ActionEvent actionEvent) {
        SceneLoader("LoginView.fxml", actionEvent);
    }

    private void SceneLoader(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/" + fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleViewImages(String orderNumber) {
        List<UploadEntry> imagesForOrder = uploadModel.getPendingUploads().stream()
                .filter(upload -> orderNumber.equals(upload.getOrderNumber()))
                .collect(Collectors.toList());

        if (imagesForOrder.isEmpty()) {
            showAlert("No images found for this order.");
            return;
        }

        VBox imagesBox = new VBox(20);
        imagesBox.setAlignment(Pos.CENTER);
        imagesBox.setPadding(new Insets(20));

        for (UploadEntry upload : imagesForOrder) {
            try {
                ImageView imageView = new ImageView(new Image(new File(upload.getImagePath()).toURI().toString()));
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(600);
                addZoomCapability(imageView);
                imagesBox.getChildren().add(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> ((Stage) closeButton.getScene().getWindow()).close());

        VBox root = new VBox(10, closeButton, imagesBox);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        Stage stage = new Stage();
        stage.setTitle("Images for Order: " + orderNumber);
        stage.setScene(new Scene(scrollPane, 800, 600));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();

        stage.setOnHidden(e -> {
            Stage mainStage = (Stage) pendingOrdersList.getScene().getWindow();
            mainStage.setFullScreen(true);
        });
    }

    private void handlePreviewReport(String orderNumber) {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(80, 80);

        Stage loadingStage = new Stage();
        VBox loadingRoot = new VBox(spinner);
        loadingRoot.setAlignment(Pos.CENTER);
        loadingStage.setScene(new Scene(loadingRoot, 200, 200));
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Loading Report...");
        loadingStage.show();

        Task<Void> loadTask = new Task<>() {
            private Parent root;
            private ReportPreviewController controller;

            @Override
            protected Void call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/ReportPreview.fxml"));
                root = loader.load();
                controller = loader.getController();
                controller.setOrderNumber(orderNumber);
                controller.loadReportData(orderNumber);
                return null;
            }

            @Override
            protected void succeeded() {
                loadingStage.close();
                Stage previewStage = new Stage();
                previewStage.setTitle("Report Preview - " + orderNumber);
                previewStage.setScene(new Scene(root));
                previewStage.initModality(Modality.APPLICATION_MODAL);
                previewStage.setFullScreen(true);
                previewStage.setFullScreenExitHint("");
                previewStage.showAndWait();
            }

            @Override
            protected void failed() {
                loadingStage.close();
                getException().printStackTrace();
                showAlert("Failed to load report: " + getException().getMessage());
            }
        };

        new Thread(loadTask).start();
    }

    private void handleApprove(String orderNumber) {
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            showAccessDenied("Admins are not allowed to approve QA orders.");
            return;
        }

        try {
            uploadModel.updateApprovalStatusByOrder(orderNumber, "approved", currentUser.getUsername());
            showAlert("Approved all Images for order: " + orderNumber);

            uploadModel.loadPendingUploads();
            uploadModel.loadAllOrderSummaries();
            loadOrders();
            loadPendingOrderNumbers();

        } catch (SQLException e) {
            showAlert("Error approving uploads: " + e.getMessage());
        }
    }

    private void handleReject(String orderNumber) {
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            showAccessDenied("Admins are not allowed to reject QA orders.");
            return;
        }
        try {
            uploadModel.updateApprovalStatusByOrder(orderNumber, "rejected", currentUser.getUsername());

            showAlert("Rejected all Images for order: " + orderNumber);

            uploadModel.loadPendingUploads();
            uploadModel.loadAllOrderSummaries();
            loadOrders();
            loadPendingOrderNumbers();

        } catch (SQLException e) {
            showAlert("Error rejecting uploads: " + e.getMessage());
        }
    }

    private void showAccessDenied(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Find a safe fallback window
        Window owner = null;
        try {
            owner = pendingOrdersList.getScene().getWindow();
        } catch (Exception ignored) {}

        if (owner != null)
            alert.initOwner(owner);

        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }


    private void handleSendEmail(String orderNumber) {
        try {
            String subject = "Order Review for: " + orderNumber;
            String body = "The order " + orderNumber + " has been reviewed. Please check Belsign for details.";

            String gmailUrl = String.format(
                    "https://mail.google.com/mail/?view=cm&fs=1&to=support@belman.dk&su=%s&body=%s",
                    java.net.URLEncoder.encode(subject, "UTF-8"),
                    java.net.URLEncoder.encode(body, "UTF-8")
            );

            Desktop.getDesktop().browse(new URI(gmailUrl));

        } catch (Exception e) {
            showAlert("Failed to open Gmail: " + e.getMessage());
        }
    }

    private void addZoomCapability(ImageView imageview) {
        imageview.setOnScroll((ScrollEvent event) -> {
            if (event.getDeltaY() == 0) return;
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            imageview.setFitWidth(imageview.getFitWidth() * zoomFactor);
            event.consume();
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        currentUserLabel.setText("Logged in as: " + user.getUsername());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(currentUserLabel.getScene().getWindow());
        alert.initModality(Modality.WINDOW_MODAL);
        alert.show();
    }

}
