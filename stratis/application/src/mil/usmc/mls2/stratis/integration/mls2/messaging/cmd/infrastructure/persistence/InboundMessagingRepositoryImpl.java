package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessagingId;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessagingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;

import java.util.Optional;
import java.util.UUID;

@Repository("cmdInboundMessagingRepository")
@RequiredArgsConstructor
@CommonDbTransaction(propagation = Propagation.MANDATORY)
@Profile(Profiles.INTEGRATION_ANY)
public class InboundMessagingRepositoryImpl implements InboundMessagingRepository {

  private final InboundMessagingEntityCmdRepository entityRepository;
  private final InboundMessagingMapper mapper;

  @Override
  public InboundMessagingId nextIdentity() {
    return InboundMessagingId.of(UUID.randomUUID());
  }

  @Override
  public Optional<InboundMessaging> find(InboundMessagingId id) {
    return entityRepository.findById(id.toUuid()).map(mapper::map);
  }

  @Override
  public void save(InboundMessaging messaging) {
    val entity = entityRepository.findById(messaging.id().toUuid()).orElseGet(InboundMessagingEntity::new);
    apply(entity, messaging);
    entityRepository.saveAndFlush(entity);
  }

  @Override
  public void remove(InboundMessagingId id) {
    entityRepository.deleteById(id.toUuid());
  }

  @Override
  public void remove(InboundMessaging messaging) {
    entityRepository.deleteById(messaging.id().toUuid());
  }

  private void apply(InboundMessagingEntity entity, InboundMessaging input) {
    entity.dateProcessed(input.dateProcessed());
    entity.dateReceived(input.dateReceived());
    entity.id(input.id().toUuid());
    entity.payload(input.payload().message());
    entity.payloadClass(input.payload().payloadClass());
    entity.payloadClassVersion(input.payload().payloadClassVersion());
    entity.payloadMessageId(input.payloadMessageId());
    entity.processedCount(input.processedCount());
    entity.receivedCount(input.receivedCount());
    entity.sourceSystemId(input.sourceSystem());
    entity.status(input.status().id());
    entity.statusMessage(input.statusMessage());
    entity.siteIdentifier(input.siteIdentifier());
    entity.type(input.type().id());
  }
}
