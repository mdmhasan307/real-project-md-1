package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Location;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.LocationEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class LocationMapper {

  private final LocationMapper locationMapper;
  private final WacMapper wacMapper;

  public LocationMapper(@Lazy LocationMapper locationMapper, @Lazy WacMapper wacMapper) {
    this.locationMapper = locationMapper;
    this.wacMapper = wacMapper;
  }

  public Location map(LocationEntity input) {
    if (input == null) return null;

    return Location.builder()
        .aisle(input.getAisle())
        .availabilityFlag(input.getAvailabilityFlag())
        .bay(input.getBay())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .cube(input.getCube())
        .dividerIndex(input.getDividerIndex())
        .lastStowDate(input.getLastStowDate())
        .locationHeaderBinId(input.getLocationHeaderBinId())
        .locationId(input.getLocationId())
        .locationLabel(input.getLocationLabel())
        .locClassificationId(input.getLocClassificationId())
        .locLevel(input.getLocLevel())
        .mechanizedFlag(input.getMechanizedFlag())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .side(input.getSide())
        .slot(input.getSlot())
        .wac(wacMapper.map(input.getWac()))
        .weight(input.getWeight())
        .build();
  }
}