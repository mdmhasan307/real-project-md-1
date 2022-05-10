package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model;

import java.util.Optional;

public interface InboundMessagingRepository {

  InboundMessagingId nextIdentity();

  Optional<InboundMessaging> find(InboundMessagingId id);

  void save(InboundMessaging messaging);

  void remove(InboundMessagingId id);

  void remove(InboundMessaging messaging);
}