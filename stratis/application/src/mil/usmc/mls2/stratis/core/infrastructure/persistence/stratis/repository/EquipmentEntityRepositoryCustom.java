package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.EquipmentSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.EquipmentEntity;

import java.util.List;
import java.util.Optional;

public interface EquipmentEntityRepositoryCustom {

  Optional<EquipmentEntity> findByCurrentUserId(Integer currentUserId);

  Long count(EquipmentSearchCriteria criteria);

  List<EquipmentEntity> search(EquipmentSearchCriteria criteria);
}
