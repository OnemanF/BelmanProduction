package dk.easv.belman.Gui.Controller;

import dk.easv.belman.BE.User;
import dk.easv.belman.BLL.UserBLL;
import dk.easv.belman.Gui.Model.UserModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    private final UserModel userModel = UserModel.getInstance();

    public void handleLogin(ActionEvent actionEvent) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return userModel.getAuthenticatedUser(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            if (user != null) {
                Platform.runLater(() -> switchToDashboard(user.getRole(), actionEvent));
            } else {
                showError("Invalid credentials. Try again.");
            }
        });

        loginTask.setOnFailed(event -> showError("Login error."));

        new Thread(loginTask).start();
    }

    private void switchToDashboard(String role, ActionEvent event) {
        String fxmlFile = switch (role.toLowerCase()) {
            case "admin" -> "AdminDashboard.fxml";
            case "qa" -> "QaDashboard.fxml";
            case "worker" -> "WorkerDashboard.fxml";
            default -> null;
        };

        if (fxmlFile == null) {
            showError("Unknown role.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint(""); // Optional: hide exit hint
            stage.show();

            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load dashboard.");
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, message).show());
    }
}
