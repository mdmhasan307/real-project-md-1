package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RequiredArgsConstructor
@CommonDbTransaction(propagation = Propagation.MANDATORY)
class OutboundMessagingEntityCmdRepositoryCustomImpl implements OutboundMessagingEntityCmdRepositoryCustom {

  @PersistenceContext(unitName = GlobalConstants.COMMON_PERSISTENCE_UNIT)
  private EntityManager entityManager;
}