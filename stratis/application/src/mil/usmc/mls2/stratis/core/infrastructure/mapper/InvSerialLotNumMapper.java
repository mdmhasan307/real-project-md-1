package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.InvSerialLotNum;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.InvSerialLotNumEntity;
import org.springframework.stereotype.Component;

@Component
public class InvSerialLotNumMapper {

  public InvSerialLotNum map(InvSerialLotNumEntity input) {
    if (input == null) return null;

    return InvSerialLotNum.builder()
        .invSerialLotNumId(input.getInvSerialLotNumId())
        .niinId(input.getNiinId())
        .serialNumber(input.getSerialNumber())
        .lotConNum(input.getLotConNum())
        .cc(input.getCc())
        .expirationDate(input.getExpirationDate())
        .timestamp(input.getTimestamp())
        .qty(input.getQty())
        .locationId(input.getLocationId())
        .invDoneFlag(input.getInvDoneFlag())
        .inventoryItemId(input.getInventoryItemId())
        .build();
  }
}
