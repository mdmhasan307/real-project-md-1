package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingPayload;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingStatus;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class InboundMessagingMapper {

  public InboundMessaging map(InboundMessagingEntity entity) {
    if (entity == null) return null;
    return InboundMessaging
        .builder()
        .id(entity.id())
        .type(InboundMessagingType.valueOf(entity.type()).orElseThrow(() -> new NullPointerException("type")))
        .status(InboundMessagingStatus.valueOf(entity.status()).orElseThrow(() -> new NullPointerException("status")))
        .statusMessage(entity.statusMessage())
        .sourceSystem(entity.sourceSystemId())
        .dateReceived(entity.dateReceived())
        .dateProcessed(entity.dateProcessed())
        .receivedCount(entity.receivedCount())
        .processedCount(entity.processedCount())
        .payloadMessageId(entity.payloadMessageId())
        .payload(InboundMessagingPayload.of(entity.payload(), entity.payloadClass(), entity.payloadClassVersion()))
        .siteIdentifier(entity.siteIdentifier())
        .build();
  }
}