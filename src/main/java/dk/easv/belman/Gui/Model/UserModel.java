package dk.easv.belman.Gui.Model;

import dk.easv.belman.BE.User;
import dk.easv.belman.BLL.UserBLL;
import dk.easv.belman.Utility.ModelException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public class UserModel {
    private static final UserModel instance = new UserModel();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final UserBLL userBLL = new UserBLL();

    private UserModel() {}

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
            throw new ModelException("Failed to authenticate user", e);
        }
    }

    public void addUser(User user) throws SQLException {
        User addedUser = userBLL.addUser(user);
        users.add(addedUser);
    }

    public boolean userExists(String username) throws SQLException {
        return userBLL.userExists(username);
    }

    public void deleteUser(User user) throws SQLException {
        userBLL.deleteUser(user);
        users.remove(user);
    }

    public void loadUsersFromDatabase() {
        try {
            List<User> allUsers = userBLL.getAllUsers();
            users.setAll(allUsers);
        } catch (SQLException e) {
            throw new ModelException("Failed to load users from database", e);
        }
    }
}
