package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessagingPayload;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessagingStatus;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessagingType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class OutboundMessagingMapper {

  public OutboundMessaging map(OutboundMessagingEntity entity) {
    if (entity == null) return null;
    return OutboundMessaging.builder()
        .id(entity.id())
        .destinationSystemId(entity.destinationSystemId())
        .sentCount(entity.sentCount())
        .processedCount(entity.processedCount())
        .dateQueued(entity.dateQueued())
        .dateSent(entity.dateSent())
        .dateProcessed(entity.dateProcessed())
        .type(OutboundMessagingType.valueOf(entity.type()).orElseThrow(() -> new IllegalStateException("undefined outbound message type")))
        .status(OutboundMessagingStatus.valueOf(entity.status()).orElseThrow(() -> new IllegalStateException("undefined outbound message status")))
        .statusMessage(entity.statusMessage())
        .payload(OutboundMessagingPayload.of(entity.payload(), entity.payloadClass(), entity.payloadClassVersion()))
        .payloadMessageId(entity.payloadMessageId())
        .relatedInboundMessageId(entity.relatedInboundMessageId())
        .siteIdentifier(entity.siteIdentifier())
        .build();
  }
}