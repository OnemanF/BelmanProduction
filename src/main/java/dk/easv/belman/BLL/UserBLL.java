package dk.easv.belman.BLL;

import dk.easv.belman.BE.PasswordHashing;
import dk.easv.belman.BE.User;
import dk.easv.belman.DAL.UserDal;
import dk.easv.belman.Interface.Useri;

import java.sql.SQLException;
import java.util.List;

public class UserBLL {
    private final Useri userDAL;

    public UserBLL() {
        this.userDAL = new UserDal();
    }

    public User getAuthenticatedUser(String username, String password) throws SQLException {
        User found = userDAL.getByUsername(username);
        if (found != null && PasswordHashing.verifyPassword(password, found.getPassword())) {
            return found;
        }
        return null;
    }

    public User addUser(User user) throws SQLException {
        String hashed = PasswordHashing.hashPassword(user.getPassword());
        user.setPassword(hashed);
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