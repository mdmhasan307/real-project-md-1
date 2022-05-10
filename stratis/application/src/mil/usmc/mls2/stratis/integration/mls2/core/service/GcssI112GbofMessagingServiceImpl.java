package mil.usmc.mls2.stratis.integration.mls2.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.integration.migs.gcss.i112.outbound.message.GbofDataResponse;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsData;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.DefaultTransactionExecutor;
import mil.usmc.mls2.stratis.core.infrastructure.util.InternalSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.service.GcssMcImportsDataService;
import mil.usmc.mls2.stratis.integration.mls2.core.shared.CmdPayloadUtils;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessagingStatus;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.OutboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.OutboundMessagingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
@SuppressWarnings("Duplicates")
public class GcssI112GbofMessagingServiceImpl implements GcssI112GbofMessagingService {

  private static final String GBOF_INTERFACE_NAME = "GBOF";

  private final GcssMcImportsDataService gcssMcImportsDataService;
  private final OutboundMessagingRepository cmdInboundMessagingRepository;
  private final MessageSupport messageSupport;
  private final DefaultTransactionExecutor defaultTransactionExecutor;

  @Override
  public void processMessage(InboundMessaging inboundMessage) {
    OutboundMessaging relatedOutboundMessage = null;
    try {
      val gbofDataResponse = CmdPayloadUtils.toMls2Message(inboundMessage, GbofDataResponse.class, true);
      InternalSiteSelectionTracker.configureTracker(gbofDataResponse.getSiteIdentifier());
      //      val requestDasfFeedId = dasfDataResponse.getDasfFeedId(); // do we need this?

      // We want to push this XML into GCSSMC_IMPORTS_DATA
      val feedXml = gbofDataResponse.getFeedXml();

      // Store new XML data in DB... then... does anything else need done at this point?  Eventually we'll want to store the data objects.
      val gcssMcImportsData = GcssMcImportsData.builder()
          .interfaceName(GBOF_INTERFACE_NAME)
          .createdBy(1)
          .createdDate(OffsetDateTime.now())
          .status("READY")
          .xml(feedXml)
          .build();

      defaultTransactionExecutor.execute("SaveGcssMcImportsData", () ->
          gcssMcImportsDataService.save(gcssMcImportsData));

      val correlationId = gbofDataResponse.getMeta().getCorrelationId();
      relatedOutboundMessage = cmdInboundMessagingRepository.findByPayloadMessageId(correlationId).orElseThrow(() -> new StratisRuntimeException(String.format("MLS2 Outbound message with id '%s' not found for MLS2 Inbound Message '%s'", correlationId, inboundMessage.payloadMessageId())));
      relatedOutboundMessage.linkInboundToOutbound(inboundMessage.id().toUuid());
      relatedOutboundMessage.changeStatusToProcessed("processed");

      inboundMessage.changeStatus(InboundMessagingStatus.PROCESSED, "processed");
    }
    catch (Exception e) {
      messageSupport.handleMls2InboundMessageException(e, inboundMessage, log);
    }
    finally {
      messageSupport.flagInboundMessageProcessedAndSaveEach(inboundMessage, relatedOutboundMessage, log);
    }
  }
}