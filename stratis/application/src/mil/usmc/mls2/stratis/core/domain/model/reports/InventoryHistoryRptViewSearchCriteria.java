package mil.usmc.mls2.stratis.core.domain.model.reports;

import lombok.*;
import lombok.extern.slf4j.*;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryHistoryRptViewSearchCriteria extends BaseSearchCriteria {

  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private String niin;
  private String wac;
}
