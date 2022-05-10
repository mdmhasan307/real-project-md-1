package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import mil.usmc.mls2.stratis.common.domain.model.AggregateBase;
import mil.usmc.mls2.stratis.integration.mls2.core.domain.event.OutboundMessagingCreated;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents an MLS2 Outbound Message to be sent via the MLS2 Integration Services
 * <p>
 * destinationSystemId:         intended recipient of the outbound message
 * dateQueued:                  date the outbound message was queued for processing
 * dateSent:                    date the outbound message was sent to the destination system
 * dateProcessed:               date the outbound message was processed
 * dateSent:                    date the outbound message was sent to the destination (occurs after dateProcessed)
 * type:                        message type
 * status:                      message processing status
 * statusMessage:               message processing status comments
 * payload:                     payload object
 * payloadMessageId:            ?
 * relatedInboundMessageId:     inbound message identifier, associated with this outbound message (if any)
 * sentCount:                   number of times this message has been sent to the destination
 * processedCount:              number of times this message has been processed by the outbound message processor
 */
@Getter
@ToString
@Accessors(fluent = true)
@Builder(access = AccessLevel.PRIVATE, builderClassName = "InternalBuilder", builderMethodName = "_builder")
public class OutboundMessaging extends AggregateBase<OutboundMessagingId> {

  @EqualsAndHashCode.Include
  private final OutboundMessagingId id;

  UUID destinationSystemId;
  OffsetDateTime dateQueued;
  OffsetDateTime dateSent;
  OffsetDateTime dateProcessed;
  OutboundMessagingType type;
  OutboundMessagingStatus status;
  String statusMessage;
  OutboundMessagingPayload payload;
  UUID payloadMessageId;
  UUID relatedInboundMessageId;
  int sentCount;
  int processedCount;
  String siteIdentifier;

  @SuppressWarnings("unused")
  @Builder(builderClassName = "CreationBuilder", builderMethodName = "creation", buildMethodName = "create")
  private static OutboundMessaging create(
      @NonNull OutboundMessagingId id,
      //@Nullable OffsetDateTime dateProcessed,
      //@NonNull OffsetDateTime dateQueued,
      @Nullable OffsetDateTime dateSent,
      @Nullable UUID destinationSystem,
      @NonNull OutboundMessagingPayload payload,
      @NonNull UUID payloadMessageId,
      //@NonNull Integer processedCount,
      @Nullable UUID relatedInboundMessageId,
      //@NonNull Integer sentCount,
      @NonNull OutboundMessagingStatus status,
      @Nullable String statusMessage,
      @NonNull OutboundMessagingType type,
      @NonNull String siteIdentifier) {

    val dt = OffsetDateTime.now();

    val messaging = OutboundMessaging
        ._builder()
        .id(id)
        .dateProcessed(dt)
        .dateQueued(dt)
        .dateSent(dateSent)
        .destinationSystemId(destinationSystem)
        .payload(payload)
        .payloadMessageId(payloadMessageId)
        .processedCount(0)
        .relatedInboundMessageId(relatedInboundMessageId)
        .sentCount(0)
        .status(status)
        .statusMessage(statusMessage)
        .type(type)
        .siteIdentifier(siteIdentifier)
        .build();

    val event = OutboundMessagingCreated
        .builder()
        .id(messaging.id())
        .timestamp(messaging.dateProcessed())
        .build();

    messaging.registerEvent(event);

    return messaging;
  }

  @SuppressWarnings("unused")
  @Builder(builderClassName = "RestorationBuilder", builderMethodName = "restoration", buildMethodName = "restore")

  private static OutboundMessaging restore(
      @NonNull OutboundMessagingId id,
      @Nullable OffsetDateTime dateProcessed,
      @NonNull OffsetDateTime dateQueued,
      @Nullable OffsetDateTime dateSent,
      @Nullable UUID destinationSystem,
      @NonNull OutboundMessagingPayload payload,
      @NonNull UUID payloadMessageId,
      @NonNull Integer processedCount,
      @NonNull Integer sentCount,
      @NonNull OutboundMessagingStatus status,
      @Nullable String statusMessage,
      @NonNull OutboundMessagingType type,
      @Nullable UUID relatedInboundMessageId,
      @NonNull String siteIdentifier) {

    return OutboundMessaging
        ._builder()
        .id(id)
        .dateProcessed(dateProcessed)
        .dateQueued(dateQueued)
        .dateSent(dateSent)
        .destinationSystemId(destinationSystem)
        .payload(payload)
        .payloadMessageId(payloadMessageId)
        .processedCount(processedCount)
        .sentCount(sentCount)
        .destinationSystemId(destinationSystem)
        .relatedInboundMessageId(relatedInboundMessageId)
        .status(status)
        .statusMessage(statusMessage)
        .type(type)
        .siteIdentifier(siteIdentifier)
        .build();
  }

  public void recordSent() {
    dateSent = OffsetDateTime.now();
    sentCount++;
  }

  public void linkInboundToOutbound(UUID inboundMessageId) {
    relatedInboundMessageId = inboundMessageId;
  }

  public void changeStatusToProcessed(String statusMessage) {
    this.status = OutboundMessagingStatus.PROCESSED;
    this.statusMessage = statusMessage;
  }

  public void changeStatus(OutboundMessagingStatus status, String statusMessage) {
    this.status = status;
    this.statusMessage = statusMessage;
  }
}