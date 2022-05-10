package mil.usmc.mls2.stratis.modules.workload.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
public class PackingConsolidationInfo {

  boolean singleUseStation;
  int packingConsolidationId;
  int packingStationId;
  int level;
  int column;

  public static PackingConsolidationInfo empty() {
    return PackingConsolidationInfo.builder()
        .packingConsolidationId(-1)
        .packingStationId(-1)
        .level(-1)
        .column(-1)
        .singleUseStation(true)
        .build();
  }

  public boolean isEmpty() {
    return packingConsolidationId == -1;
  }
}
