package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Location;
import mil.usmc.mls2.stratis.core.domain.model.LocationSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.LocationMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.LocationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.LocationEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.WacEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class LocationRepositoryImpl implements LocationRepository {

  private final LocationEntityRepository entityRepository;
  private final WacEntityRepository wacEntityRepository;
  private final LocationMapper mapper;

  @Override
  public void save(Location location) {
    val entity = entityRepository.findById(location.getLocationId()).orElseGet(LocationEntity::new);
    apply(entity, location);
    entityRepository.saveAndFlush(entity);
  }

  private void apply(LocationEntity entity, Location input) {
    entity.setAisle(input.getAisle());
    entity.setSide(input.getSide());
    entity.setBay(input.getBay());
    entity.setLocLevel(input.getLocLevel());
    entity.setAvailabilityFlag(input.getAvailabilityFlag());
    entity.setLocationLabel(input.getLocationLabel());
    entity.setSlot(input.getSlot());
    if (input.getWac() != null) {
      val wac = wacEntityRepository.findById(input.getWac().getWacId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate WAC with id '%s'", input.getWac().getWacId())));
      entity.setWac(wac);
    }
    entity.setLocationHeaderBinId(input.getLocationHeaderBinId());
    entity.setMechanizedFlag(input.getMechanizedFlag());
    entity.setLocClassificationId(input.getLocClassificationId());
    entity.setDividerIndex(input.getDividerIndex());
    entity.setCube(input.getCube());
    entity.setWeight(input.getWeight());
    entity.setLastStowDate(input.getLastStowDate());
    entity.setCreatedBy(input.getCreatedBy());
    entity.setCreatedDate(input.getCreatedDate());
    entity.setModifiedBy(input.getModifiedBy());
    entity.setModifiedDate(input.getModifiedDate());
  }

  @Override
  public Optional<Location> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public Long count(LocationSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  public List<Location> search(LocationSearchCriteria criteria) { return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList()); }
}