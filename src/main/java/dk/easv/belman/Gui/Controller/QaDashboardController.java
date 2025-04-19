package dk.easv.belman.Gui.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class QaDashboardController {

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

    public void handleApprove(ActionEvent actionEvent) {
    }

    public void handleReject(ActionEvent actionEvent) {
    }

    public void handlePreviewReport(ActionEvent actionEvent) {
    }

    public void handleSendEmail(ActionEvent actionEvent) {
    }
}
