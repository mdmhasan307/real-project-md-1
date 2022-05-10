package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PackingStationEntity;

import java.util.Optional;

public interface PackingStationEntityRepository extends EntityRepository<PackingStationEntity, Integer> {

  Optional<PackingStationEntity> findByEquipmentNumber(Integer equipmentNumber);
}
