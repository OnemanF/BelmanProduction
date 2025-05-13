package dk.easv.belman.Gui.Controller;

import dk.easv.belman.BE.User;
import dk.easv.belman.Gui.Model.UserModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    private final UserModel userModel = UserModel.getInstance();

    @FXML
    private void initialize() {
        txtPassword.setOnAction(e -> handleLogin(new ActionEvent()));
    }

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
                Platform.runLater(() -> switchToDashboard(user, actionEvent));
            } else {
                showError("Invalid credentials. Try again.");
            }
        });

        loginTask.setOnFailed(event -> showError("Login error."));

        new Thread(loginTask).start();
    }

    private void switchToDashboard(User user, ActionEvent event) {
        String fxmlFile = switch (user.getRole().toLowerCase()) {
            case "admin" -> "AdminDashboard.fxml";
            case "quality assurance" -> "QaDashboard.fxml";
            case "production worker" -> "WorkerDashboard.fxml";
            default -> null;
        };

        if (fxmlFile == null) {
            showError("Unknown role: " + user.getRole());
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/" + fxmlFile));
            Parent root = loader.load();

            //Pass user to the loaded controller
            Object controller = loader.getController();
            if (controller instanceof QaDashboardController qaCtrl) {
                qaCtrl.setCurrentUser(user);
            } else if (controller instanceof AdminDashboardController adminCtrl) {
                adminCtrl.setCurrentUser(user);
            } else if (controller instanceof WorkerDashboardController workerCtrl) {
                workerCtrl.setCurrentUser(user);
            }

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();

            Stage currentStage = (Stage) txtUsername.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, message).show());
    }
}
