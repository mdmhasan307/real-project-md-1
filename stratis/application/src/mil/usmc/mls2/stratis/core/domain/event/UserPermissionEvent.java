package mil.usmc.mls2.stratis.core.domain.event;

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
public class UserPermissionEvent implements Event {

  private static final String LABEL = "User group deleted";

  @Builder.Default
  private final EventMeta meta = EventMeta.of(LABEL);

  @Builder.Default
  private final ZonedDateTime messageTimestamp = ZonedDateTime.now();

  private final UserInfo modifyingUser;
  private final String permission;
  private final String modifiedUser;
  private final String message;
  private final UserPermissionEvent.PermissionType type;
  private final UserPermissionEvent.Action action;
  private final AuditLogTypeEnum result;

  public enum PermissionType {
    TYPE,
    GROUP
  }

  public enum Action {
    ADD,
    DELETE,
    ERROR
  }
}
