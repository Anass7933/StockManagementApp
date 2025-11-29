package com.stockapp.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class PasswordUtilsTest {

    @Test
    public void testHashPassword_ShouldReturnNonNullHash() {
        String password = "testPassword123";
        String hash = PasswordUtils.hashPassword(password);

        assertNotNull(hash, "Hash should not be null");
        assertFalse(hash.isEmpty(), "Hash should not be empty");
    }

    @Test
    public void testHashPassword_ShouldReturnConsistentHash() {
        String password = "mySecurePassword";
        String hash1 = PasswordUtils.hashPassword(password);
        String hash2 = PasswordUtils.hashPassword(password);

        assertEquals(hash1, hash2, "Same password should produce same hash");
    }

    @Test
    public void testHashPassword_DifferentPasswordsShouldProduceDifferentHashes() {
        String password1 = "password1";
        String password2 = "password2";

        String hash1 = PasswordUtils.hashPassword(password1);
        String hash2 = PasswordUtils.hashPassword(password2);

        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }

    @Test
    public void testHashPassword_ShouldReturn64CharacterHash() {
        String password = "testPassword";
        String hash = PasswordUtils.hashPassword(password);

        // SHA-256 produces 64 hexadecimal characters
        assertEquals(64, hash.length(), "SHA-256 hash should be 64 characters long");
    }

    @Test
    public void testVerifyPassword_ShouldReturnTrueForCorrectPassword() {
        String password = "correctPassword";
        String hash = PasswordUtils.hashPassword(password);

        assertTrue(PasswordUtils.verifyPassword(password, hash),
                "Verification should succeed for correct password");
    }

    @Test
    public void testVerifyPassword_ShouldReturnFalseForIncorrectPassword() {
        String correctPassword = "correctPassword";
        String incorrectPassword = "wrongPassword";
        String hash = PasswordUtils.hashPassword(correctPassword);

        assertFalse(PasswordUtils.verifyPassword(incorrectPassword, hash),
                "Verification should fail for incorrect password");
    }

    @Test
    public void testVerifyPassword_EmptyPassword() {
        String password = "";
        String hash = PasswordUtils.hashPassword(password);

        assertTrue(PasswordUtils.verifyPassword(password, hash),
                "Empty password should verify against its own hash");
    }

    @Test
    public void testHashPassword_SpecialCharacters() {
        String password = "p@ssw0rd!#$%^&*()";
        String hash = PasswordUtils.hashPassword(password);

        assertNotNull(hash);
        assertTrue(PasswordUtils.verifyPassword(password, hash),
                "Password with special characters should hash and verify correctly");
    }

    @Test
    public void testHashPassword_UnicodeCharacters() {
        String password = "пароль密码🔒";
        String hash = PasswordUtils.hashPassword(password);

        assertNotNull(hash);
        assertTrue(PasswordUtils.verifyPassword(password, hash),
                "Password with unicode characters should hash and verify correctly");
    }

    @Test
    public void testHashPassword_LongPassword() {
        String password = "a".repeat(1000);
        String hash = PasswordUtils.hashPassword(password);

        assertNotNull(hash);
        assertEquals(64, hash.length(), "Long password should still produce 64-character hash");
        assertTrue(PasswordUtils.verifyPassword(password, hash));
    }
}
