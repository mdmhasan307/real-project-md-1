package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.OutboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.OutboundMessagingId;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.OutboundMessagingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;

import java.util.Optional;
import java.util.UUID;

@Repository("cmdOutboundMessagingRepository")
@RequiredArgsConstructor
@CommonDbTransaction(propagation = Propagation.MANDATORY)
@Profile(Profiles.INTEGRATION_ANY)
public class OutboundMessagingRepositoryImpl implements OutboundMessagingRepository {

  private final OutboundMessagingEntityCmdRepository entityRepository;
  private final OutboundMessagingMapper mapper;

  @Override
  public OutboundMessagingId nextIdentity() {
    return OutboundMessagingId.of(UUID.randomUUID());
  }

  @Override
  public Optional<OutboundMessaging> find(OutboundMessagingId id) {
    return entityRepository.findById(id.toUuid()).map(mapper::map);
  }

  @Override
  public Optional<OutboundMessaging> findByPayloadMessageId(UUID id) {
    return entityRepository.findByPayloadMessageId(id).map(mapper::map);
  }

  @Override
  public void save(OutboundMessaging messaging) {
    val entity = entityRepository.findById(messaging.id().toUuid()).orElseGet(OutboundMessagingEntity::new);
    apply(entity, messaging);
    entityRepository.saveAndFlush(entity);
  }

  @Override
  public void remove(OutboundMessagingId id) {
    entityRepository.deleteById(id.toUuid());
  }

  @Override
  public void remove(OutboundMessaging messaging) {
    entityRepository.deleteById(messaging.id().toUuid());
  }

  private void apply(OutboundMessagingEntity entity, OutboundMessaging input) {
    entity.dateProcessed(input.dateProcessed());
    entity.dateQueued(input.dateQueued());
    entity.dateSent(input.dateSent());
    entity.destinationSystemId(input.destinationSystemId());
    entity.id(input.id().toUuid());
    entity.payload(input.payload().message());
    entity.payloadClass(input.payload().payloadClass());
    entity.payloadClassVersion(input.payload().payloadClassVersion());
    entity.payloadMessageId(input.payloadMessageId());
    entity.processedCount(input.processedCount());
    entity.relatedInboundMessageId(input.relatedInboundMessageId());
    entity.status(input.status().id());
    entity.statusMessage(input.statusMessage());
    entity.sentCount(input.sentCount());
    entity.type(input.type().id());
    entity.siteIdentifier(input.siteIdentifier());
  }
}
