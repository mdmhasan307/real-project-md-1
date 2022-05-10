package mil.usmc.mls2.stratis.core.service.gcss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.integration.common.model.MessageMeta;
import mil.usmc.mls2.integration.migs.gcss.i111.inbound.message.DasfDataRequest;
import mil.usmc.mls2.integration.migs.gcss.i111.outbound.message.DasfNotification;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import mil.usmc.mls2.stratis.core.infrastructure.util.InternalSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.service.common.SettingsSystemService;
import mil.usmc.mls2.stratis.core.utility.JsonUtils;
import mil.usmc.mls2.stratis.integration.mls2.core.service.MessageSupport;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
class I111DasfServiceImpl implements I111DasfService {

  private final StratisConfig multiTenancyConfig;
  private final MessageSupport messageSupport;
  private final SettingsSystemService settingsSystemService;

  @CommonDbTransaction(propagation = Propagation.MANDATORY)
  public void handleDasfNotification(DasfNotification dasfNotification) {
    val site = dasfNotification.getSiteIdentifier().toUpperCase();
    if (multiTenancyConfig.getDatasources().contains(site)) {
      OutboundMessaging outboundMessage = null;
      try {
        InternalSiteSelectionTracker.configureTracker(site);
        val dasfDataRequest = generateDasfDataRequest(dasfNotification.getDasfFeedId());

        val payload = OutboundMessagingPayload.of(JsonUtils.toJson(dasfDataRequest), dasfDataRequest.getClass(), dasfDataRequest.getVersion());

        // ** create MLS2 Outbound Message with EquipmentDetailRequestPayload payload
        outboundMessage = OutboundMessaging
            .creation()
            .destinationSystem(dasfDataRequest.getMeta().getToSystemId())
            .id(OutboundMessagingId.of(UUID.randomUUID()))
            .payload(payload)
            .payloadMessageId(dasfDataRequest.getMeta().getId())
            .status(OutboundMessagingStatus.PROCESSING)
            .type(OutboundMessagingType.I111_DASF_REQUEST)
            .siteIdentifier(site)
            .create();

        messageSupport.processOutboundMessages(outboundMessage);
      }
      catch (Exception e) {
        messageSupport.handleMls2OutboundMessageException(e, outboundMessage, log);
      }
      finally {
        messageSupport.flagOutboundMessageProcessedAndSaveEach(outboundMessage, log);
      }
    }
    else {
      log.info("DASF Feed for site {} ignored.  Site not currently configured.", site);
    }
  }

  private DasfDataRequest generateDasfDataRequest(UUID dasfIdToRequest) {
    val now = OffsetDateTime.now();

    val meta = MessageMeta.builder()
        .id(UUID.randomUUID())
        .fromSystemId(settingsSystemService.getSystemUuid())
        .timestamp(now)
        .build();

    return DasfDataRequest.builder()
        .meta(meta)
        .requestDasfFeedId(dasfIdToRequest)
        .requestDateTime(now)
        .build();
  }
}