package com.stockapp.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void testHashPassword_Consistency() {
        System.out.println("Running: testHashPassword_Consistency");
        
        String password = "mySecretPassword123";
        
        // Hashing the same password twice should produce the exact same string
        String hash1 = PasswordUtils.hashPassword(password);
        String hash2 = PasswordUtils.hashPassword(password);

        assertNotNull(hash1);
        assertEquals(hash1, hash2, "Hashing consistency failed");
    }

    @Test
    void testHashPassword_Uniqueness() {
        System.out.println("Running: testHashPassword_Uniqueness");
        
        String hash1 = PasswordUtils.hashPassword("passwordA");
        String hash2 = PasswordUtils.hashPassword("passwordB");

        assertNotEquals(hash1, hash2, "Different passwords must have different hashes");
    }

    @Test
    void testVerifyPassword_Success() {
        System.out.println("Running: testVerifyPassword_Success");
        
        String plainText = "adminUser";
        String storedHash = PasswordUtils.hashPassword(plainText);

        assertTrue(PasswordUtils.verifyPassword("adminUser", storedHash), 
            "Should verify successfully with the correct password");
    }

    @Test
    void testVerifyPassword_Failure() {
        System.out.println("Running: testVerifyPassword_Failure");
        
        String plainText = "adminUser";
        String storedHash = PasswordUtils.hashPassword(plainText);

        assertFalse(PasswordUtils.verifyPassword("wrongPassword", storedHash), 
            "Should fail verification with incorrect password");
    }

    @Test
    void testHashFormat_SHA256() {
        System.out.println("Running: testHashFormat_SHA256");
        
        String hash = PasswordUtils.hashPassword("test");
        
        // SHA-256 produces 32 bytes, which becomes a 64-character Hex string
        assertEquals(64, hash.length(), "SHA-256 hash length should be 64 characters");
        
        // Verify it contains only Hex characters (0-9, a-f)
        assertTrue(hash.matches("[0-9a-f]+"), "Hash should be a valid Hex string");
    }
}
