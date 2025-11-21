package com.stockapp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {
	public static String hashPassword(String plainPassword) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashedBytes = md.digest(plainPassword.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : hashedBytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Hashing algorithm not found", e);
		}
	}

	public static String unhashPassword(String hashedPassword) throws NoSuchAlgorithmException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] unhashedBytes = md.digest(hashedPassword.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : unhashedBytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Hashing algorithm not found", e);
		}
	}

	public static boolean verifyPassword(String plainPassword, String storedHash) {
		String hashedInput = hashPassword(plainPassword);
		return hashedInput.equals(storedHash);
	}
}
