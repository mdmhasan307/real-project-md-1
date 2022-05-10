package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Equipment;
import mil.usmc.mls2.stratis.core.domain.model.EquipmentSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository {

  void save(Equipment equipment);

  Optional<Equipment> findById(Integer id);

  Optional<Equipment> findByCurrentUserId(Integer currentUserId);

  List<Equipment> getAll();

  Long count(EquipmentSearchCriteria criteria);
  
  List<Equipment> search(EquipmentSearchCriteria criteria);
}
