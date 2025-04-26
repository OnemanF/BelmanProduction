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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class QaDashboardController {

    @FXML private Label currentUserLabel;
    @FXML private ListView<UploadEntry> pendingOrdersList;
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
        pendingOrdersList.setItems(uploadModel.getPendingUploads());

        setupOrderTable();
        loadOrders();
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
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        currentUserLabel.setText("Logged in as: " + user.getUsername());
    }
    @FXML
    private void handleApprove(ActionEvent event) {
        UploadEntry selected = pendingOrdersList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a Order Number to approve.");
            return;
        }

        try {
            String approver = currentUser.getUsername();
            uploadModel.updateApprovalStatus(selected.getId(), "approved", approver);

            showAlert("Order approved by: " + approver);

            // Refresh list
            uploadModel.loadPendingUploads();
            pendingOrdersList.setItems(uploadModel.getPendingUploads());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error approving order: " + e.getMessage());
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
    }

    public void handlePreviewReport(ActionEvent actionEvent) {
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
