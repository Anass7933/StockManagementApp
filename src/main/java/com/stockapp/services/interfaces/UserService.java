package com.stockapp.services.interfaces;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import java.util.List;

public interface UserService extends CrudService<User> {
	List<User> findByRole(UserRole role);

	User findByUsername(String userName);
}
