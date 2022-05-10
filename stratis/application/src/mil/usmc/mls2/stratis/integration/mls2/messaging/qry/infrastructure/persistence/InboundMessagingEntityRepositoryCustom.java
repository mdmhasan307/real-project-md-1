package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence;

import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingCriteria;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingStatus;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

interface InboundMessagingEntityRepositoryCustom {

  long count(InboundMessagingCriteria criteria);

  Stream<InboundMessagingEntity> search(InboundMessagingCriteria criteria);

  Stream<InboundMessagingEntity> findByMessageStatus(InboundMessagingStatus status);

  Optional<InboundMessagingEntity> findByPayloadMessageId(UUID payloadMessageId);
}