package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.InventoryItemSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.InventoryItemEntity;

import java.util.List;

public interface InventoryItemEntityRepositoryCustom {

  Long count(InventoryItemSearchCriteria criteria);

  List<InventoryItemEntity> search(InventoryItemSearchCriteria criteria);
}
