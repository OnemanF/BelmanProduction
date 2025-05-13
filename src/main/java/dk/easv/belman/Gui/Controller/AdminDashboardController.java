package dk.easv.belman.Gui.Controller;

import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.BE.User;
import dk.easv.belman.Gui.Model.UploadModel;
import dk.easv.belman.Gui.Model.UserModel;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.transformation.SortedList;

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
    @FXML private TextField searchField;
    @FXML private AnchorPane qaTabContent;
    @FXML private AnchorPane prodTabContent;

    @FXML private TableView<UploadEntry> uploadTable;
    @FXML private TableColumn<UploadEntry, String> orderNumberCol;
    @FXML private TableColumn<UploadEntry, String> uploadedByCol;
    @FXML private TableColumn<UploadEntry, String> uploadDateCol;
    @FXML private TableColumn<UploadEntry, String> statusCol;
    @FXML private TableColumn<UploadEntry, String> approvedByCol;
    @FXML private TableColumn<UploadEntry, String> approvalDateCol;
    @FXML private TextField uploadSearchField;


    @FXML private Label currentUserLabel;
    private User currentUser;

    private final UploadModel uploadModel = UploadModel.getInstance();

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

        setupUserSearch();
        loadUploadEntries();
        setupUploadSearch();
    }

    private void loadUploadEntries() {
        uploadModel.loadAllUploads();
        uploadTable.setItems(uploadModel.getAllUploads());

        orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        uploadedByCol.setCellValueFactory(new PropertyValueFactory<>("uploadedBy"));
        uploadDateCol.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        approvedByCol.setCellValueFactory(new PropertyValueFactory<>("approvedBy"));
        approvalDateCol.setCellValueFactory(new PropertyValueFactory<>("approvalDate"));
    }

    private void loadTabContent() {
        try {
            FXMLLoader qaLoader = new FXMLLoader(getClass().getResource("/dk/easv/belman/QaDashboard.fxml"));
            Parent qaRoot = qaLoader.load();
            QaDashboardController qaController = qaLoader.getController();
            qaController.setCurrentUser(currentUser);

            BorderPane qaPane = (BorderPane) qaRoot;
            Node qaCenter = qaPane.getCenter();
            qaTabContent.getChildren().setAll(qaCenter);
            AnchorPane.setTopAnchor(qaCenter, 0.0);
            AnchorPane.setBottomAnchor(qaCenter, 0.0);
            AnchorPane.setLeftAnchor(qaCenter, 0.0);
            AnchorPane.setRightAnchor(qaCenter, 0.0);

            /*
            FXMLLoader prodLoader = new FXMLLoader(getClass().getResource("/dk/easv/belman/WorkerDashboard.fxml"));
            Parent prodRoot = prodLoader.load();
            WorkerDashboardController prodController = prodLoader.getController();
            prodController.setCurrentUser(currentUser);

            BorderPane prodPane = (BorderPane) prodRoot;
            Node prodCenter = prodPane.getCenter();
            prodTabContent.getChildren().setAll(prodCenter);
            AnchorPane.setTopAnchor(prodCenter, 0.0);
            AnchorPane.setBottomAnchor(prodCenter, 0.0);
            AnchorPane.setLeftAnchor(prodCenter, 0.0);
            AnchorPane.setRightAnchor(prodCenter, 0.0);
             */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupUserSearch() {
        FilteredList<User> filteredUsers = new FilteredList<>(usermodel.getUsers(), p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getUsername().toLowerCase().contains(lowerCaseFilter)
                        || user.getRole().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(userTableView.comparatorProperty());

        userTableView.setItems(sortedUsers);
    }

    private void setupUploadSearch() {
        FilteredList<UploadEntry> filteredUploads = new FilteredList<>(uploadModel.getAllUploads(), p -> true);

        uploadSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();

            filteredUploads.setPredicate(entry -> {
                if (newVal == null || newVal.isEmpty()) return true;

                return safeContains(entry.getOrderNumber(), lower)
                        || safeContains(entry.getUploadedBy(), lower)
                        || safeContains(entry.getStatus(), lower)
                        || safeContains(entry.getApprovedBy(), lower);
            });
        });

        SortedList<UploadEntry> sorted = new SortedList<>(filteredUploads);
        sorted.comparatorProperty().bind(uploadTable.comparatorProperty());

        uploadTable.setItems(sorted);
    }

    private boolean safeContains(String source, String target) {
        return source != null && source.toLowerCase().contains(target);
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.initOwner(UsernameTxt.getScene().getWindow());
        alert.show();
    }

    @FXML
    public void handleDeleteUser(ActionEvent event) {
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert("No user selected.");
            return;
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedUser.getUsername() + "?",
                ButtonType.YES, ButtonType.NO);

        confirm.initOwner(stage);
        confirm.initModality(Modality.WINDOW_MODAL);

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

    public void setCurrentUser(User user) {
        this.currentUser = user;
        currentUserLabel.setText("Logged in as: " + user.getUsername());
        loadTabContent();
    }
}
