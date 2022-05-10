package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.PackingStation;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.PackingStationMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.PackingStationEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PackingStationRepositoryImpl implements PackingStationRepository {

  private final PackingStationEntityRepository entityRepository;
  private final PackingStationMapper mapper;

  @Override
  public Optional<PackingStation> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public Optional<PackingStation> findByEquipmentNumber(Integer equipmentNumber) {
    return entityRepository.findByEquipmentNumber(equipmentNumber).map(mapper::map);
  }
}
