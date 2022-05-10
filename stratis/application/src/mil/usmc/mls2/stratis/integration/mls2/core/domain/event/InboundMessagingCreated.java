package mil.usmc.mls2.stratis.integration.mls2.core.domain.event;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import mil.usmc.mls2.stratis.common.domain.model.Event;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessagingId;

import java.time.OffsetDateTime;

@Value
@Accessors(fluent = true)
@Builder
public class InboundMessagingCreated implements Event {

  @NonNull
  InboundMessagingId id;

  @NonNull
  OffsetDateTime timestamp;
}