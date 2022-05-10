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
public class FloorLocation {

  private int floorLocationId;
  private String floorLocation;
  private Route route;
  private String inUse;
  private Warehouse warehouse;
}
