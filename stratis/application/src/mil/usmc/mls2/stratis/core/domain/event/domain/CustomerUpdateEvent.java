package mil.usmc.mls2.stratis.core.domain.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.Customer;
import mil.usmc.mls2.stratis.core.domain.model.Event;
import mil.usmc.mls2.stratis.core.domain.model.EventMeta;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class CustomerUpdateEvent implements Event {

  private static final String LABEL = "Customer Update";

  @Builder.Default
  private final EventMeta meta = EventMeta.of(LABEL);

  @Builder.Default
  private final ZonedDateTime messageTimestamp = ZonedDateTime.now();

  private static CustomerUpdateEventBuilder builder() {
    return new CustomerUpdateEventBuilder();
  }

  public static CustomerUpdateEventBuilder builder(AuditLogTypeEnum result) {
    return new CustomerUpdateEventBuilder().result(result);
  }

  private final UserInfo userInfo;
  private final Customer customer;
  private final String group;
  private final AuditLogTypeEnum result;
}
