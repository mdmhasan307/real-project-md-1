package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.PackingConsolidationSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PackingConsolidationEntity;

import java.util.List;
import java.util.Optional;

public interface PackingConsolidationEntityRepositoryCustom {

  Long count(PackingConsolidationSearchCriteria criteria);

  List<PackingConsolidationEntity> search(PackingConsolidationSearchCriteria criteria);

  Optional<PackingConsolidationEntity> findPackingConsolidationByEquipmentAndCustomer(String equipmentDescription, String packingGroup, Integer customerId);
}
