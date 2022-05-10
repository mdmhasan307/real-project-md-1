package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
public class SerialLotNumTrackSearchCriteria extends BaseSearchCriteria {

  private Integer serialLotNumTrackId;
  private Integer pickPid;
  private String lotConNum;
  private Integer niinId;
  private String serialNumber;
  private String cc;
  private Integer locationId;
  private String issueFlag;
  private Integer notLocationId;
}
