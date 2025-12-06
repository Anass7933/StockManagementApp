package com.stockapp.services.interfaces;

import com.stockapp.models.entities.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService extends CrudService<Product> {
	List<Product> findByCategory(String category);

	Optional<Product> findByName(String keyword);

	List<Product> findByPreName(String keyword);

	void updateStock(Long productId, int ammount);

	boolean isNeedRestock(Long productId);
}
