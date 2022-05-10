package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.InvSerialLotNum;
import mil.usmc.mls2.stratis.core.domain.model.InvSerialLotNumSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.InvSerialLotNumMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.InvSerialLotNumEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.InvSerialLotNumEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InvSerialLotNumRepositoryImpl implements InvSerialLotNumRepository {

  private final InvSerialLotNumEntityRepository entityRepository;
  private final InvSerialLotNumMapper mapper;

  @Override
  public Optional<InvSerialLotNum> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public Long count(InvSerialLotNumSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public List<InvSerialLotNum> search(InvSerialLotNumSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public void save(InvSerialLotNum serial) {
    val entity = entityRepository.findById(serial.getInvSerialLotNumId()).orElseGet(InvSerialLotNumEntity::new);
    apply(entity, serial);
    entityRepository.saveAndFlush(entity);
  }

  @Override
  public void delete(InvSerialLotNum serial) {
    entityRepository.delete(InvSerialLotNumEntity.builder().invSerialLotNumId(serial.getInvSerialLotNumId()).build());
  }

  private void apply(InvSerialLotNumEntity entity, InvSerialLotNum input) {
    entity.setInvSerialLotNumId(input.getInvSerialLotNumId());
    entity.setNiinId(input.getNiinId());
    entity.setSerialNumber(input.getSerialNumber());
    entity.setLotConNum(input.getLotConNum());
    entity.setCc(input.getCc());
    entity.setExpirationDate(input.getExpirationDate());
    entity.setTimestamp(input.getTimestamp());
    entity.setQty(input.getQty());
    entity.setLocationId(input.getLocationId());
    entity.setInvDoneFlag(input.getInvDoneFlag());
    entity.setInventoryItemId(input.getInventoryItemId());
  }
}
