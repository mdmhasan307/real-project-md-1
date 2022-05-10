package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.*;
import org.springframework.stereotype.Component;

@Component("cmdInboundMessagingMapper")
@RequiredArgsConstructor
class InboundMessagingMapper {

  public InboundMessaging map(InboundMessagingEntity entity) {
    if (entity == null) return null;
    return InboundMessaging
        .restoration()
        .id(InboundMessagingId.of(entity.id()))
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
        .restore();
  }
}
