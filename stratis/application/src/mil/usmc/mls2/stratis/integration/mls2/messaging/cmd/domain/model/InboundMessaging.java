package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import mil.usmc.mls2.stratis.common.domain.model.AggregateBase;
import mil.usmc.mls2.stratis.integration.mls2.core.domain.event.InboundMessagingCreated;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@ToString
@Accessors(fluent = true)
@Builder(access = AccessLevel.PRIVATE, builderClassName = "InternalBuilder", builderMethodName = "_builder")
public class InboundMessaging extends AggregateBase<InboundMessagingId> {

  @EqualsAndHashCode.Include
  private final InboundMessagingId id;

  UUID sourceSystem;
  UUID destinationSystem;
  InboundMessagingType type;
  InboundMessagingStatus status;
  String statusMessage;
  int receivedCount;
  int processedCount;
  UUID payloadMessageId;
  InboundMessagingPayload payload;
  OffsetDateTime dateReceived;
  OffsetDateTime dateProcessed;
  String siteIdentifier;

  @SuppressWarnings("unused")
  @Builder(builderClassName = "CreationBuilder", builderMethodName = "creation", buildMethodName = "create")
  private static InboundMessaging create(
      @NonNull InboundMessagingId id,
      @Nullable OffsetDateTime dateProcessed,
      @NonNull OffsetDateTime dateReceived,
      @Nullable UUID destinationSystem,
      @NonNull InboundMessagingPayload payload,
      @NonNull UUID payloadMessageId,
      @NonNull Integer processedCount,
      @NonNull Integer receivedCount,
      @NonNull UUID sourceSystem,
      @NonNull InboundMessagingStatus status,
      @Nullable String statusMessage,
      @NonNull InboundMessagingType type,
      @NonNull String siteIdentifier) {

    val messaging = InboundMessaging
        ._builder()
        .id(id)
        .dateProcessed(dateProcessed)
        .dateReceived(dateReceived)
        .destinationSystem(destinationSystem)
        .payload(payload)
        .payloadMessageId(payloadMessageId)
        .processedCount(processedCount)
        .receivedCount(receivedCount)
        .sourceSystem(sourceSystem)
        .status(status)
        .statusMessage(statusMessage)
        .type(type)
        .siteIdentifier(siteIdentifier)
        .build();

    val event = InboundMessagingCreated
        .builder()
        .id(messaging.id())
        .timestamp(messaging.dateReceived())
        .build();

    messaging.registerEvent(event);

    return messaging;
  }

  @SuppressWarnings("unused")
  @Builder(builderClassName = "RestorationBuilder", builderMethodName = "restoration", buildMethodName = "restore")

  private static InboundMessaging restore(
      @NonNull InboundMessagingId id,
      @Nullable OffsetDateTime dateProcessed,
      @NonNull OffsetDateTime dateReceived,
      @Nullable UUID destinationSystem,
      @NonNull InboundMessagingPayload payload,
      @NonNull UUID payloadMessageId,
      @NonNull Integer processedCount,
      @NonNull Integer receivedCount,
      @NonNull UUID sourceSystem,
      @NonNull InboundMessagingStatus status,
      @Nullable String statusMessage,
      @NonNull InboundMessagingType type,
      @NonNull String siteIdentifier) {

    return InboundMessaging
        ._builder()
        .id(id)
        .dateProcessed(dateProcessed)
        .dateReceived(dateReceived)
        .destinationSystem(destinationSystem)
        .payload(payload)
        .payloadMessageId(payloadMessageId)
        .processedCount(processedCount)
        .receivedCount(receivedCount)
        .sourceSystem(sourceSystem)
        .status(status)
        .statusMessage(statusMessage)
        .type(type)
        .siteIdentifier(siteIdentifier)
        .build();
  }

  public void recordReceived() {
    this.dateReceived = OffsetDateTime.now();
    this.receivedCount++;
  }

  public void recordProcessed() {
    this.dateProcessed = OffsetDateTime.now();
    this.processedCount++;
  }

  public void changeStatus(InboundMessagingStatus status, String statusMessage) {
    this.status = status;
    this.statusMessage = statusMessage;
  }
}