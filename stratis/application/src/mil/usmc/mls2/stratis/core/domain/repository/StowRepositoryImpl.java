package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Stow;
import mil.usmc.mls2.stratis.core.domain.model.StowSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.StowMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.StowEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.LocationEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.PickingEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.ReceiptEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.StowEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class StowRepositoryImpl implements StowRepository {

  private final StowEntityRepository entityRepository;
  private final LocationEntityRepository locationEntityRepository;
  private final PickingEntityRepository pickingEntityRepository;
  private final ReceiptEntityRepository receiptEntityRepository;

  private final StowMapper mapper;

  @Override
  public Optional<Stow> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public Long count(StowSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public List<Stow> search(StowSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public void save(Stow stow) {
    val id = stow.getStowId();
    val entity = id != null ? entityRepository.findById(id).orElseGet(StowEntity::new) : new StowEntity();
    apply(entity, stow);
    entityRepository.saveAndFlush(entity);
  }

  @Override
  public void delete(Stow stow) {
    entityRepository.delete(StowEntity.builder().stowId(stow.getStowId()).build());
  }

  private void apply(StowEntity entity, Stow input) {

    entity.setSid(input.getSid());
    entity.setQtyToBeStowed(input.getQtyToBeStowed());
    if (input.getReceipt() != null) {
      val receipt = receiptEntityRepository
          .findById(input.getReceipt().getRcn())
          .orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate Receipt with id '%s'", input.getReceipt().getRcn())));
      entity.setReceipt(receipt);
    }
    entity.setCreatedBy(input.getCreatedBy());
    entity.setCreatedDate(input.getCreatedDate());
    entity.setModifiedBy(input.getModifiedBy());
    entity.setModifiedDate(input.getModifiedDate());
    if (input.getPick() != null) {
      val picking = pickingEntityRepository
          .findById(input.getPick().getPid())
          .orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate WAC with id '%s'", input.getPick().getPid())));
      entity.setPickingEntity(picking);
    }
    entity.setStatus(input.getStatus());
    entity.setCancelReason(input.getCancelReason());
    entity.setExpirationDate(input.getExpirationDate());
    entity.setDateOfManufacture(input.getDateOfManufacture());
    if (input.getLocation() != null) {
      val location = locationEntityRepository
          .findById(input.getLocation().getLocationId())
          .orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate WAC with id '%s'", input.getLocation().getLocationId())));
      entity.setLocation(location);
    }
    entity.setLotConNum(input.getLotConNum());
    entity.setCaseWeightQty(input.getCaseWeightQty());
    entity.setPackedDate(input.getPackedDate());
    entity.setSerialNumber(input.getSerialNumber());
    entity.setStowQty(input.getStowQty());
    entity.setScanInd(input.getScanInd());
    entity.setBypassCount(input.getBypassCount());
    entity.setAssignToUser(input.getAssignToUser());
    entity.setStowedBy(input.getStowedBy());
    entity.setStowedDate(input.getStowedDate());
    entity.setSecurityMarkClass(input.getSecurityMarkClass());
  }
}
