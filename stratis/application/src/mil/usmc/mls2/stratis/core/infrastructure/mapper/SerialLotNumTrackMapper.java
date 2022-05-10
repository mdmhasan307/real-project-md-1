package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrack;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SerialLotNumTrackEntity;
import org.springframework.stereotype.Component;

@Component
public class SerialLotNumTrackMapper {

  public SerialLotNumTrack map(SerialLotNumTrackEntity input) {
    if (input == null) return null;

    return SerialLotNumTrack.builder()
        .serialLotNumTrackId(input.getSerialLotNumTrackId())
        .niinId(input.getNiinId())
        .serialNumber(input.getSerialNumber())
        .lotConNum(input.getLotConNum())
        .cc(input.getCc())
        .expirationDate(input.getExpirationDate())
        .issuedFlag(input.getIssuedFlag())
        .timestamp(input.getTimestamp())
        .qty(input.getQty())
        .locationId(input.getLocationId())
        .iuid(input.getIuid())
        .recallFlag(input.getRecallFlag())
        .recallDate(input.getRecallDate()).build();
  }
}
