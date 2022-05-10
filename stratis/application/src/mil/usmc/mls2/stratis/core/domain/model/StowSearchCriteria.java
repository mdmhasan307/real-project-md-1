package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StowSearchCriteria extends BaseSearchCriteria {

  private Receipt receipt;
  private Integer stowId;
  private Integer wacId;
  private Integer pid;
  private String sid;
  private Integer assignedUserId;
  private String scanInd;
  private List<String> statuses;
  private List<String> sides;
  private String serialNumber;

  public static StowSearchCriteria openStowsForUser(Wac wac) {

    return StowSearchCriteria.builder()
        .wacId(wac.getWacId())
        .scanInd("Y")
        .statuses(Arrays.asList("STOW READY", "STOW BYPASS1"))
        .build();
  }
}
