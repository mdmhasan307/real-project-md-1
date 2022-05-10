package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.PackingConsolidation;
import mil.usmc.mls2.stratis.core.domain.model.PackingConsolidationSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.PackingConsolidationMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PackingConsolidationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.CustomerEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.PackingConsolidationEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PackingConsolidationRepositoryImpl implements PackingConsolidationRepository {

  private final PackingConsolidationEntityRepository entityRepository;
  private final PackingConsolidationMapper mapper;
  private final CustomerEntityRepository customerEntityRepository;

  @Override
  public void save(PackingConsolidation packingConsolidation) {
    PackingConsolidationEntity entity = new PackingConsolidationEntity();
    if (packingConsolidation.getPackingConsolidationId() != null)
      entity = entityRepository.findById(packingConsolidation.getPackingConsolidationId()).orElseGet(PackingConsolidationEntity::new);
    apply(entity, packingConsolidation);
    val result = entityRepository.saveAndFlush(entity);
    packingConsolidation.updateIdFromDb(result.getPackingConsolidationId());
  }

  @Override
  public List<PackingConsolidation> search(PackingConsolidationSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Optional<PackingConsolidation> findPackingConsolidationByEquipmentAndCustomer(String equipmentDescription, String packingGroup, Integer customerId) {
    return entityRepository.findPackingConsolidationByEquipmentAndCustomer(equipmentDescription, packingGroup, customerId).map(mapper::map);
  }

  @Override
  public long count(PackingConsolidationSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public void delete(Integer id) {
    entityRepository.deleteById(id);
  }

  @Override
  public Optional<PackingConsolidation> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  private void apply(PackingConsolidationEntity entity, PackingConsolidation input) {

    entity.setClosedCarton(input.getClosedCarton());
    entity.setConsolidationBarcode(input.getConsolidationBarcode());
    entity.setConsolidationCube(input.getConsolidationCube());
    entity.setConsolidationWeight(input.getConsolidationWeight());
    entity.setCreatedBy(input.getCreatedBy());
    entity.setCreatedDate(input.getCreatedDate());

    entity.setIssuePriorityGroup(input.getIssuePriorityGroup());
    entity.setModifiedBy(input.getModifiedBy());
    entity.setModifiedDate(input.getModifiedDate());
    entity.setNumberOfIssues(input.getNumberOfIssues());
    entity.setPackColumn(input.getPackColumn());
    entity.setPackedBy(input.getPackedBy());
    entity.setPackedDate(input.getPackedDate());
    entity.setPackingConsolidationId(input.getPackingConsolidationId());
    entity.setPackingStationId(input.getPackingStationId());
    entity.setPackLevel(input.getPackLevel());
    entity.setPackLocationBarcode(input.getPackLocationBarcode());
    entity.setPartialRelease(input.getPartialRelease());
    entity.setSecurityMarkClass(input.getSecurityMarkClass());

    if (input.getCustomer() != null) {
      val customer = customerEntityRepository.findById(input.getCustomer().getCustomerId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate Customer with id '%s'", input.getCustomer().getCustomerId())));
      entity.setCustomer(customer);
    }
  }
}
