package mil.usmc.mls2.stratis.core.processor.gcss;

import exmlservice.I136.StratisItemMasterResponse;
import exmlservice.I136UnMarshaller;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
class MhifProcessorImpl implements MhifProcessor {

  protected static final String HOST_NAME = "MHIF";

  private final RefMhifService refMhifService;
  private final RefDataloadLogService refDataloadLogService;
  private final SiteInfoService siteInfoService;
  private final GcssMcImportsDataService gcssMcImportsDataService;
  private final ErrorQueueService errorQueueService;
  private final SiteInterfaceService siteInterfaceService;

  @Override
  public boolean process() {
    try {
      setMhifStatus("RUNNING");
      saveDataloadLog("INFO: REF_MHIF GCSS-MC STARTED", "INFO: REF_MHIF GCSS-MC STARTED", 0);

      //* get the GCSSMC XML from the database for import
      val gcssMcImportsDataSet = getAllGcssMcImportsData();
      if (CollectionUtils.isEmpty(gcssMcImportsDataSet)) {
        saveDataloadLog("ERROR: REF_MHIF GCSS-MC FAILED TO LOAD DUE TO NO XML", "ERROR: REF_MHIF GCSS-MC Missing XML, wait until MHIF XML ready", 0);
        setMhifStatus("NOXMLREADY");
        return false;
      }

      //Note: deleteAll called first to mirror old process.
      refMhifService.deleteAll();
      deleteRefDataloadLogsAndReferences();

      val processingStatus = new ProcessingStatus();
      gcssMcImportsDataSet.forEach(gcssMcImportsData -> loadMhifFeed(gcssMcImportsData, processingStatus));
      saveDataloadLog("INFO: REF_MHIF GCSS_MC Loading Complete (Part I of II) - Found total rows " + processingStatus.totalRowCount.get() + ", Inserted total rows " + processingStatus.rowsProcessed.get() + ", Total Item (NIIN) not found " + processingStatus.notFound.get() + ".", "INFO: REF_MHIF LOADED", processingStatus.rowsProcessed.get());

      processMhif();
      setMhifStatus("COMPLETED");

      return true;
    }
    catch (Exception e) {
      saveDataloadLog("ERROR: SET STATUS TO FAILED " + e, "ERROR: ERROR getting latest XML", 0);
      setMhifStatus("FAILED");
      return false;
    }
  }

  private void processMhif() {
    try {
      saveDataloadLog("INFO: REF_MHIF Executing PROCESS_MHIF ....",
          "INFO: EXECUTE PROCESS_MHIF", 0);
      refMhifService.callProcessMhif();
      saveDataloadLog("INFO: REF_MHIF Processing Complete (Part II of II)",
          "INFO: REF_MHIF COMPLETED", 0);
    }
    catch (Exception e) {
      saveDataloadLog("ERROR: SET STATUS TO FAILED " + e, "ERROR: An exception occurred during MHIF processing", 0);
      log.error("MHIF ERROR during processing", e);
      setMhifStatus("FAILED");
      throw new StratisRuntimeException("Exception calling ProcessMhif Stored Procedure", e);
    }
  }

  private void loadMhifFeed(GcssMcImportsData gcssMcImportsData, ProcessingStatus processingStatus) {
    try {
      StratisItemMasterResponse i136Response;
      i136Response = I136UnMarshaller.unmarshallDecompressedString(gcssMcImportsData.getXml());
      if (i136Response == null) {
        saveDataloadLog("ERROR: REF_MHIF FAILED TO LOAD DUE TO NO XML COLLECTION", "ERROR: REF_MHIF GCSS-MC Missing XML COLLECTION", 0);
        setMhifStatus("XMLERR");
        return;
      }

      val collection = i136Response.getStratisItemMasterCollection();
      if (collection == null) {
        saveDataloadLog("ERROR: REF_MHIF FAILED TO LOAD DUE TO NO XML COLLECTION", "ERROR: REF_MHIF GCSS-MC Missing XML COLLECTION", 0);
        setMhifStatus("XMLERR");
        return;
      }

      val currentJulianDate = Util.getCurrentJulian(4);
      val mhifList = collection.getStratisItemMasterRecord();

      mhifList.forEach(mhif -> insertMhifRecord(mhif, processingStatus, currentJulianDate));

      gcssMcImportsData.processingComplete();
      gcssMcImportsDataService.save(gcssMcImportsData);
    }
    catch (Exception e1) {
      log.error("An error occurred during the processing of MHIF", e1);
      saveDataloadLog("ERROR: SET STATUS TO FAILED " + e1, "ERROR: An exception occurred during load GCSS-MC", 0);
      gcssMcImportsData.processingFailed();
      gcssMcImportsDataService.save(gcssMcImportsData);
    }
  }

