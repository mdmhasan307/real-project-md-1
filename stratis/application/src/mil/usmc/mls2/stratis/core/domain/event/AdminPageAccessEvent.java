package mil.usmc.mls2.stratis.core.domain.event;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.Event;
import mil.usmc.mls2.stratis.core.domain.model.EventMeta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class AdminPageAccessEvent implements Event {

  private static final String LABEL = "Admin Page Access";
  @Builder.Default
  private final EventMeta meta = EventMeta.of(LABEL);

  @Builder.Default
  private final ZonedDateTime messageTimestamp = ZonedDateTime.now();

  private final UserInfo userInfo;
  private final String page;
  private final AuditLogTypeEnum result;
}
