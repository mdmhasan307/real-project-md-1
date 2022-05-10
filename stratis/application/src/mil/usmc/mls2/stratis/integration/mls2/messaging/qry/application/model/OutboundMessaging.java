package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
@Value
@Builder
@Accessors(fluent = true)
public class OutboundMessaging implements Serializable {

  UUID id;
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
}
