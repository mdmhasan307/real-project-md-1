package mil.usmc.mls2.stratis.core.service.gcss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.integration.common.model.MessageMeta;
import mil.usmc.mls2.integration.migs.gcss.i112.inbound.message.GbofDataRequest;
import mil.usmc.mls2.integration.migs.gcss.i112.outbound.message.GbofNotification;
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
class I112GbofServiceImpl implements I112GbofService {

  private final StratisConfig multiTenancyConfig;
  private final MessageSupport messageSupport;
  private final SettingsSystemService settingsSystemService;

  @CommonDbTransaction(propagation = Propagation.MANDATORY)
  public void handleGbofNotification(GbofNotification gbofNotification) {
    val site = gbofNotification.getSiteIdentifier().toUpperCase();
    if (multiTenancyConfig.getDatasources().contains(site)) {
      OutboundMessaging outboundMessage = null;
      try {
        InternalSiteSelectionTracker.configureTracker(site);
        val gbofDataRequest = generateGbofDataRequest(gbofNotification.getGbofFeedId());

        val payload = OutboundMessagingPayload.of(JsonUtils.toJson(gbofDataRequest), gbofDataRequest.getClass(), gbofDataRequest.getVersion());

        outboundMessage = OutboundMessaging
            .creation()
            .destinationSystem(gbofDataRequest.getMeta().getToSystemId())
            .id(OutboundMessagingId.of(UUID.randomUUID()))
            .payload(payload)
            .payloadMessageId(gbofDataRequest.getMeta().getId())
            .status(OutboundMessagingStatus.PROCESSING)
            .type(OutboundMessagingType.I112_GBOF_REQUEST)
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
      log.info("MATS Feed for site {} ignored.  Site not currently configured.", site);
    }
  }

  private GbofDataRequest generateGbofDataRequest(UUID gbofIdToRequest) {
    val now = OffsetDateTime.now();

    val meta = MessageMeta.builder()
        .id(UUID.randomUUID())
        .fromSystemId(settingsSystemService.getSystemUuid())
        .timestamp(now)
        .build();

    return GbofDataRequest.builder()
        .meta(meta)
        .requestGbofFeedId(gbofIdToRequest)
        .requestDateTime(now)
        .build();
  }
}