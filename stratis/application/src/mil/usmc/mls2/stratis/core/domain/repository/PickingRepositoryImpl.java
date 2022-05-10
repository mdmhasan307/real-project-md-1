package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Picking;
import mil.usmc.mls2.stratis.core.domain.model.PickingSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.PickingMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PickingEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.NiinLocationEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.PickingEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class PickingRepositoryImpl implements PickingRepository {

  private final PickingEntityRepository entityRepository;
  private final NiinLocationEntityRepository niinLocationEntityRepository;
  private final PickingMapper mapper;

  @Override
  public void save(Picking picking) {
    PickingEntity entity = new PickingEntity();
    if (picking.getPid() != null) entity = entityRepository.findById(picking.getPid()).orElseGet(PickingEntity::new);
    apply(entity, picking);
    val result = entityRepository.saveAndFlush(entity);
    picking.updateIdFromDb(result.getPid());
  }

  @Override
  public void delete(Picking picking) {
    entityRepository.deleteById(picking.getPid());
  }

  @Override
  public void deleteByScn(String scn) {
    entityRepository.deleteByScn(scn);
  }

  @Override
  public void deleteHistoryByScnAndDate(String scn, OffsetDateTime createdDate) {
    entityRepository.deleteHistoryByScnAndDate(scn, createdDate);
  }

  @Override
  public Optional<Long> findCountOfPickingHistory(String scn, String status, OffsetDateTime createdDate) {
    return entityRepository.findCountOfPickingHistory(scn, status, createdDate);
  }

  @Override
  public Optional<Picking> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public Long count(PickingSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public List<Picking> search(PickingSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public List<Picking> findByScn(String scn) {
    val criteria = PickingSearchCriteria.builder().scn(scn).build();
    return search(criteria);
  }

  @Override
  public List<Picking> getPicksForProcessing(PickingSearchCriteria criteria) {
    return entityRepository.getPicksForProcessing(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  private void apply(PickingEntity entity, Picking input) {

    entity.setAssignToUser(input.getAssignToUser());
    entity.setBypassCount(input.getBypassCount());
    entity.setCreatedBy(input.getCreatedBy());
    entity.setCreatedDate(input.getCreatedDate());
    entity.setModifiedBy(input.getModifiedBy());
    entity.setModifiedDate(input.getModifiedDate());
    entity.setPackedDate(input.getPackedDate());
    entity.setPackingConsolidationId(input.getPackingConsolidationId());
    entity.setPickedBy(input.getPickedBy());
    entity.setPickQty(input.getPickQty());
    entity.setPid(input.getPid());
    entity.setPin(input.getPin());
    entity.setQtyPicked(input.getQtyPicked());
    entity.setQtyRefused(input.getQtyRefused());
    entity.setRefusedBy(input.getRefusedBy());
    entity.setRefusedDate(input.getRefusedDate());
    entity.setRefusedFlag(input.getRefusedFlag());
    entity.setScn(input.getScn());
    entity.setSecurityMarkClass(input.getSecurityMarkClass());
    entity.setStatus(input.getStatus());
    entity.setSuffixCode(input.getSuffixCode());
    entity.setTimeOfPick(input.getTimeOfPick());

    if (input.getNiinLocation() != null) {
      val niinLocation = niinLocationEntityRepository.findById(input.getNiinLocation().getNiinLocationId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate WAC with id '%s'", input.getNiinLocation().getNiinLocationId())));
      entity.setNiinLocation(niinLocation);
    }
  }
}