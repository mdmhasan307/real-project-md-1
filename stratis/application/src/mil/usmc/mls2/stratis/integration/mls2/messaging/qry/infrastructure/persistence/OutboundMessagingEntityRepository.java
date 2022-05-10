package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EntityRepository;

import java.util.UUID;

interface OutboundMessagingEntityRepository extends EntityRepository<OutboundMessagingEntity, UUID>, OutboundMessagingEntityRepositoryCustom {
  // Marker interface - intentionally blank
}