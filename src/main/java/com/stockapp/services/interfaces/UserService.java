package com.stockapp.services.interfaces;

import java.util.List;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;

public interface UserService extends CrudService<User> {
	List<User> findByRole(UserRole role);

	List<User> findByuserName(String userName);
}
