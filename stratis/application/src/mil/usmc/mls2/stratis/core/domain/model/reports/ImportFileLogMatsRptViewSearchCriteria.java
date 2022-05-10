package mil.usmc.mls2.stratis.core.domain.model.reports;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
public class ImportFileLogMatsRptViewSearchCriteria extends BaseSearchCriteria {

  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
}
