package dk.easv.belman.Gui.Model;

import dk.easv.belman.BE.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    private UserModel userModel;

    @BeforeEach
    void setUp() {
        userModel = UserModel.getInstance(); // Singleton
        userModel.getUsers().clear(); // Reset list before each test
    }

    @Test
    void getInstance_shouldReturnSameInstance() {
        assertNotNull(userModel);
    }

    @Test
    void addUser_shouldAddUserToList() {
        // Arrange
        User user = new User(-1, "testuser", "pass", "Admin");

        // Act
        userModel.getUsers().add(user);

        // Assert
        assertEquals(1, userModel.getUsers().size());
        assertEquals("testuser", userModel.getUsers().get(0).getUsername());
    }
}