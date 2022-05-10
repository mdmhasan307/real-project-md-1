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
public class InvSerialLotNumSearchCriteria extends BaseSearchCriteria {

  private Integer invSerialLotNumId;
  private Integer pickPid;
  private String lotConNum;
  private Integer niinId;
  private String serialNumber;
  private String cc;
  private Integer locationId;
  private Integer notThisLocationId;
  private Integer inventoryItemId;
}
