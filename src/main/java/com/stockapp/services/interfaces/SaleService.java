package com.stockapp.services.interfaces;

import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.SaleItem;
import java.util.List;

public interface SaleService extends CrudService<Sale> {
	Sale createSaleWithItems(Sale sale, List<SaleItem> items);
}
