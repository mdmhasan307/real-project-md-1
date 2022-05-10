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
@Builder(toBuilder = true)
public class GcssMcImportsDataSearchCriteria extends BaseSearchCriteria {

  private Integer id;
  private String interfaceName;
  private List<String> statuses;
  private List<String> statusesNotEqual;
  private OffsetDateTime createdBefore;
}