package mil.usmc.mls2.stratis.core.service.gcss;

import exmlservice.I136.StratisItemMasterResponse;
import exmlservice.I136Marshaller;
import exmlservice.I136UnMarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.integration.migs.gcss.r001.outbound.model.ItemMasterData;
import mil.usmc.mls2.r12.i136.I136ItemMasterReconInboundOutbound;
import mil.usmc.mls2.r12.i136.I136ItemMasterReconInboundOutboundProcessCompressedRequest;
import mil.usmc.mls2.r12.i136.I136ItemMasterReconInboundOutboundProcessResponse;
import mil.usmc.mls2.r12.i136.I136ItemMasterReconInboundOutbound_Service;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.common.model.enumeration.I136ProcessingStatusEnum;
import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsData;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfo;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfoSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.service.GcssMcImportsDataService;
import mil.usmc.mls2.stratis.core.service.NiinInfoService;
import mil.usmc.mls2.stratis.core.service.SiteInfoService;
import mil.usmc.mls2.stratis.core.utility.ExmlServiceManager;
import mil.usmc.mls2.stratis.integration.mls2.core.service.GcssI136Service;
import mil.usmc.mls2.stratis.modules.mhif.application.model.ItemMasterProcessResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.xml.ws.BindingProvider;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Profile(Profiles.LEGACY)
@SuppressWarnings("Duplicates")
class I136NiinStandardServiceImpl implements I136NiinService {

  @Value("${stratis.ws.gcss.i136.endPoint:I136ItemMasterReconInOut}")
  private String feedEndPoint;

  @Value("${stratis.ws.gcss.disabled}")
  private boolean wsDisabled;

  //This will always cause a failure downstream if its disabled.
  @Value("${stratis.ws.disable.exml:false}")
  private boolean exmlDisabled;

  private final SiteInfoService siteInfoService;
  private final ExmlServiceManager exmlServiceManager;
  private final GcssMcImportsDataService gcssMcImportsDataService;
  private final NiinInfoService niinInfoService;
  private final R12GcssFeedConstants gcssFeedConstants;
  private final R12GcssFeedProcessorHelper gcssFeedProcessorHelper;
  private final GcssI136Service gcssI136Service;

  @Override
  public boolean isWsDisabled() {
    return wsDisabled;
  }

  @Override
  public ItemMasterProcessResult processI136Niin(String niin) {
    return processI136Niins(Collections.singleton(niin));
  }

  //previous design in GCSSMCTransactionsImpl.process136Request_Immediate returned a string and various processes looked for ERROR in said string
  //new process will return a boolean if a response was received from GCSS.
  @Override
  public ItemMasterProcessResult processI136Niins(Set<String> niins) {
    log.info("Inside I136NiinStandardServiceImpl");
    val siteInfo = siteInfoService.getRecord();

    if (!"Y".equals(siteInfo.getInterfacesOn())) {
      log.info("GCSS Interface not enabled.  So I136StandardNiinService not communicating with GCSS");
      return ItemMasterProcessResult.ofStatusWithTotal(I136ProcessingStatusEnum.FAILURE_INTERFACES_OFF, niins.size());
    }

    if (CollectionUtils.isEmpty(niins)) {
      log.info("Call to GCSS skipped, no NIINS to process.");
      return ItemMasterProcessResult.ofStatusWithTotal(I136ProcessingStatusEnum.FAILURE, niins.size());
    }

    //FUTURE Backlog verify if this could just use the i136Marshaller instead to generate the xml

    StringBuilder xmlSStr = new StringBuilder();
    xmlSStr.append("<?xml version = '1.0' encoding = 'UTF-8'?>" + "<")
        .append(I136Marshaller.ROOT_TAG)
        .append(" xmlns=\"")
        .append(I136Marshaller.NAMESPACE)
        .append("\" >")
        .append("<rIC>")
        .append(siteInfo.getRoutingId())
        .append("</rIC>")
        .append("<stratisItemCollection>");
    niins.forEach(niin -> xmlSStr.append("<nIIN>")
        .append(niin)
        .append("</nIIN>"));
    xmlSStr.append("</stratisItemCollection>")
        .append("<countRequested>")
        .append(niins.size())
        .append("</countRequested>")
        .append("</")
        .append(I136Marshaller.ROOT_TAG)
        .append('>');

    byte[] output;
    if (exmlDisabled) {
      output = xmlSStr.toString().getBytes(StandardCharsets.UTF_8);
    }
    else {
      output = exmlServiceManager.compress(xmlSStr.toString());
    }

    if (output == null) {
      return ItemMasterProcessResult.ofStatusWithTotal(I136ProcessingStatusEnum.FAILURE_ERROR_COMPRESSING_DATA_TO_SEND, niins.size());
    }

    I136ItemMasterReconInboundOutboundProcessCompressedRequest request = new I136ItemMasterReconInboundOutboundProcessCompressedRequest();
    request.setInput(output);

    val configInfo = createServiceConfigInfo();

    I136ItemMasterReconInboundOutboundProcessResponse response;
    gcssFeedProcessorHelper.configureWebServiceRequest(configInfo);
    try {
      response = new I136ItemMasterReconInboundOutboundProcessResponse();
      val interfaceToUse = configInfo.getFeedInterface();
      val result = interfaceToUse.initiateCompressed(request);
      response.setResult(result.getResult());
    }
    catch (Exception e) {
      log.error("Error occurred getting I136 data from GCSS", e);
      return ItemMasterProcessResult.ofStatusWithTotal(I136ProcessingStatusEnum.FAILURE_ERROR_RESPONSE, niins.size());
    }

    if (response.getResult() == null) {
      return ItemMasterProcessResult.ofStatusWithTotal(I136ProcessingStatusEnum.FAILURE_NO_RESPONSE, niins.size());
    }

    String xmlStr = exmlServiceManager.decompress(response.getResult());
    if (StringUtils.isBlank(xmlStr)) {
      return ItemMasterProcessResult.ofStatusWithTotal(I136ProcessingStatusEnum.FAILURE_ERROR_DECOMPRESSING_RESULT, niins.size());
    }

    val gcssMcImportsData = GcssMcImportsData.builder()
        .xml(xmlStr)
        .interfaceName("MHIF")
        .status("READY")
        .createdBy(1)
        .createdDate(OffsetDateTime.now())
        .build();

    gcssMcImportsDataService.save(gcssMcImportsData);

    StratisItemMasterResponse i136Response = I136UnMarshaller.unmarshall(new BufferedInputStream(new ByteArrayInputStream(xmlStr.getBytes())));
    if (i136Response == null) {
      log.info("No Response found from GCSS");
      return ItemMasterProcessResult.ofStatusWithTotal(I136ProcessingStatusEnum.FAILURE_NO_RESPONSE, niins.size());
    }

    val mhifs = i136Response.getStratisItemMasterCollection().getStratisItemMasterRecord();
    val mhifsCount = mhifs.size();
    val mhifsFiltered = mhifs.stream().map(this::toItemMasterData).collect(Collectors.toSet());
    if (mhifsCount > mhifsFiltered.size()) {
      log.info("MHIFS count against filtered do not match.  Duplicates found in GCSS results (ItemMasterData defined by NIIN)");
    }

    val result = gcssI136Service.processItemMasterData(mhifsFiltered, false);
    log.debug("Item Master Result {}", result);
    return result;
  }

