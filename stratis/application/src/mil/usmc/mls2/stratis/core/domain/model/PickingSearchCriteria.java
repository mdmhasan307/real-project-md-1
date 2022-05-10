package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PickingSearchCriteria extends BaseSearchCriteria {

  private Integer pid;
  private String pin;
  private Integer wacId;
  private List<String> statuses;
  private Integer assignedUserId;
  private String aac;
  private Integer routeId;
  private String priority;
  private List<String> sides;
  private boolean allowNullAssignedUserIds;
  private String scn;
  private Integer niinLocationId;
  private Integer packingConsolidationId;
}