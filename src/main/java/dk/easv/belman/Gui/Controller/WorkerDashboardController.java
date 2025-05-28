package dk.easv.belman.Gui.Controller;

import dk.easv.belman.BE.ImageUploadWrapper;
import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.BE.User;
import dk.easv.belman.Gui.Model.UploadModel;
import dk.easv.belman.Utility.ModelException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WorkerDashboardController {

    @FXML private TextField orderNumberField;
    @FXML private ListView<VBox> imageListView;
    @FXML private Label currentUserLabel;

    private final UploadModel uploadModel = UploadModel.getInstance();
    private User currentUser;

    public void setCurrentUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null.");
        this.currentUser = user;
        currentUserLabel.setText("Logged in as: " + user.getUsername());
    }

    public void handleLogout(ActionEvent actionEvent) {
        loadScene("LoginView.fxml", actionEvent);
    }

    private void loadScene(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            throw new ModelException("Failed to load scene: " + fxmlFile, e);
        }
    }

    public void handleUploadImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString(), 200, 150, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);

            ComboBox<String> angleBox = new ComboBox<>();
            angleBox.setItems(FXCollections.observableArrayList(
                    "Front view", "Back view", "Top view", "Close-up",
                    "Broken part", "Scratched surface", "Packaging damaged",
                    "Missing component", "Wrong item received", "Label or serial number photo",
                    "Fully assembled"
            ));
            angleBox.setValue("Front view");

            ImageUploadWrapper wrapper = new ImageUploadWrapper(selectedFile, "Front view");
            angleBox.valueProperty().addListener((obs, oldVal, newVal) -> wrapper.setAngle(newVal));

            VBox vbox = new VBox(5, imageView, angleBox);
            imageListView.getItems().add(vbox);

            uploadModel.addImageWrapper(wrapper);
        }
    }

    public void handleSubmitImages(ActionEvent actionEvent) {
        String orderNumber = orderNumberField.getText();
        if (orderNumber == null || orderNumber.isBlank()) {
            showAlert("Please enter an order number.");
            return;
        }

        try {
            List<UploadEntry> submitted = uploadModel.submitImages(orderNumber, currentUser.getUsername());

            if (!submitted.isEmpty()) {
                imageListView.getItems().clear();
                orderNumberField.clear();
                uploadModel.loadPendingUploads();
                showAlert("Images submitted successfully!");
            }
        } catch (ModelException e) {
            showAlert("Failed to submit images: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(msg);
        alert.setHeaderText(null);
        try {
            Stage stage = (Stage) orderNumberField.getScene().getWindow();
            alert.initOwner(stage);
        } catch (Exception ignored) {}
        alert.show();
    }

    @FXML
    private void handleOpenCamera(ActionEvent event) {
        try {
            Runtime.getRuntime().exec("cmd.exe /c start microsoft.windows.camera:");
        } catch (IOException e) {
            showAlert("Camera function only supported on Windows tablets.");
        }
    }
}
