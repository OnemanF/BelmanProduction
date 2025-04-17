package dk.easv.belman.BLL;

import dk.easv.belman.BE.User;
import dk.easv.belman.DAL.UserDal;

import java.sql.SQLException;

public class UserBLL {
    private final UserDal userDAL = new UserDal();

    public User getAuthenticatedUser(String username, String password) throws SQLException {
        return userDAL.getAuthenticatedUser(username, password);
    }
}