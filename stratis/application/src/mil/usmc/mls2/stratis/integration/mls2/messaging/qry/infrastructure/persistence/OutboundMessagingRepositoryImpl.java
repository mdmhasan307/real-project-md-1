package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessagingCriteria;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.service.OutboundMessagingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository("qryOutboundMessagingRepository")
@RequiredArgsConstructor
@CommonDbTransaction(propagation = Propagation.MANDATORY)
@Profile(Profiles.INTEGRATION_ANY)
public class OutboundMessagingRepositoryImpl implements OutboundMessagingRepository {

  private final OutboundMessagingEntityRepository entityRepository;
  private final OutboundMessagingMapper mapper;

  @Override
  public Optional<OutboundMessaging> find(UUID id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public long countAll() {
    return entityRepository.count();
  }

  @Override
  public long count(OutboundMessagingCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public Stream<OutboundMessaging> search(OutboundMessagingCriteria criteria) {
    return entityRepository.search(criteria).map(mapper::map);
  }
}