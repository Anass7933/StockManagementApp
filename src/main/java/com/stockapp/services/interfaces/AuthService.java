package com.stockapp.services.interfaces;

import com.stockapp.models.entities.User;

public interface AuthService {
	User validateLogin(String username, String password);
}
