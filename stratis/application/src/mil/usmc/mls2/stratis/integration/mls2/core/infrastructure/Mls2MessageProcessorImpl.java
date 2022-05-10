package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.integration.common.Mls2Message;
import mil.usmc.mls2.integration.migs.gcss.r12.outbound.message.GcssR12OutboundMessage;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.CamelMessage;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.utility.JsonUtils;
import mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.command.IncrementMls2InboundMessageReceivedCount;
import mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.routing.IntegrationResources;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.*;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.service.InboundMessagingRepository;
import org.apache.camel.ExchangePattern;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Responsible for processing MLS2 Messages received via MLS2 Integration Services
 * Scope:
 * 1. Persist inbound message
 * 2. Publish inbound message to a queue for further processing
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
@SuppressWarnings("Duplicates")
class Mls2MessageProcessorImpl implements Mls2MessageProcessor {

  private final ApplicationEventPublisher eventPublisher;
  private final InboundMessagingRepository qryInboundMessagingRepository;
  private final mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessagingRepository cmdInboundMessagingRepository;

  @Override
  @CommonDbTransaction(propagation = Propagation.MANDATORY)
  public void process(Mls2Message message) {
    Stream.of(message)
        .map(this::persist)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(this::publish);
  }

  /**
   * Persists Mls2Message as an Mls2InboundMessage.
   * If the mls2 message has previously been processed, the received count is incremented but the id is not returned (to prevent further processing)
   *
   * @param payloadMessage mls2 message
   * @return optional UUID of the persisted Mls2InboundMessage.
   */
  private Optional<InboundMessagingId> persist(Mls2Message payloadMessage) {
    try {
      val existingMessage = qryInboundMessagingRepository.findByPayloadMessageId(payloadMessage.getMeta().getId());
      if (existingMessage.isPresent()) {
        process(IncrementMls2InboundMessageReceivedCount.of(payloadMessage.getMeta().getId()));
        return Optional.empty();
      }

      val messageType = mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessagingType.valueOf(payloadMessage.getMessageType()).orElse(null);
      if (InboundMessagingType.UNKNOWN == messageType) {
        log.warn("Received unexpected mls2 message of class '{}', ignoring message", payloadMessage.getClass().getName());
        return Optional.empty();
      }

      val inboundMessage = InboundMessaging
          .creation()
          .id(InboundMessagingId.of(UUID.randomUUID()))
          .type(messageType)
          .status(InboundMessagingStatus.PENDING)
          .dateReceived(OffsetDateTime.now())
          .sourceSystem(payloadMessage.getMeta().getFromSystemId())
          .payload(InboundMessagingPayload.of(JsonUtils.toJson(payloadMessage), payloadMessage.getClass(), payloadMessage.getVersion()))
          .payloadMessageId(payloadMessage.getMeta().getId())
          .receivedCount(1)
          .processedCount(0)
          .siteIdentifier(getSiteIdentifierOfMessage(payloadMessage))
          .create();

      cmdInboundMessagingRepository.save(inboundMessage);
      return Optional.of(inboundMessage.id());
    }
    catch (Exception e) {
      log.error("Failed to persist mls2 message: {}", payloadMessage, e);
      return Optional.empty();
    }
  }

  private void process(IncrementMls2InboundMessageReceivedCount command) {
    val message = loadMls2InboundMessage(command.getUuid());
    message.recordReceived();
    cmdInboundMessagingRepository.save(message);
  }

  private mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessaging loadMls2InboundMessage(UUID uuid) {
    val id = InboundMessagingId.of(uuid);
    return cmdInboundMessagingRepository.find(id).orElseThrow(() -> new RuntimeException("FAILED TO LOAD MLS2 INBOUND MESSAGE"));
  }

  private void publish(InboundMessagingId inboundMessageId) {
    log.info("Publishing inbound message (id) to local JMS queue for throttled processing: {}", inboundMessageId);
    val camelMessage = CamelMessage.of("publishInboundMessageId", IntegrationResources.DIRECT_MLS2_INBOUND_MESSAGES, ExchangePattern.InOnly, inboundMessageId.toUuid());
    eventPublisher.publishEvent(camelMessage);
    log.info("after publish complete");
  }

  private String getSiteIdentifierOfMessage(Mls2Message payloadMessage) {
    if (payloadMessage instanceof GcssR12OutboundMessage) {
      val message = (GcssR12OutboundMessage) payloadMessage;
      return message.getSiteIdentifier();
    }
    throw new StratisRuntimeException("Unknown Message Type.  Currently only GcssR12Outbound Messages supported.  Unable to retrieve Site Identifier");
  }
}