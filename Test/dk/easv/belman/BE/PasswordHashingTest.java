package dk.easv.belman.BE;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashingTest {

    @Test
    void hashPassword() {
        //Arrange
        String rndPassword = "password123";
        String hashedPassword = PasswordHashing.hashPassword(rndPassword);

        //act
        boolean isVerified = PasswordHashing.verifyPassword(rndPassword, hashedPassword);

        //Assert
        assertTrue(isVerified);

    }

    @Test
    void verifyPassword() {
        //arrange
        String correctPassword = "correct123";
        String hash = PasswordHashing.hashPassword(correctPassword);

        // Act
        boolean isVerified = PasswordHashing.verifyPassword("WrongPassword", hash);

        // Assert
        assertTrue(isVerified);
    }
}