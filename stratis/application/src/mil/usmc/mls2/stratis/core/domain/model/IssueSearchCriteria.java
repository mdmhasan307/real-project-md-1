package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
public class IssueSearchCriteria extends BaseSearchCriteria {

  private Integer wacId;
  private String scn;
  private List<String> scns;
  private String status;
  private OffsetDateTime createdBefore;
  private String issueType;
  private String documentNumber;
  private String suffix;
  private boolean allowNullIssueType;
}
