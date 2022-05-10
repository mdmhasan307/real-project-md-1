package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.common.domain.model.CamelMessage;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.routing.IntegrationResources;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingStatus;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.service.InboundMessagingRepository;
import org.apache.camel.ExchangePattern;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
class Mls2InboundMessageEventListener {

  private final TransactionCommands transactionCommands;

  @EventListener
  @SuppressWarnings("unused")
  public void onApplicationReady(ApplicationReadyEvent event) {
    transactionCommands.onApplicationReady();
  }

  @Slf4j
  @Component
  @RequiredArgsConstructor
  @CommonDbTransaction(propagation = Propagation.REQUIRES_NEW)
  @Profile(Profiles.INTEGRATION_ANY)
  static class TransactionCommands {

    private final ApplicationEventPublisher eventPublisher;
    private final InboundMessagingRepository qryInboundMessagingRepository;

    public void onApplicationReady() {
      log.info("Mls2InboundMessageService: publishing pending mls2 inbound messages for processing...");
      log.info("Transaction Name {}", TransactionSynchronizationManager.getCurrentTransactionName());
      val pendingMessages = qryInboundMessagingRepository.findByMessageStatus(InboundMessagingStatus.PENDING);
      pendingMessages.forEach(pm -> {
        // must push to queue in order for message to be re-processed
        val camelMessage = CamelMessage.of("republishPendingInboundMessage", IntegrationResources.DIRECT_MLS2_INBOUND_MESSAGES, ExchangePattern.InOnly, pm.id());
        eventPublisher.publishEvent(camelMessage);
      });
    }
  }
}
