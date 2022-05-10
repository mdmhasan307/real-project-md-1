package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTypeSearchCriteria extends BaseSearchCriteria {

  private String type;
}
