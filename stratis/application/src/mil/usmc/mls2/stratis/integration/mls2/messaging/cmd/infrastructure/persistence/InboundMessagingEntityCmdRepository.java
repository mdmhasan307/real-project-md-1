package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.infrastructure.persistence;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EntityRepository;

import java.util.UUID;

interface InboundMessagingEntityCmdRepository extends EntityRepository<InboundMessagingEntity, UUID>, InboundMessagingEntityCmdRepositoryCustom {
  
}
