package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model;

import com.querydsl.core.types.dsl.BeanPath;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor(staticName = "of")
@Accessors(fluent = true)
public class SortOrderPair {

  BeanPath<?> path;
  String property;
  SortOrder order;

  public boolean isAscending() {
    return SortOrder.ASC == order;
  }
}