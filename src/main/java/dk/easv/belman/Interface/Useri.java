package dk.easv.belman.Interface;

import dk.easv.belman.BE.User;

import java.sql.SQLException;
import java.util.List;

public interface Useri {
    User addUser(User user) throws SQLException;
    boolean userExists(String username) throws SQLException;
    void deleteUser(User user) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    User getByUsername(String username) throws SQLException;
}
