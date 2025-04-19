package dk.easv.belman.Gui.Controller;

import dk.easv.belman.BE.User;
import dk.easv.belman.Gui.Model.UserModel;
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
import java.util.Optional;

public class AdminDashboardController {

    @FXML private TextField UsernameTxt;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;

    private final UserModel usermodel = UserModel.getInstance();

    @FXML
    private void initialize() {
        roleComboBox.getItems().addAll("Admin", "Quality Assurance", "Production Worker");
        UsernameTxt.setPromptText("Username");
        newPasswordField.setPromptText("Password");

        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        usermodel.loadUsersFromDatabase();
        userTableView.setItems(usermodel.getUsers());
    }


    public void handleLogout(ActionEvent actionEvent) {
        loadScene("LoginView.fxml", actionEvent);
    }

    private void loadScene(String fxmlFile, ActionEvent event) {
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

    @FXML
    private void handleAddUser() {
        String username = UsernameTxt.getText().trim();
        String password = newPasswordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert("All fields must be filled.");
            return;
        }

        try {
            if (usermodel.userExists(username)) {
                showAlert("Username already exists.");
                return;
            }

            User newUser = new User(-1, username, password, role);
            usermodel.addUser(newUser);

            showAlert("User added successfully!");
            UsernameTxt.clear();
            newPasswordField.clear();
            roleComboBox.getSelectionModel();


        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    @FXML
    public void handleDeleteUser(ActionEvent event) {
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert("No user selected.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedUser.getUsername() + "?",
                ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                usermodel.deleteUser(selectedUser);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Failed to delete user: " + e.getMessage());
            }
        }
    }


    public void handleGeneratePdf(ActionEvent actionEvent) {
    }



}
