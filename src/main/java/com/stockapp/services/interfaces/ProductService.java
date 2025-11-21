package com.stockapp.services.interfaces;

import com.stockapp.models.entities.Product;
import java.util.List;

public interface ProductService extends CrudService<Product> {
	List<Product> findByCategory(String category);

	Product findByName(String keyword);

	void updateStock(Long productId, int ammount);

	boolean isNeedRestock(Long productId);
}
