package com.stockapp.services.interfaces;

import java.util.List;
import java.util.Optional;

public interface CrudService<T> {
	T create(T entity);

	Optional<T> read(Long id);

	T update(T entity);

	void delete(Long id);

	List<T> readAll();
}
