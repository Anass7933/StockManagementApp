package com.stockapp.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PasswordUtilsTest {
	@Test
	void testHashPassword_Consistency() {
		System.out.println("Running: testHashPassword_Consistency");

		String password = "mySecretPassword123";
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

		assertEquals(64, hash.length(), "SHA-256 hash length should be 64 characters");
	}
}
