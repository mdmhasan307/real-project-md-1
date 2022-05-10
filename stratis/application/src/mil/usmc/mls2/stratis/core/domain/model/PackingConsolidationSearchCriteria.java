package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PackingConsolidationSearchCriteria extends BaseSearchCriteria {

  private Integer packingConsolidationId;
  private Integer packingStationId;
  private Integer level;
  private Integer column;
  private String closeCarton;
  private Integer customerId;
  private String issuePriorityGroup;
  private Boolean nullBarcode;
}