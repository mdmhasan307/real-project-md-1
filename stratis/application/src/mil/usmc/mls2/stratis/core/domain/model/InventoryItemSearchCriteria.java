package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder(toBuilder = true)
public class InventoryItemSearchCriteria extends BaseSearchCriteria {

  private Integer inventoryItemId;
  private Integer wacId;
  private Integer locationId;
  private List<String> statuses;
  private Integer assignedUserId;
  private Integer niinId;
  private Integer niinLocationId;
  private List<String> sides;
  private boolean allowNullAssignedUserIds;
  private boolean allowSelfRecounts;
}
