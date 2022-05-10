package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@Accessors(fluent = true)
public class InboundMessaging {

  UUID id;
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

  OffsetDateTime dateReceivedStart;
  OffsetDateTime dateReceivedEnd;

  OffsetDateTime dateProcessedStart;
  OffsetDateTime dateProcessedEnd;
  String siteIdentifier;
}