package com.stockapp.services.interfaces;

import com.stockapp.models.entities.SaleItem;
import java.util.List;

public interface SaleItemService extends CrudService<SaleItem> {

	List<SaleItem> creatSaleItems(List<SaleItem> items);

	void updateSaleItems(Long saleId, List<SaleItem> items);

	List<SaleItem> findByProductId(Long productId);

	List<SaleItem> findBySaleId(Long saleId);
}
