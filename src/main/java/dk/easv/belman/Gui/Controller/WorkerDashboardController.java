package dk.easv.belman.Gui.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WorkerDashboardController {
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

    public void handleAddImage(ActionEvent actionEvent) {
    }

    public void handleSubmit(ActionEvent actionEvent) {
    }

    public void handleUploadImage(ActionEvent actionEvent) {
    }

    public void handleSubmitImages(ActionEvent actionEvent) {

    }
}