  private void insertMhifRecord(StratisItemMasterResponse.StratisItemMasterCollection.StratisItemMasterRecord stratisItemMasterRecord, ProcessingStatus processingStatus, String currentJulianDate) {
    processingStatus.totalRowCount.getAndIncrement();
    if ("S".equals(stratisItemMasterRecord.getEStatus())) {

      val unitPrice = stratisItemMasterRecord.getUnitPrice() != null ? stratisItemMasterRecord.getUnitPrice().intValue() : 1;

      val refMhif = RefMhif.builder()
          .recordFsc(stratisItemMasterRecord.getFSC())
          .recordNiin(stratisItemMasterRecord.getNIIN().substring(0, 9))
          .primeFsc(stratisItemMasterRecord.getPrimeFSC())
          .primeNiin(stratisItemMasterRecord.getPrimeNIIN().substring(0, 9))
          .itemNameNomenclature(stratisItemMasterRecord.getNomenclature())
          .unitOfIssue(stratisItemMasterRecord.getUOI())
          .storesAccountCode(stratisItemMasterRecord.getStoresAccountCode())
          .unitPrice(unitPrice)
          .recoverabilityCode(stratisItemMasterRecord.getRecoverabilityCode())
          .shelfLifeCode(stratisItemMasterRecord.getShelfLifeCode())
          .ciic(stratisItemMasterRecord.getControlledInvItemCode())
          .phraseCode(stratisItemMasterRecord.getPhraseCode())
          .pmic(stratisItemMasterRecord.getPrecMIC())
          .combatEssentiallyCode(stratisItemMasterRecord.getCombatEssentiallyCode())
          .demilitarizationCode(stratisItemMasterRecord.getDemilC())
          .jdate(currentJulianDate)
          .serialControlFlag(stratisItemMasterRecord.getSerialControlFlag())
          .lotControlFlag(stratisItemMasterRecord.getLotControlFlag())
          .physicalSecurityCode(stratisItemMasterRecord.getControlledInvItemCode())
          .build();

      refMhifService.save(refMhif);
      processingStatus.rowsProcessed.getAndIncrement();
    }
    else {
      processingStatus.notFound.getAndIncrement();
      saveDataloadLog("ERROR: GCSS_MC ITEM NOT FOUND = " + stratisItemMasterRecord.getNIIN(), "ERROR: REF_MHIF GCSS-MC Return code other than Success means Item not found in GCSS_MC", processingStatus.totalRowCount.get());
    }
  }

  private Set<GcssMcImportsData> getAllGcssMcImportsData() {
    val gcssmcImportDataSearchCriteria = GcssMcImportsDataSearchCriteria.builder()
        .interfaceName(HOST_NAME)
        .statuses(Collections.singletonList("READY"))
        .build();
    gcssmcImportDataSearchCriteria.setSort("id", SortOrder.ASC);
    return gcssMcImportsDataService.search(gcssmcImportDataSearchCriteria);
  }

  private void deleteRefDataloadLogsAndReferences() {
    val siteInfo = siteInfoService.getRecord();

    val now = OffsetDateTime.now();
    int gcssLogClear = siteInfo.getGcssmcLogClear() != null ? siteInfo.getGcssmcLogClear() : 7;
    int refLogClear = siteInfo.getRefLogClear() != null ? siteInfo.getRefLogClear() : 1;
    val gcssCreatedBefore = now.minusDays(gcssLogClear);
    val refCreatedBefore = now.minusDays(refLogClear);

    val refDataloadCriteria = RefDataloadLogSearchCriteria.builder()
        .interfaceName(HOST_NAME)
        .createdBefore(refCreatedBefore)
        .build();

    val refData = refDataloadLogService.search(refDataloadCriteria);
    refData.forEach(refDataloadLogService::delete);

    val importsCriteriaForDelete = GcssMcImportsDataSearchCriteria.builder()
        .interfaceName(HOST_NAME)
        .createdBefore(gcssCreatedBefore)
        .statusesNotEqual(Collections.singletonList("READY"))
        .build();

    val refMhifSearchCriteria = RefMhifSearchCriteria.builder()
        .build();
    val refMhifRecords = refMhifService.search(refMhifSearchCriteria);
    val keysToNotMatch = refMhifRecords.stream()
        .map(s -> String.valueOf(s.getRefMhifId()))
        .collect(Collectors.toList());

    val importsDataToDelete = gcssMcImportsDataService.search(importsCriteriaForDelete);
    importsDataToDelete.forEach(gcssMcImportsDataService::delete);

    val errorQueueCriteriaForDelete = ErrorQueueCriteria.builder()
        .keyType("ref_mhif_id")
        .keyNumsToNotMatch(keysToNotMatch)
        .build();

    val errorQueuesToDelete = errorQueueService.search(errorQueueCriteriaForDelete);
    errorQueuesToDelete.forEach(errorQueueService::delete);
  }

  private void setMhifStatus(String status) {
    val siteInterface = siteInterfaceService.getByInterfaceName(HOST_NAME);
    if (siteInterface.isPresent()) {
      val site = siteInterface.get();
      site.setLastImpExpDate(OffsetDateTime.now());
      site.setStatus(status);
      siteInterfaceService.save(site);
    }
  }

  private void saveDataloadLog(String dataRow, String description, int lineNumber) {
    val refDataloadLog = RefDataloadLog.builder()
        .interfaceName(HOST_NAME)
        .createdDate(OffsetDateTime.now())
        .createdBy("1") //system user
        .dataRow(dataRow)
        .description(description)
        .lineNumber(lineNumber)
        .build();

    refDataloadLogService.save(refDataloadLog);
  }

  @Data
  private static class ProcessingStatus {

    AtomicInteger totalRowCount = new AtomicInteger(0);
    AtomicInteger rowsProcessed = new AtomicInteger(0);
    AtomicInteger notFound = new AtomicInteger(0);
  }
}
