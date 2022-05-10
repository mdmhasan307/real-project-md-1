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
public class CreateNiinEvent implements Event {

  private static final String LABEL = "NIIN Created";

  @Builder.Default
  private final EventMeta meta = EventMeta.of(LABEL);

  @Builder.Default
  private final ZonedDateTime messageTimestamp = ZonedDateTime.now();

  private static CreateNiinEventBuilder builder() {
    return new CreateNiinEventBuilder();
  }

  public static CreateNiinEventBuilder builder(AuditLogTypeEnum result) {
    return new CreateNiinEventBuilder().result(result);
  }

  private final UserInfo userInfo;
  private final String niin;
  private final String fsc;
  private final AuditLogTypeEnum result;
}
