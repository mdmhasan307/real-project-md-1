package mil.usmc.mls2.stratis.core.service.gcss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.r12.i009.I009ShipmentReceiptsInbound;
import mil.usmc.mls2.r12.i009.I009ShipmentReceiptsInboundProcessCompressedRequest;
import mil.usmc.mls2.r12.i009.I009ShipmentReceiptsInbound_Service;
import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLog;
import mil.usmc.mls2.stratis.core.domain.model.Spool;
import mil.usmc.mls2.stratis.core.domain.model.SpoolSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.service.RefDataloadLogService;
import mil.usmc.mls2.stratis.core.service.SiteInfoService;
import mil.usmc.mls2.stratis.core.service.SiteRemoteConnectionService;
import mil.usmc.mls2.stratis.core.service.SpoolService;
import mil.usmc.mls2.stratis.core.utility.ExmlServiceManager;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.xml.ws.BindingProvider;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.LEGACY)
class I009ShipmentReceiptStandardServiceImpl implements I009ShipmentReceiptService {

  @Value("${stratis.ws.gcss.i009.endPoint:I009ShipmentReceiptsIn}")
  private String feedEndPoint;

  private final SpoolService spoolService;
  private final SiteInfoService siteInfoService;
  private final SiteRemoteConnectionService siteRemoteConnectionService;
  private final ExmlServiceManager exmlServiceManager;
  private final R12GcssFeedConstants gcssFeedConstants;
  private final R12GcssFeedProcessorHelper gcssFeedProcessorHelper;
  private final RefDataloadLogService refDataloadLogService;

  @Override
  public void processShipmentReceipt() {
    val start = OffsetDateTime.now();
    val siteInfo = siteInfoService.getRecord();
    if (!"Y".equalsIgnoreCase(siteInfo.getInterfacesOn()) || !"Y".equalsIgnoreCase(siteInfo.getGcssMc())) {
      log.info("Interfaces are not or not GCSS_MC on in site info.  ExportI009StandardJob skipping.");
      return;
    }

    val spoolRecord = getSpoolToProcess();
    if (spoolRecord.isPresent()) {
      val spool = spoolRecord.get();

      val output = exmlServiceManager.compress(spool.getXml());

      if (output != null) {
        spool.updateToInProgress();
        spoolService.save(spool);

        I009ShipmentReceiptsInboundProcessCompressedRequest request = new I009ShipmentReceiptsInboundProcessCompressedRequest();
        request.setInput(output);

        val configInfo = createServiceConfigInfo();

        gcssFeedProcessorHelper.configureWebServiceRequest(configInfo);

        val interfaceToUse = configInfo.getFeedInterface();
        interfaceToUse.initiateCompressed(request);

        spool.updateToComplete(spoolService.getSpoolBatchNumber());
        spoolService.save(spool);
        log.info(": spool_id sent: {}", spool.getSpoolId());
        val end = OffsetDateTime.now();
        saveDataloadLog("Success send", String.format("Job took %n seconds", ChronoUnit.SECONDS.between(start, end)), 0);
      }
      else {
        log.warn("null output issue with compression.");
      }
    }
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

  protected ServiceConfigInfo<I009ShipmentReceiptsInbound> createServiceConfigInfo() {
    val feedService = new I009ShipmentReceiptsInbound_Service();
    val feedInterface = feedService.getI009ShipmentReceiptsInboundPort();
    val ctx = ((BindingProvider) feedInterface).getRequestContext();

    val client = ClientProxy.getClient(feedInterface);

    val config = new ServiceConfigInfo<I009ShipmentReceiptsInbound>(gcssFeedConstants);
    config.setClient(client);
    config.setContext(ctx);
    config.setFeedInterface(feedInterface);
    config.setFeedEndPoint(feedEndPoint);

    return config;
  }
}
