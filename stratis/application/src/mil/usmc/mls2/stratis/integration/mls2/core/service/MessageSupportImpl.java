package mil.usmc.mls2.stratis.integration.mls2.core.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.integration.common.Mls2Message;
import mil.usmc.mls2.integration.common.model.MessageMeta;
import mil.usmc.mls2.stratis.common.domain.model.CamelMessage;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.CommonTransactionExecutor;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.TransactionExecutorUtils;
import mil.usmc.mls2.stratis.core.service.common.SettingsSystemService;
import mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.routing.IntegrationResources;
import mil.usmc.mls2.stratis.integration.mls2.core.shared.CmdPayloadUtils;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.*;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component("R12MessageSupport")
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
class MessageSupportImpl implements MessageSupport {

  private final InboundMessagingRepository mls2InboundMessageRepository;
  private final OutboundMessagingRepository mls2OutboundMessageRepository;
  private final CommonTransactionExecutor transactionExecutor;
  private final ApplicationEventPublisher eventPublisher;
  private final SettingsSystemService settingsSystemService;
  private final ProducerTemplate camelTemplate;

  @SuppressWarnings("Duplicates")
  @Override
  public void flagInboundMessageProcessedAndSaveEach(InboundMessaging inboundMessage, OutboundMessaging outboundMessage, Logger log) {

    Runnable saveInboundProcess = () -> {
      inboundMessage.recordProcessed();
      mls2InboundMessageRepository.save(inboundMessage);
    };

    transactionExecutor.execute("txSaveInboundMessage", saveInboundProcess, TransactionExecutorUtils.logAndIgnore(log));

    flagOutboundMessageProcessedAndSaveEach(outboundMessage, log);
  }

  @Override
  public void flagOutboundMessageProcessedAndSaveEach(OutboundMessaging outboundMessage, Logger log) {

    Runnable saveOutboundProcess = () -> {
      if (outboundMessage != null) {
        mls2OutboundMessageRepository.save(outboundMessage);
      }
      else {
        log.warn("Outbound message was null, unable to flagOutboundMessageProcessedAndSaved");
      }
    };

    transactionExecutor.execute("txSaveOutboundMessage", saveOutboundProcess, TransactionExecutorUtils.logAndIgnore(log));
  }

  @Override
  public void processOutboundMessages(OutboundMessaging outboundMessage) {
    outboundMessage.recordSent();
    outboundMessage.changeStatusToProcessed("Sent");
    sendToMiddleware(CmdPayloadUtils.toMls2Message(outboundMessage, true));
  }

  @Override
  public void handleMls2InboundMessageException(Exception e, InboundMessaging inboundMessage, Logger log) {
    log.error("mls2 inbound message exception", e);
    inboundMessage.changeStatus(InboundMessagingStatus.EXCEPTION, e.getMessage());
  }

  @Override
  public void handleMls2OutboundMessageException(Exception e, OutboundMessaging outboundMessage, Logger log) {
    log.error("mls2 inbound message exception", e);
    outboundMessage.changeStatus(OutboundMessagingStatus.EXCEPTION, e.getMessage());
  }

  @Override
  public MessageMeta buildMetaMessage(UUID correlationId, UUID toSystemId) {
    return MessageMeta.builder()
        .id(UUID.randomUUID())
        .fromSystemId(settingsSystemService.getSystemUuid())
        .correlationId(correlationId)
        .toSystemId(toSystemId)
        .timestamp(OffsetDateTime.now())
        .build();
  }

  private void sendToMiddleware(Mls2Message mls2Message) {
    // Sends to internal direct DIRECT_MLS2_STRATIS_MIPS queue, which will then be intercepted by IntegrationRouteService.interceptOutbound,
    //   processed, and forwarded to "real" migs to mips queue.
    // Note: ExchangePattern.InOnly indicates one-way event message (not request/reply)

    val eventMessage = CamelMessage.of("sendToMips", IntegrationResources.DIRECT_MLS2_STRATIS_MIPS, ExchangePattern.InOnly, mls2Message);
    eventPublisher.publishEvent(eventMessage);
  }
}