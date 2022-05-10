package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.NiinLocation;
import mil.usmc.mls2.stratis.core.domain.model.NiinLocationSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.NiinLocationMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.NiinLocationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.LocationEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.NiinInfoEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.NiinLocationEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class NiinLocationRepositoryImpl implements NiinLocationRepository {

  private final NiinLocationEntityRepository entityRepository;
  private final LocationEntityRepository locationRepository;
  private final NiinInfoEntityRepository niinInfoEntityRepository;
  private final NiinLocationMapper mapper;

  @Override
  public void save(NiinLocation location) {

    val entity = location.getNiinLocationId() != null ? entityRepository.findById(location.getNiinLocationId()).orElseGet(NiinLocationEntity::new) : new NiinLocationEntity();
    apply(entity, location);
    entityRepository.saveAndFlush(entity);
  }

  private void apply(NiinLocationEntity entity, NiinLocation input) {
    if (input.getLocation() != null) {
      val location = locationRepository.findById(input.getLocation().getLocationId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate WAC with id '%s'", input.getLocation().getLocationId())));
      entity.setLocation(location);
    }
    entity.setQty(input.getQty());
    entity.setExpirationDate(input.getExpirationDate());
    entity.setDateOfManufacture(input.getDateOfManufacture());
    entity.setCc(input.getCc());
    entity.setLocked(input.getLocked());
    entity.setCreatedBy(input.getCreatedBy());
    entity.setCreatedDate(input.getCreatedDate());
    entity.setModifiedBy(input.getModifiedBy());
    entity.setModifiedDate(input.getModifiedDate());
    entity.setProjectCode(input.getProjectCode());
    entity.setPc(input.getPc());
    entity.setLastInvDate(input.getLastInvDate());
    entity.setCaseWeightQty(input.getCaseWeightQty());
    entity.setLotConNum(input.getLotConNum());
    entity.setSerialNumber(input.getSerialNumber());
    entity.setPackedDate(input.getPackedDate());
    entity.setNumExtents(input.getNumExtents());
    entity.setNumCounts(input.getNumCounts());
    entity.setUnderAudit(input.getUnderAudit());
    entity.setOldUi(input.getOldUi());
    entity.setNsnRemark(input.getNsnRemark());
    entity.setExpRemark(input.getExpRemark());
    entity.setOldQty(input.getOldQty());
    entity.setRecallFlag(input.getRecallFlag());
    entity.setSecurityMarkClass(input.getSecurityMarkClass());
    if (input.getNiinInfo() != null) {
      val niinInfo = niinInfoEntityRepository.findById(input.getNiinInfo().getNiinId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate Niin Info with id '%s'", input.getNiinInfo().getNiinId())));
      entity.setNiinInfo(niinInfo);
    }
  }

  @Override
  @Transactional
  public Optional<NiinLocation> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public long count(NiinLocationSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public List<NiinLocation> search(NiinLocationSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }
}