package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence;

import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessagingCriteria;

import java.util.stream.Stream;

interface OutboundMessagingEntityRepositoryCustom {

  long count(OutboundMessagingCriteria criteria);

  Stream<OutboundMessagingEntity> search(OutboundMessagingCriteria criteria);
}