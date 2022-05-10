package mil.usmc.mls2.stratis.core.domain.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.Event;
import mil.usmc.mls2.stratis.core.domain.model.EventMeta;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class WorkstationDeletionEvent implements Event {

  private static final String LABEL = "Workstation Deleted";

  private static final WorkstationDeletionEventBuilder builder() {
    return new WorkstationDeletionEventBuilder();
  }

  public static final WorkstationDeletionEventBuilder builder(AuditLogTypeEnum result) {
    return new WorkstationDeletionEventBuilder().result(result);
  }

  @Builder.Default
  private final EventMeta meta = EventMeta.of(LABEL);

  @Builder.Default
  private final ZonedDateTime messageTimestamp = ZonedDateTime.now();

  private final UserInfo userInfo;
  private final String wacId;
  private final String group;
  private final AuditLogTypeEnum result;
}
