package dk.easv.belman.Gui.Controller;

import dk.easv.belman.BE.Order;
import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.BE.User;
import dk.easv.belman.Gui.Model.OrderModel;
import dk.easv.belman.Gui.Model.UploadModel;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class QaDashboardController {

    @FXML private Label currentUserLabel;
    @FXML private ListView<String> pendingOrdersList;
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> orderNumberCol;
    @FXML private TableColumn<Order, String> customerCol;
    @FXML private TableColumn<Order, String> statusCol;
    @FXML private TextField orderNumberField;
    @FXML private TextField customerField;

    private User currentUser;
    private final UploadModel uploadModel = UploadModel.getInstance();
    private final OrderModel orderModel = new OrderModel();

    @FXML
    private void initialize() {
        uploadModel.loadPendingUploads();
        loadPendingOrderNumbers();

        setupOrderTable();
        loadOrders();
    }

    private void loadPendingOrderNumbers() {
        List<String> uniqueOrderNumbers = uploadModel.getPendingUploads().stream()
                .map(UploadEntry::getOrderNumber)
                .distinct()
                .collect(Collectors.toList());

        pendingOrdersList.setItems(FXCollections.observableArrayList(uniqueOrderNumbers));
    }

    private void setupOrderTable() {
        orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadOrders() {
        orderModel.loadOrders();
        orderTable.setItems(orderModel.getOrders());
    }

    public void handleLogout(ActionEvent actionEvent) {
        SceneLoader("LoginView.fxml", actionEvent);
    }

    private void SceneLoader(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/" + fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleViewImages(ActionEvent actionEvent) {
        String selectedOrderNumber = pendingOrdersList.getSelectionModel().getSelectedItem();
        if (selectedOrderNumber == null || selectedOrderNumber.isBlank()) {
            showAlert("Please select an order number to view images.");
            return;
        }

        List<UploadEntry> imagesForOrder = uploadModel.getPendingUploads().stream()
                .filter(upload -> selectedOrderNumber.equals(upload.getOrderNumber()))
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

        ScrollPane scrollPane = new ScrollPane(imagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        Scene scene = new Scene(scrollPane, 800, 600);

        Stage stage = new Stage();
        stage.setTitle("Images for Order: " + selectedOrderNumber);
        stage.setScene(scene);
        stage.show();
    }

    private void addZoomCapability(ImageView imageview) {
        imageview.setOnScroll(event -> {
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
    @FXML
    private void handleApprove(ActionEvent event) {
        String selectedOrderNumber = pendingOrdersList.getSelectionModel().getSelectedItem();
        if (selectedOrderNumber == null || selectedOrderNumber.isBlank()) {
            showAlert("Please select an order number to approve.");
            return;
        }

        try {
            String approver = currentUser.getUsername();

            List<UploadEntry> entriesToApprove = uploadModel.getPendingUploads().stream()
                    .filter(upload -> selectedOrderNumber.equals(upload.getOrderNumber()))
                    .collect(Collectors.toList());

            if (entriesToApprove.isEmpty()) {
                showAlert("No uploads found for selected order.");
                return;
            }

            for (UploadEntry entry : entriesToApprove) {
                uploadModel.updateApprovalStatus(entry.getId(), "approved", approver);
            }

            showAlert("Approved all uploads for order: " + selectedOrderNumber);

            uploadModel.loadPendingUploads();
            loadPendingOrderNumbers();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error approving uploads: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleReject(ActionEvent actionEvent) {
        String selectedOrderNumber = pendingOrdersList.getSelectionModel().getSelectedItem();
        if (selectedOrderNumber == null || selectedOrderNumber.isBlank()) {
            showAlert("Please select an order number to reject.");
            return;
        }

        try {
            String approver = currentUser.getUsername();

            List<UploadEntry> entriesToReject = uploadModel.getPendingUploads().stream()
                    .filter(upload -> selectedOrderNumber.equals(upload.getOrderNumber()))
                    .collect(Collectors.toList());

            if (entriesToReject.isEmpty()) {
                showAlert("No uploads found for selected order.");
                return;
            }

            for (UploadEntry entry : entriesToReject) {
                uploadModel.updateApprovalStatus(entry.getId(), "rejected", approver);
            }

            showAlert("Rejected all uploads for order: " + selectedOrderNumber);

            uploadModel.loadPendingUploads();
            loadPendingOrderNumbers();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error rejecting uploads: " + e.getMessage());
        }
    }

    public void handlePreviewReport(ActionEvent actionEvent) {
        String selectedOrderNumber = pendingOrdersList.getSelectionModel().getSelectedItem();
        if (selectedOrderNumber == null || selectedOrderNumber.isBlank()) {
            showAlert("Please select an order number to preview the report.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/ReportPreview.fxml"));
            Parent root = loader.load();

            // Get the controller and pass data
            ReportPreviewController controller = loader.getController();
            controller.setOrderNumber(selectedOrderNumber);
            controller.loadReportData(selectedOrderNumber);

            Stage stage = new Stage();
            stage.setTitle("Report Preview - Order: " + selectedOrderNumber);
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading report preview: " + e.getMessage());
        }

    }

    public void handleSendEmail(ActionEvent actionEvent) {
    }

    @FXML
    public void handleAddOrder(ActionEvent event) {
        String orderNumber = orderNumberField.getText();
        String customerName = customerField.getText();

        if (orderNumber == null || orderNumber.isBlank()) {
            showAlert("Order Number is required.");
            return;
        }

        try {
            Order entry = new Order(0, orderNumber, customerName, null, "open");
            orderModel.addOrder(entry);
            orderNumberField.clear();
            customerField.clear();
            loadOrders();
        } catch (SQLException e) {
            showAlert("Error adding order: " + e.getMessage());
        }
    }
}
