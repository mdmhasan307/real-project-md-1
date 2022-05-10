package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.integration.common.Mls2Message;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.common.share.CamelContextReadyEvent;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.Mls2InboundMessageProcessor;
import mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.Mls2MessageProcessor;
import mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.Mls2NotificationProcessor;
import mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.routing.IntegrationResources;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.UUID;

/**
 * IntegrationRouteService services the IntegrationRouteBuilder.  As Camel does not use the proxied spring object, transaction annotations
 * are ignore.  As a result this service delegates its work to the TransactionCommands object.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
@SuppressWarnings("unused")
class IntegrationRouteServiceImpl implements IntegrationRouteService {

  private final TransactionCommands transactionCommands;
  private final ProducerTemplate camelTemplate;

  @Override
  public void processMls2Message(Mls2Message message, Exchange exchange) {
    transactionCommands.processMls2Message(message, exchange);
  }

  @Override
  public void processTest(Object message, Exchange exchange) {
    log.info("received test message: {}", message);
  }

  @Override
  public void processMls2InboundMessage(UUID inboundMessageId) {
    transactionCommands.processMls2InboundMessage(inboundMessageId);
  }

  @Override
  public void interceptOutbound(Mls2Message message, Exchange exchange) {
    transactionCommands.interceptOutbound(message, exchange);
  }

  @Override
  public void processMigsNotification(Mls2Message message, Exchange exchange) {
    transactionCommands.processMigsNotification(message, exchange);
  }

  @EventListener
  @SuppressWarnings("unused")
  public void handle(CamelContextReadyEvent event) {
    log.info("*** THE CAMEL CONTEXT is ready, testing integration routing...");
    camelTemplate.sendBody(IntegrationResources.DIRECT_STRATIS_INTERNAL_TEST, ExchangePattern.InOnly, "STRATIS Integration configuration startup message");
  }

  @Slf4j
  @Component
  @RequiredArgsConstructor
  @CommonDbTransaction(propagation = Propagation.REQUIRES_NEW)
  @Profile(Profiles.INTEGRATION_ANY)
  static class TransactionCommands {

    private final Mls2NotificationProcessor mls2NotificationProcessor;

    private final Mls2InboundMessageProcessor mls2InboundMessageProcessor;
    private final Mls2MessageProcessor mls2MessageProcessor;

    public void processMigsNotification(Mls2Message message, Exchange exchange) {
      // Note headers are not *currently* being set on new data notifications sent from MIGS
      log.info("RECEIVED MIGS NEW DATA NOTIFICATION: mls2 headers [mls2_messageType: {}]", message.getMessageType().getLabel());
      mls2NotificationProcessor.processMigsNotification(message);
    }

    public void processMls2Message(Mls2Message message, Exchange exchange) {
      log.info("RECEIVED MLS2 MESSAGE: mls2 headers [mls2_messageId: {}, mls2_systemId: {}, mls2_messageType: {}]", exchange.getIn().getHeader("mls2_messageId"), exchange.getIn().getHeader("mls2_systemId"), exchange.getIn().getHeader("mls2_messageType"));
      mls2MessageProcessor.process(message);
    }

    public void processMls2InboundMessage(UUID inboundMessageId) {
      log.info("RECEIVED Mls2InboundMessage (uuid) for processing: {}", inboundMessageId);
      mls2InboundMessageProcessor.process(inboundMessageId);
    }

    public void interceptOutbound(Mls2Message message, Exchange exchange) {
      if (message != null) {
        exchange.getIn().setHeader("mls2_messageId", message.getMeta().getId().toString());
        exchange.getIn().setHeader("mls2_systemId", message.getMeta().getFromSystemId().toString());
        exchange.getIn().setHeader("mls2_messageType", message.getClass().getName());
      }
    }
  }
}