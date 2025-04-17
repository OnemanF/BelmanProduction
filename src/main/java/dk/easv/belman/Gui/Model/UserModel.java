package dk.easv.belman.Gui.Model;

import dk.easv.belman.BE.User;
import dk.easv.belman.BLL.UserBLL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class UserModel {
    private static final UserModel instance = new UserModel();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final UserBLL userBLL = new UserBLL();

    private UserModel() {
    }

    public static UserModel getInstance() {
        return instance;
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    public User getAuthenticatedUser(String username, String password) {
        try {
            return userBLL.getAuthenticatedUser(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
