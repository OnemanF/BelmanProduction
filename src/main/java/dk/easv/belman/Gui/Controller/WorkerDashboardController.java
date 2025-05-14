package dk.easv.belman.Gui.Controller;

import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.BE.User;
import dk.easv.belman.Gui.Model.UploadModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WorkerDashboardController {

    @FXML private TextField orderNumberField;
    @FXML private ListView<ImageView> imageListView;
    @FXML private Label currentUserLabel;
    @FXML private ComboBox<String> imageTypeComboBox;

    private final UploadModel uploadModel = UploadModel.getInstance();
    private User currentUser;

    public void setCurrentUser(User user) {
        try {
            if (user == null) throw new IllegalArgumentException("User cannot be null.");
            this.currentUser = user;
            currentUserLabel.setText("Logged in as: " + user.getUsername());
        } catch (Exception e) {
            showAlert("Failed to set current user: " + e.getMessage());
        }
    }

    public void handleLogout(ActionEvent actionEvent) {
        LoadSceneLogin("LoginView.fxml", actionEvent);
    }

    private void LoadSceneLogin(String fxmlFile, ActionEvent event) {
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

    public void handleUploadImage(ActionEvent actionEvent) {
        try {
        String selectedType = imageTypeComboBox.getValue();
        if (selectedType == null || selectedType.isBlank()) {
            showAlert("Please select an image type before uploading.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString(), 200, 150, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);

            imageListView.getItems().add(imageView);

            uploadModel.addImagePath(selectedFile.getAbsolutePath());
        }
        } catch (Exception e) {
            showAlert("Failed to upload image: " + e.getMessage());
        }
    }

    public void handleSubmitImages(ActionEvent actionEvent) {
        try {
        String orderNumber = orderNumberField.getText();
        if (orderNumber == null || orderNumber.isBlank()) {
            showAlert("Please enter an order number.");
            return;
        }

        if (uploadModel.getImagePaths().isEmpty()) {
            showAlert("Please upload at least one image.");
            return;
        }

        List<UploadEntry> submitted = uploadModel.submitImages(orderNumber, currentUser.getUsername());
        System.out.println("Submitted: " + submitted.size());

        imageListView.getItems().clear();
        orderNumberField.clear();
        uploadModel.loadPendingUploads();
        showAlert("Images submitted successfully!");
        } catch (Exception e) {
            showAlert("Failed to submit images: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        try {
            Stage stage = (Stage) orderNumberField.getScene().getWindow();
            alert.initOwner(stage);
        } catch (Exception ignored) {}

        alert.show();
    }
}
