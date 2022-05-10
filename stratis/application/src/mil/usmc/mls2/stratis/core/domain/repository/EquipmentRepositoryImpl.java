package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Equipment;
import mil.usmc.mls2.stratis.core.domain.model.EquipmentSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.EquipmentMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.EquipmentEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EquipmentEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.WacEntityRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class EquipmentRepositoryImpl implements EquipmentRepository {

  private final EquipmentEntityRepository entityRepository;
  private final WacEntityRepository wacEntityRepository;
  private final EquipmentMapper mapper;

  @Override
  public void save(Equipment equipment) {
    val entity = entityRepository.findById(equipment.getEquipmentNumber()).orElseGet(EquipmentEntity::new);
    apply(entity, equipment);
    entityRepository.saveAndFlush(entity);
  }

  @Override
  public Optional<Equipment> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public List<Equipment> getAll() {
    return entityRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Long count(EquipmentSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public List<Equipment> search(EquipmentSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Optional<Equipment> findByCurrentUserId(Integer currentUserId) {
    return entityRepository.findByCurrentUserId(currentUserId).map(mapper::map);
  }

  private void apply(EquipmentEntity entity, Equipment input) {

    entity.setComPortEquipmentId(input.getComPortEquipmentId());
    entity.setComPortPrinterId(input.getComPortPrinterId());
    entity.setCurrentUserId(input.getCurrentUserId());
    entity.setDescription(input.getDescription());
    entity.setEquipmentNumber(input.getEquipmentNumber());
    entity.setName(input.getName());
    entity.setWarehouseId(input.getWarehouseId());
    entity.setHasCubiscan(input.getHasCubiscan());
    entity.setPackingGroup(input.getPackingGroup());
    entity.setPrinterName(input.getPrinterName());
    entity.setShippingArea(input.getShippingArea());

    if (input.getWac() != null) {
      val wac = wacEntityRepository.findById(input.getWac().getWacId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate WAC with id '%s'", input.getWac().getWacId())));
      entity.setWac(wac);
    }
  }
}