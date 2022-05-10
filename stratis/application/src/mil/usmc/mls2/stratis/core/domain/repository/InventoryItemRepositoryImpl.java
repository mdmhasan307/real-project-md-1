package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.InventoryItem;
import mil.usmc.mls2.stratis.core.domain.model.InventoryItemSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.InventoryItemMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.InventoryItemEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.InventoryItemEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.LocationEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.NiinLocationEntityRepository;
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
class InventoryItemRepositoryImpl implements InventoryItemRepository {

  private final InventoryItemEntityRepository entityRepository;
  private final InventoryItemMapper mapper;
  private final LocationEntityRepository locationEntityRepository;
  private final NiinLocationEntityRepository niinLocationEntityRepository;
  private final WacEntityRepository wacEntityRepository;

  @Override
  public void save(InventoryItem inventoryItem) {
    val entity = entityRepository.findById(inventoryItem.getInventoryItemId()).orElseGet(InventoryItemEntity::new);
    apply(entity, inventoryItem);
    entityRepository.saveAndFlush(entity);
  }

  @Override
  public Optional<InventoryItem> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public Long count(InventoryItemSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public List<InventoryItem> search(InventoryItemSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  private void apply(InventoryItemEntity entity, InventoryItem input) {

    entity.setAssignToUser(input.getAssignToUser());
    entity.setBypassCount(input.getBypassCount());
    entity.setCreatedBy(input.getCreatedBy());
    entity.setCreatedDate(input.getCreatedDate());
    entity.setModifiedBy(input.getModifiedBy());
    entity.setModifiedDate(input.getModifiedDate());
    entity.setCompletedDate(input.getCompletedDate());
    entity.setCompletedBy(input.getCompletedBy());
    entity.setCumNegAdj(input.getCumNegAdj());
    entity.setCumPosAdj(input.getCumPosAdj());
    entity.setInventoryId(input.getInventoryId());
    entity.setInventoryItemId(input.getInventoryItemId());
    entity.setInvType(input.getInvType());
    entity.setNiinId(input.getNiinId());
    entity.setNiinLocQty(input.getNiinLocQty());
    entity.setNumCounted(input.getNumCounted());
    entity.setNumCounts(input.getNumCounts());
    entity.setPriority(input.getPriority());
    entity.setReleasedBy(input.getReleasedBy());
    entity.setReleasedDate(input.getReleasedDate());
    entity.setTransactionType(input.getTransactionType());
    entity.setStatus(input.getStatus());

    if (input.getWac() != null) {
      val wac = wacEntityRepository.findById(input.getWac().getWacId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate WAC with id '%s'", input.getWac().getWacId())));
      entity.setWac(wac);
    }

    if (input.getLocation() != null) {
      val location = locationEntityRepository.findById(input.getLocation().getLocationId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate Location with id '%s'", input.getLocation().getLocationId())));
      entity.setLocation(location);
    }

    if (input.getNiinLocation() != null) {
      val niinLocation = niinLocationEntityRepository.findById(input.getNiinLocation().getNiinLocationId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate Niin Location with id '%s'", input.getNiinLocation().getNiinLocationId())));
      entity.setNiinLocation(niinLocation);
    }

    if (input.getLocation() != null) {
      val location = locationEntityRepository.findById(input.getLocation().getLocationId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate Location with id '%s'", input.getLocation().getLocationId())));
      entity.setLocation(location);
    }
  }
}