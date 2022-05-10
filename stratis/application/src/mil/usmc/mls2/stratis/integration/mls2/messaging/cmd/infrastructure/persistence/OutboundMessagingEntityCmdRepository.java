package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.infrastructure.persistence;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EntityRepository;

import java.util.Optional;
import java.util.UUID;

interface OutboundMessagingEntityCmdRepository extends EntityRepository<OutboundMessagingEntity, UUID>, OutboundMessagingEntityCmdRepositoryCustom {

  Optional<OutboundMessagingEntity> findByPayloadMessageId(UUID id);
}
