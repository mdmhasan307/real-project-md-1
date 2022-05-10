package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNum;
import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNumSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.PickSerialLotNumMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PickSerialLotNumEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.PickSerialLotNumEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.SerialLotNumTrackEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PickSerialLotNumRepositoryImpl implements PickSerialLotNumRepository {

  private final PickSerialLotNumEntityRepository entityRepository;
  private final SerialLotNumTrackEntityRepository serialLotNumTrackEntityRepository;
  private final PickSerialLotNumMapper mapper;

  @Override
  public List<PickSerialLotNum> search(PickSerialLotNumSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public void delete(PickSerialLotNum pickSerialLotNum) {
    entityRepository.delete(PickSerialLotNumEntity.builder().pickSerialLotNum(pickSerialLotNum.getPickSerialLotNum()).build());
  }

  @Override
  public void save(PickSerialLotNum pickSerialLotNum) {
    val entity = entityRepository.findById(pickSerialLotNum.getPickSerialLotNum()).orElseGet(PickSerialLotNumEntity::new);
    apply(entity, pickSerialLotNum);
    entityRepository.saveAndFlush(entity);
  }

  private void apply(PickSerialLotNumEntity entity, PickSerialLotNum input) {

    entity.setSerialNumber(input.getSerialNumber());
    entity.setLotConNum(input.getLotConNum());
    entity.setExpirationDate(input.getExpirationDate());
    entity.setTimestamp(input.getTimestamp());
    entity.setQty(input.getQty());
    entity.setLocationId(input.getLocationId());
    entity.setPickSerialLotNum(input.getPickSerialLotNum());
    entity.setPid(input.getPid());
    entity.setScn(input.getScn());

    if (input.getSerialLotNumTrack() != null) {
      val serialLotNum = serialLotNumTrackEntityRepository.findById(input.getSerialLotNumTrack().getSerialLotNumTrackId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate Serial Lot Num with id '%s'", input.getSerialLotNumTrack().getSerialLotNumTrackId())));
      entity.setSerialLotNumTrack(serialLotNum);
    }
  }
}
