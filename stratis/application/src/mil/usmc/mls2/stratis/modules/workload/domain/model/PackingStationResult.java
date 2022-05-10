package mil.usmc.mls2.stratis.modules.workload.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
public class PackingStationResult {

  PackingConsolidationInfo packingConsolidationInfo;
  String packingStationName; //weird Error - blah stuff

  public static PackingStationResult ofName(String name) {
    return PackingStationResult.builder()
        .packingStationName(name)
        .build();
  }

  public static PackingStationResult of(String name, PackingConsolidationInfo info) {
    return PackingStationResult.builder()
        .packingStationName(name)
        .packingConsolidationInfo(info)
        .build();
  }
}
