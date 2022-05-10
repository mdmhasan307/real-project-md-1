package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.InventoryItem;
import mil.usmc.mls2.stratis.core.domain.model.InventoryItemSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.Wac;

import java.util.List;
import java.util.Optional;

public interface InventoryItemService {

  Optional<InventoryItem> findById(Integer id);

  Long count(InventoryItemSearchCriteria criteria);

  void deAssignAllInventoryItemsForUser(Integer userId);

  List<InventoryItem> search(InventoryItemSearchCriteria criteria);

  List<InventoryItem> getInventoryListForProcessing(InventoryItemSearchCriteria criteria, Wac wac);

  void save(InventoryItem inventoryItem);
}
