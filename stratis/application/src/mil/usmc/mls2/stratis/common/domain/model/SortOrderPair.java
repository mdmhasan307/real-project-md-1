package mil.usmc.mls2.stratis.common.domain.model;

import com.querydsl.core.types.dsl.BeanPath;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SortOrderPair {

  private BeanPath<?> path;
  private String property;
  private SortOrder order;

  public SortOrderPair(String property, SortOrder order, BeanPath<?> path) {
    setProperty(property);
    setOrder(order);
    this.path = path;
  }

  public boolean isOrderAscending() {
    return SortOrder.ASC == order;
  }

  public void setOrder(SortOrder order) {
    this.order = order;
  }

  public void setOrder(String order) {
    this.order = order != null ? (order.toLowerCase().contains("desc") ? SortOrder.DESC : SortOrder.ASC) : SortOrder.ASC;
  }
}
