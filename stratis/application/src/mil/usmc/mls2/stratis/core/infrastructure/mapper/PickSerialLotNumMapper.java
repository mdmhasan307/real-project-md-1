package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNum;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PickSerialLotNumEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class PickSerialLotNumMapper {

  private final SerialLotNumTrackMapper serialLotNumTrackMapper;

  public PickSerialLotNumMapper(@Lazy SerialLotNumTrackMapper serialLotNumTrackMapper) {
    this.serialLotNumTrackMapper = serialLotNumTrackMapper;
  }

  public PickSerialLotNum map(PickSerialLotNumEntity input) {
    if (input == null) return null;

    return PickSerialLotNum.builder()
        .pickSerialLotNum(input.getPickSerialLotNum())
        .serialLotNumTrack(serialLotNumTrackMapper.map(input.getSerialLotNumTrack()))
        .timestamp(input.getTimestamp())
        .pid(input.getPid())
        .qty(input.getQty())
        .lotConNum(input.getLotConNum())
        .serialNumber(input.getSerialNumber())
        .scn(input.getScn())
        .locationId(input.getLocationId())
        .expirationDate(input.getExpirationDate()).build();
  }
}
