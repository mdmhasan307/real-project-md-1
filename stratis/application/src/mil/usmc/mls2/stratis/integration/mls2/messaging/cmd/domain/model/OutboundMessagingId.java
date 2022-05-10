package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model;

import lombok.NonNull;
import mil.usmc.mls2.stratis.common.domain.model.UuidEntityId;

import java.util.UUID;

public class OutboundMessagingId extends UuidEntityId {

  private OutboundMessagingId(@NonNull UUID id) { super(id); }

  public static OutboundMessagingId of(@NonNull UUID id) { return new OutboundMessagingId(id); }
}