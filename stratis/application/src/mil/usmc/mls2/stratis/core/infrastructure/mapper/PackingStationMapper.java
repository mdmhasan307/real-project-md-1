package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.PackingStation;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PackingStationEntity;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class PackingStationMapper {

  public PackingStation map(PackingStationEntity input) {
    if (input == null) return null;

    return PackingStation.builder()
        .columns(input.getColumns())
        .equipmentNumber(input.getEquipmentNumber())
        .levels(input.getLevels())
        .numberOfSlotsInUse(input.getNumberOfSlotsInUse())
        .packingStationId(input.getPackingStationId())
        .totalIssues(input.getTotalIssues())
        .build();
  }
}