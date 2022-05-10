package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.PackingConsolidation;
import mil.usmc.mls2.stratis.core.domain.model.PackingConsolidationSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface PackingConsolidationRepository {

  void save(PackingConsolidation packingConsolidation);

  Optional<PackingConsolidation> findById(Integer id);

  void delete(Integer id);

  List<PackingConsolidation> search(PackingConsolidationSearchCriteria criteria);

  Optional<PackingConsolidation> findPackingConsolidationByEquipmentAndCustomer(String equipmentDescription, String packingGroup, Integer customerId);

  long count(PackingConsolidationSearchCriteria criteria);
}
