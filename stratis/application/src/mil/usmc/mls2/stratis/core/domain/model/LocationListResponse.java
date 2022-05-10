package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Data;
import mil.stratis.common.dm.LocationSelectionOption;

import java.util.List;

@Data
public class LocationListResponse {
  List<LocationSelectionOption> list;
  boolean error;
  String errorMessage;
}