  //Converts GCSS data feed records into ItemMasterData records for reuse of new innovation processing of data into the db.
  private ItemMasterData toItemMasterData(StratisItemMasterResponse.StratisItemMasterCollection.StratisItemMasterRecord input) {
    val dateOfReconciliation = input.getDateOfReconciliation() != null ? input.getDateOfReconciliation().toGregorianCalendar().toZonedDateTime().toOffsetDateTime() : null;
    val unitPrice = input.getUnitPrice() != null ? input.getUnitPrice().doubleValue() : null;

    return ItemMasterData
        .builder()
        .combatEssentiallyCode(input.getCombatEssentiallyCode())
        .controlledInvItemCode(input.getControlledInvItemCode())
        .dateOfReconciliation(dateOfReconciliation)
        .demilC(input.getDemilC())
        .emsg(input.getEMsg())
        .estatus(input.getEStatus())
        .fsc(input.getFSC())
        .ipaac(input.getIPAAC())
        .lotControlFlag(input.getLotControlFlag())
        .niin(input.getNIIN())
        .nomenclature(input.getNomenclature())
        .phraseCode(input.getPhraseCode())
        .precMIC(input.getPrecMIC())
        .primeFSC(input.getPrimeFSC())
        .primeNIIN(input.getPrimeNIIN())
        .recoverabilityCode(input.getRecoverabilityCode())
        .serialControlFlag(input.getSerialControlFlag())
        .shelfLifeCode(input.getShelfLifeCode())
        .storesAccountCode(input.getStoresAccountCode())
        .unitPrice(unitPrice)
        .uoi(input.getUOI())
        .build();
  }

  /*
  This is called from the front end in the admin -> interfaces screen for the MHIF (no NIIN input process)
   */
  @Override
  public ItemMasterProcessResult processBatchI136Niins() {
    val siteInfo = siteInfoService.getRecord();

    val niinInfoSearchCriteria = NiinInfoSearchCriteria.builder()
        .niinIdGreaterThan(siteInfo.getLastImportedNiinId())
        .build();
    niinInfoSearchCriteria.setSort("niinId", SortOrder.ASC);
    niinInfoSearchCriteria.setMaxItems(siteInfo.getMhifRange());

    val results = niinInfoService.search(niinInfoSearchCriteria);

    val niins = results.stream().map(NiinInfo::getNiin).collect(Collectors.toSet());

    val maxNiinId = results.stream().mapToInt(NiinInfo::getNiinId).max();

    val processingComplete = processI136Niins(niins);

    // Move the starting point for next time no matter if the update failed or not.
    if (maxNiinId.isPresent()) {
      siteInfo.updateMhifLastImportedNiinId(maxNiinId.getAsInt());
      siteInfoService.save(siteInfo);
    }
    return processingComplete;
  }

  protected ServiceConfigInfo<I136ItemMasterReconInboundOutbound> createServiceConfigInfo() {
    val feedService = new I136ItemMasterReconInboundOutbound_Service();
    val feedInterface = feedService.getI136ItemMasterReconInboundOutboundPort();
    val ctx = ((BindingProvider) feedInterface).getRequestContext();

    val client = ClientProxy.getClient(feedInterface);

    val config = new ServiceConfigInfo<I136ItemMasterReconInboundOutbound>(gcssFeedConstants);
    config.setClient(client);
    config.setContext(ctx);
    config.setFeedInterface(feedInterface);
    config.setFeedEndPoint(feedEndPoint);

    return config;
  }
}
