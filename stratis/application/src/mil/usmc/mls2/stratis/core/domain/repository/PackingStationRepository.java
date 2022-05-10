package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.PackingStation;

import java.util.Optional;

public interface PackingStationRepository {

  Optional<PackingStation> findById(Integer id);

  Optional<PackingStation> findByEquipmentNumber(Integer equipmentNumber);
}
