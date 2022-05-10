package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model;

import java.util.Optional;
import java.util.UUID;

public interface OutboundMessagingRepository {

  OutboundMessagingId nextIdentity();

  Optional<OutboundMessaging> find(OutboundMessagingId id);

  Optional<OutboundMessaging> findByPayloadMessageId(UUID id);

  void save(OutboundMessaging messaging);

  void remove(OutboundMessagingId id);

  void remove(OutboundMessaging messaging);
}