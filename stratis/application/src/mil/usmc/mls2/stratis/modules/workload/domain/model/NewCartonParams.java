package mil.usmc.mls2.stratis.modules.workload.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
public class NewCartonParams {

  int stationId;
  int stationLevels;
  int stationColumns;

  PackingConsolidationInfo lastPackedItem;
}