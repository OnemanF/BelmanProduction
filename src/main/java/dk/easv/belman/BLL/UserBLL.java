package dk.easv.belman.BLL;

import dk.easv.belman.BE.User;
import dk.easv.belman.DAL.UserDal;

import java.sql.SQLException;
import java.util.List;

public class UserBLL {
    private final UserDal userDAL = new UserDal();

    public User getAuthenticatedUser(String username, String password) throws SQLException {
        return userDAL.getAuthenticatedUser(username, password);
    }

    public User addUser(User user) throws SQLException {
        return userDAL.addUser(user);
    }

    public boolean userExists(String username) throws SQLException {
        return userDAL.userExists(username);
    }

    public void deleteUser(User user) throws SQLException {
        userDAL.deleteUser(user);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAL.getAllUsers();
    }

}