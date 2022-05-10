package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackingStation {

  private Integer packingStationId;
  private Integer levels;
  private Integer columns;
  private Integer totalIssues;
  private Integer numberOfSlotsInUse;
  private Integer equipmentNumber;
}
