package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.service;

import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessagingCriteria;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface OutboundMessagingRepository {

  Optional<OutboundMessaging> find(UUID id);

  long countAll();

  long count(OutboundMessagingCriteria criteria);

  Stream<OutboundMessaging> search(OutboundMessagingCriteria criteria);
}