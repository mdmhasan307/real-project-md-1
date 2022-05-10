package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.service;

import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingCriteria;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingStatus;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface InboundMessagingRepository {

  Optional<InboundMessaging> find(UUID id);

  long countAll();

  long count(InboundMessagingCriteria criteria);

  Stream<InboundMessaging> search(InboundMessagingCriteria criteria);

  Stream<InboundMessaging> findByMessageStatus(InboundMessagingStatus status);

  Optional<InboundMessaging> findByPayloadMessageId(UUID payloadMessageId);
}