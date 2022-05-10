package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.listener;

import lombok.*;
import lombok.extern.slf4j.*;
import mil.usmc.mls2.integration.common.model.MessageMeta;
import mil.usmc.mls2.integration.stratis.outbound.message.StratisAudit;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.service.common.SettingsSystemService;
import mil.usmc.mls2.stratis.core.utility.JsonUtils;
import mil.usmc.mls2.stratis.integration.mls2.core.domain.event.AuditCreatedEvent;
import mil.usmc.mls2.stratis.integration.mls2.core.service.MessageSupport;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.Serializable;
import java.util.*;

@Slf4j
@Component("integrationAuditEventListener")
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
class AuditEventListener {

  private final MessageSupport messageSupport;
  private final SettingsSystemService settingsSystemService;

  /**
   * Transactional event processor (only respond to events from successful transactions)
   * Convert STRATIS Audit Message to an MLS2 Audit Message and add to queue
   */
  @Async
  @TransactionalEventListener
  public void handle(AuditCreatedEvent event) {
    //close/finalize transaction
    OutboundMessaging outboundMessage = null;
    try {
      log.info("*** AuditEventListener handle event on Thread '{}'", Thread.currentThread().getName());

      val auditLog = event.getAuditLog();
      val params = new HashMap<String, Serializable>();
      params.put("username", auditLog.getUsername());
      params.put("source", auditLog.getSource() != null ? auditLog.getSource().name() : null);
      params.put("category", auditLog.getCategory() != null ? auditLog.getCategory().name() : null);
      params.put("type", auditLog.getType() != null ? auditLog.getType().name() : null);

      MessageMeta meta = MessageMeta.builder()
          .id(UUID.randomUUID())
          .fromSystemId(settingsSystemService.getSystemUuid())
          .timestamp(event.getMeta().getTimestamp())
          .build();

      StratisAudit message = StratisAudit.builder()
          .meta(meta)
          .message(auditLog.getDescription())
          .parameters(params)
          .build();

      val payload = OutboundMessagingPayload.of(JsonUtils.toJson(message), message.getClass(), message.getVersion());

      outboundMessage = OutboundMessaging
          .creation()
          .id(OutboundMessagingId.of(UUID.randomUUID()))
          .payload(payload)
          .payloadMessageId(message.getMeta().getId())
          .status(OutboundMessagingStatus.PROCESSING)
          .type(OutboundMessagingType.STRATIS_AUDIT)
          .siteIdentifier(event.getSiteIdentifier())
          .create();

      messageSupport.processOutboundMessages(outboundMessage);
    }
    catch (Exception e) {
      log.error("Error found during Processing of Outbound Audit message", e);
    }
    finally {
      messageSupport.flagOutboundMessageProcessedAndSaveEach(outboundMessage, log);
    }
  }
}