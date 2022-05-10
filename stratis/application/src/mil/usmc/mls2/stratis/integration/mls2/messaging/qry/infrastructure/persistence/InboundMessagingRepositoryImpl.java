package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingCriteria;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingStatus;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.service.InboundMessagingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository("qryInboundMessagingRepository")
@RequiredArgsConstructor
@CommonDbTransaction(propagation = Propagation.MANDATORY)
@Profile(Profiles.INTEGRATION_ANY)
public class InboundMessagingRepositoryImpl implements InboundMessagingRepository {

  private final InboundMessagingEntityRepository entityRepository;
  private final InboundMessagingMapper mapper;

  @Override
  public Optional<InboundMessaging> find(UUID id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public Stream<InboundMessaging> findByMessageStatus(InboundMessagingStatus status) {
    return entityRepository.findByMessageStatus(status).map(mapper::map);
  }

  @Override
  public Optional<InboundMessaging> findByPayloadMessageId(UUID payloadMessageId) {
    return entityRepository.findByPayloadMessageId(payloadMessageId).map(mapper::map);
  }

  @Override
  public long countAll() {
    return entityRepository.count();
  }

  @Override
  public long count(InboundMessagingCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public Stream<InboundMessaging> search(InboundMessagingCriteria criteria) {
    return entityRepository.search(criteria).map(mapper::map);
  }
}