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
@Builder
public class SpoolSearchCriteria extends BaseSearchCriteria {

  private Integer spoolId;
  private List<String> statusList;
  private String spoolDefMode;
  private boolean allowNullXml;
}
