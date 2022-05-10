package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrack;
import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrackSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SerialLotNumTrackMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SerialLotNumTrackEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.SerialLotNumTrackEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SerialLotNumTrackRepositoryImpl implements SerialLotNumTrackRepository {

  private final SerialLotNumTrackEntityRepository entityRepository;
  private final SerialLotNumTrackMapper mapper;

  @Override
  public Optional<SerialLotNumTrack> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public List<SerialLotNumTrack> search(SerialLotNumTrackSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public void save(SerialLotNumTrack serial) {
    val entity = entityRepository.findById(serial.getSerialLotNumTrackId()).orElseGet(SerialLotNumTrackEntity::new);
    apply(entity, serial);
    entityRepository.saveAndFlush(entity);
  }

  @Override
  public void delete(SerialLotNumTrack serial) {
    entityRepository.delete(SerialLotNumTrackEntity.builder().serialLotNumTrackId(serial.getSerialLotNumTrackId()).build());
  }

  private void apply(SerialLotNumTrackEntity entity, SerialLotNumTrack input) {

    entity.setNiinId(input.getNiinId());
    entity.setSerialNumber(input.getSerialNumber());
    entity.setLotConNum(input.getLotConNum());
    entity.setCc(input.getCc());
    entity.setExpirationDate(input.getExpirationDate());
    entity.setIssuedFlag(input.getIssuedFlag());
    entity.setTimestamp(input.getTimestamp());
    entity.setQty(input.getQty());
    entity.setLocationId(input.getLocationId());
    entity.setIuid(input.getIuid());
    entity.setRecallFlag(input.getRecallFlag());
    entity.setRecallDate(input.getRecallDate());
  }
}
