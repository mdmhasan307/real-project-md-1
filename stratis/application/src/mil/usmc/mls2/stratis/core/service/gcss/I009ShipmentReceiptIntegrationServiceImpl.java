package mil.usmc.mls2.stratis.core.service.gcss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.integration.common.model.MessageMeta;
import mil.usmc.mls2.integration.migs.gcss.i009.inbound.message.ShipmentReceipt;
import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLog;
import mil.usmc.mls2.stratis.core.domain.model.Spool;
import mil.usmc.mls2.stratis.core.domain.model.SpoolSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import mil.usmc.mls2.stratis.core.infrastructure.util.InternalSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.service.RefDataloadLogService;
import mil.usmc.mls2.stratis.core.service.SiteInfoService;
import mil.usmc.mls2.stratis.core.service.SpoolService;
import mil.usmc.mls2.stratis.core.service.common.SettingsSystemService;
import mil.usmc.mls2.stratis.core.utility.JsonUtils;
import mil.usmc.mls2.stratis.integration.mls2.core.service.MessageSupport;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
class I009ShipmentReceiptIntegrationServiceImpl implements I009ShipmentReceiptService {

  private final SpoolService spoolService;
  private final SiteInfoService siteInfoService;
  private final MessageSupport messageSupport;
  private final StratisConfig multiTenancyConfig;
  private final SettingsSystemService settingsSystemService;
  private final RefDataloadLogService refDataloadLogService;

  @Override
  public void processShipmentReceipt() {
    multiTenancyConfig.getDatasources().forEach(dataSource -> {
      val start = OffsetDateTime.now();
      InternalSiteSelectionTracker.configureTracker(dataSource);

      val siteInfo = siteInfoService.getRecord();

      //FUTURE (INNOV Backlog) should integration mode care if the stratis instance things GCSS is up and online, or should it always just push to migs?
      if (!"Y".equalsIgnoreCase(siteInfo.getInterfacesOn()) || !"Y".equalsIgnoreCase(siteInfo.getGcssMc())) {
        log.info("Interfaces are not or not GCSS_MC on in site info.  I009ShipmentReceiptIntegrationServiceImpl  Job skipping.");
        return;
      }

      val spoolRecord = getSpoolToProcess();
      if (spoolRecord.isPresent()) {
        val spool = spoolRecord.get();
        spool.updateToInProgress();
        spoolService.save(spool);

        //generate outbound message to MIPS/MIGS
        sendSpoolToMips(spool.getXml(), dataSource);
        spool.updateToComplete(spoolService.getSpoolBatchNumber());
        spoolService.save(spool);
        val end = OffsetDateTime.now();
        saveDataloadLog("Success send", String.format("Job took %n seconds", ChronoUnit.SECONDS.between(start, end)), 0);
      }
    });
  }

  private Optional<Spool> getSpoolToProcess() {
    List<String> statuses = new ArrayList<>();
    statuses.add("READY");
    statuses.add("ERROR");
    statuses.add("INPROGRESS");

    val criteria = SpoolSearchCriteria.builder()
        .spoolDefMode("G")
        .statusList(statuses)
        .build();
    criteria.setSortColumn("spoolId");
    return spoolService.searchSingle(criteria);
  }

  public void sendSpoolToMips(String xml, String siteIdentifier) {
    OutboundMessaging outboundMessage = null;
    try {
      val shipmentReceipt = generateShipmentReceipt(xml, siteIdentifier);

      val payload = OutboundMessagingPayload.of(JsonUtils.toJson(shipmentReceipt), shipmentReceipt.getClass(), shipmentReceipt.getVersion());

      // ** create MLS2 Outbound Message with EquipmentDetailRequestPayload payload
      outboundMessage = OutboundMessaging
          .creation()
          .destinationSystem(shipmentReceipt.getMeta().getToSystemId())
          .id(OutboundMessagingId.of(UUID.randomUUID()))
          .payload(payload)
          .payloadMessageId(shipmentReceipt.getMeta().getId())
          .status(OutboundMessagingStatus.PROCESSING)
          .type(OutboundMessagingType.I009_SHIPMENT_RECEIPT)
          .siteIdentifier(siteIdentifier)
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

  private ShipmentReceipt generateShipmentReceipt(String xml, String siteIdentifier) {
    val now = OffsetDateTime.now();

    val meta = MessageMeta.builder()
        .id(UUID.randomUUID())
        .fromSystemId(settingsSystemService.getSystemUuid())
        .timestamp(now)
        .build();

    return ShipmentReceipt.builder()
        .meta(meta)
        .shipmentReceiptRecordXml(xml)
        .siteIdentifier(siteIdentifier)
        .build();
  }

  private void saveDataloadLog(String dataRow, String description, int lineNumber) {
    val refDataloadLog = RefDataloadLog.builder()
        .interfaceName("I142") //even though this is actually the i009 feed, everything labels it as the I142 in the database.
        .createdDate(OffsetDateTime.now())
        .createdBy("1") //system user
        .dataRow(dataRow)
        .description(description)
        .lineNumber(lineNumber)
        .build();

    refDataloadLogService.save(refDataloadLog);
  }
}