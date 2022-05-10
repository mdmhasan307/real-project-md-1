package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.InventoryItem;
import mil.usmc.mls2.stratis.core.domain.model.InventoryItemSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository {

  void save(InventoryItem inventoryItem);

  Optional<InventoryItem> findById(Integer id);

  Long count(InventoryItemSearchCriteria criteria);

  List<InventoryItem> search(InventoryItemSearchCriteria criteria);
}
