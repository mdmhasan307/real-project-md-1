package mil.usmc.mls2.stratis.core.processor.gcss;

import exmlservice.I033.StratisInvOutbCollection;
import exmlservice.I033UnMarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.stratis.common.util.Util;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.domain.repository.*;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.DefaultTransactionExecutor;
import mil.usmc.mls2.stratis.core.infrastructure.util.EntityManagerUtils;
import mil.usmc.mls2.stratis.modules.gabf.application.model.GabfProcessResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
class GabfProcessorImpl implements GabfProcessor {

  protected static final String HOST_NAME = "GABF";

  private final SiteInterfaceRepository siteInterfaceRepository;
  private final SiteInfoRepository siteInfoRepository;
  private final GcssMcImportsDataRepository gcssMcImportsDataRepository;
  private final ErrorQueueRepository errorQueueRepository;
  private final RefGabfRepository refGabfRepository;
  private final RefGabfSerialRepository refGabfSerialRepository;
  private final RefDataloadLogRepository refDataloadLogRepository;
  private final DefaultTransactionExecutor transactionExecutor;

  public GabfProcessResult process() {
    val gcssMcImportsData = gcssMcImportsDataRepository.getMostRecentRecordForProcessing(HOST_NAME).orElse(null);
    var gabfProcessResult = new GabfProcessResult();

    log.debug("GABF processing initiated");
    try {
      val watch = new StopWatch();
      watch.start();

      setGabfStatus("RUNNING");
      saveDataloadLog("INFO: REF_GABF  GCSS-MC STARTED", "INFO: REF_GABF GCSS-MC STARTED", 0);

      if (gcssMcImportsData == null) {
        saveDataloadLog("ERROR: REF_GABF FAILED TO LOAD DUE TO NO XML", "ERROR: REF_GABF GCSS-MC Missing XML", 0);
        setGabfStatus("NOXMLREADY");
        gabfProcessResult.success(false);
        return gabfProcessResult;
      }

      StratisInvOutbCollection i033Collection = I033UnMarshaller.unmarshallDecompressedString(gcssMcImportsData.getXml());
      if (i033Collection == null) {
        saveDataloadLog("WARNING: REF_GABF FAILED TO LOAD DUE TO NO XML COLLECTION", "ERROR: REF_GABF GCSS-MC Missing XML COLLECTION", gcssMcImportsData.getId());
        setGabfStatus("XMLERR");
        gabfProcessResult.success(false);
        return gabfProcessResult;
      }

      //set the gcssMcImportsDataId in the gabfProcessResult now that we know we have one in gcssMcImportsData
      gabfProcessResult.gcssmcDataImportsId(gcssMcImportsData.getId());

      val siteInfo = siteInfoRepository.getRecord().orElseThrow(() -> new StratisRuntimeException("Site Information not found"));

      //clear previous GABF data to allow import of new files data.
      refGabfRepository.truncate();

      //clear previous logs for gabf
      deleteRefDataloadLogsAndReferences(siteInfo);
      saveDataloadLog("INFO: REF_GABF PREVIOUS DATA CLEARED, STARTING IMPORT", "INFO: REF_GABF GCSS-MC IN PROGRESS", 0);

      val gabfList = i033Collection.getStratisInvOutboundMasterRec();
      gabfProcessResult.totalRecordsFromGcss(gabfList.size());

      Set<String> niinsInserted = new HashSet<>();
      gabfList.forEach(gabf -> processRecord(gabf, siteInfo.getAac(), niinsInserted, gabfProcessResult));

      callBackendProcessingOfGabf();
      setGabfStatus("COMPLETED");
      gcssMcImportsData.processingComplete();
      gcssMcImportsDataRepository.save(gcssMcImportsData);
      ignoreAllOldGcssData(gcssMcImportsData);

      gabfProcessResult.success(true);
      watch.stop();
      log.debug("GABF execution complete [durationSecs: {}]", watch.getTotalTimeSeconds());
      log.debug("GABF Results: {}", gabfProcessResult.returnStatisticsAlways());
    }
    catch (Exception e) {
      gabfProcessResult.success(false);
      setGabfStatus("FAILED");
      saveDataloadLog("ERROR: An exception occurred during GABF Load " + e, "ERROR: GABF Processing Failed", 0);
      if (gcssMcImportsData != null) {
        gcssMcImportsData.processingFailed();
        gcssMcImportsDataRepository.save(gcssMcImportsData);
      }
    }
    return gabfProcessResult;
  }

  private void processRecord(StratisInvOutbCollection.StratisInvOutboundMasterRec gabf, String siteAac, Set<String> niinsInserted, GabfProcessResult gabfProcessResult) {
    try {
      //* skip gabf record if AAC mismatch
      if (!siteAac.equalsIgnoreCase(gabf.getIPAAC())) {
        //GABF Requirement - skip row if Site AAC != AAC on GABF
        gabfProcessResult.incrementRecordSkipped();
        saveDataloadLog("INFO GABF SKIP ROW", "INFO GABF: Skip this row in ref_gabf due to AAC of GABF record does not equal site AAC", 0);
        return;
      }

      String cc = gabf.getCC();
      int qtyOnHand = Util.cleanInt(gabf.getQTOH());
      String lastTransactionDate = Util.getCurrentJulian(4);

      //* insert gabf record

      if (niinsInserted.contains(gabf.getNIIN())) {
        // transactiondate comparison in checkNIINFSCForGABF will not be accurate if not 4 digit julian
        val updated = checkNiinAndUpdateIfNeeded(gabf.getNIIN(),
            lastTransactionDate,
            (cc.contains("A") ? qtyOnHand : 0),
            (cc.contains("F") ? qtyOnHand : 0), gabfProcessResult); //* unserviceable
        if (!updated) {
          processInsert(gabf, niinsInserted, gabfProcessResult);
        }
        return;
      }
      processInsert(gabf, niinsInserted, gabfProcessResult);
    }
    catch (Exception e4) {
      val message = String.format("GABF Record Processing Failure for [NIIN %s AAC %s CC %s OUI %S]", gabf.getNIIN(), gabf.getIPAAC(), gabf.getCC(), gabf.getUOI());
      gabfProcessResult.incrementRefGabfRecordsFailed();
      gabfProcessResult.addMessage(message);
      log.info(message, e4);
      saveDataloadLog("ERROR: " + e4, "ERROR: REF_GABF An exception occurred " + message, 0);
    }
  }

  private void processInsert(StratisInvOutbCollection.StratisInvOutboundMasterRec gabf, Set<String> niinsInserted, GabfProcessResult gabfProcessResult) {
    String cc = gabf.getCC();
    int qtyOnHand = Util.cleanInt(gabf.getQTOH());

    String lastTransactionDate = Util.getCurrentJulian(4);

    val refGabfRecord = RefGabf.builder()
        .recordNiin(gabf.getNIIN())
        .activityAddressCode(gabf.getIPAAC())
        .unitOfIssue(gabf.getUOI())
        .onHandOpStockServiceable((cc.equals("A") ? String.valueOf(qtyOnHand) : "0"))
        .onHandUnserviceable((cc.equals("F") ? String.valueOf(qtyOnHand) : "0"))
        .lastTransactionDate(lastTransactionDate)
        .build();

    refGabfRepository.save(refGabfRecord);

    int newRefGabfId = refGabfRecord.getRefGabfId();
    gabfProcessResult.incrementRefGabfRecordsCreated();

    processSerialsForRecord(gabf, cc, newRefGabfId, gabfProcessResult);
    niinsInserted.add(gabf.getNIIN());
  }

  private void processSerialsForRecord(StratisInvOutbCollection.StratisInvOutboundMasterRec gabf, String cc, Integer newRefGabfId, GabfProcessResult gabfProcessResult) {
    if (gabf.getLotControlFlag().equalsIgnoreCase("Y") || gabf.getSerialControlFlag().equalsIgnoreCase("Y")) {

      val gabfColl = gabf.getStratisInvOutboundDetailCollection();
      gabfColl.getStratisInvOutboundDetailRec().forEach(gabfRec -> {
        String serialNum = gabfRec.getSerN();
        String lotNum = gabfRec.getLotN();
        try {
          val qty = Util.isEmpty(serialNum) ? gabfRec.getQLot().intValue() : 1L;
          val refGabfSerial = RefGabfSerial.builder()
              .serialNumber(serialNum)
              .refGabfId(newRefGabfId)
              .lotConNum(lotNum)
              .cc(cc)
              .quantity(qty)
              .build();
          refGabfSerialRepository.save(refGabfSerial);
          gabfProcessResult.incrementRefGabfSerialRecordsCreated();
        }
        catch (Exception e) {
          val message = String.format("GABF Serial Processing Failure for [ID: %s Serial Number: %s CC: %s]", newRefGabfId, serialNum, cc);
          gabfProcessResult.incrementRefGabfSerialRecordsFailed();
          gabfProcessResult.addMessage(message);
          throw e; //this error gets logged upsteam
        }
      });
    }
  }

  private void ignoreAllOldGcssData(GcssMcImportsData data) {
    gcssMcImportsDataRepository.updateIgnoreAllPreviousDataByInterface(data.getId(), HOST_NAME);
  }

  private void callBackendProcessingOfGabf() {
    val watch = new StopWatch();
    watch.start();
    setGabfStatus("PROCESSING");
    saveDataloadLog("INFO: REF_GABF Executing POPULATE_RECON_HIST ....",
        "INFO: EXECUTE POPULATE_RECON_HIST", 0);

    refGabfRepository.callPopulateGcssReconHist();

    saveDataloadLog("INFO: REF_GABF Processing Complete (Part II of II)",
        "INFO: REF_GABF COMPLETED", 0);
    watch.stop();
    log.debug("GABF Stored Procedure execution complete [durationSecs: {}]", watch.getTotalTimeSeconds());
  }

  private boolean checkNiinAndUpdateIfNeeded(String niin, String lastTrnDt, int qtyCCA, int qtyCCF, GabfProcessResult gabfProcessResult) {

    try {
      val refGabfSearchCriteria = RefGabfSearchCriteria.builder()
          .niin(niin)
          .build();

      val results = refGabfRepository.search(refGabfSearchCriteria);
      if (CollectionUtils.isEmpty(results)) {
        return false;
      }
      val currentGabfRecord = results.iterator().next();

      String lastTransactionDate = currentGabfRecord.getLastTransactionDate();
      if (lastTransactionDate.trim().length() <= 0)
        lastTransactionDate = "0000";

      val dateCheck = (date1NewerThenDate2(lastTrnDt, lastTransactionDate));

      if (dateCheck || checkForUpdateNeeded(currentGabfRecord, qtyCCA, qtyCCF)) {
        currentGabfRecord.updateFromFeed(
            qtyCCA != 0 ? String.valueOf(qtyCCA) : currentGabfRecord.getOnHandOpStockServiceable(),
            qtyCCF != 0 ? String.valueOf(qtyCCF) : currentGabfRecord.getOnHandUnserviceable(),
            lastTrnDt);
        gabfProcessResult.incrementRefGabfRecordsUpdated();
        refGabfRepository.save(currentGabfRecord);
        return true;
      }
    }
    catch (Exception e) { log.error("Error in checkNIINFSCForGABF {}", niin, e); }
    return false;
  }

  private boolean checkForUpdateNeeded(RefGabf currentGabfRecord, int qtyCCA, int qtyCCF) {
    val onHandOpStockServiceable = Util.cleanInt(currentGabfRecord.getOnHandOpStockServiceable());
    val onHandUnserviceable = Util.cleanInt(currentGabfRecord.getOnHandUnserviceable());

    if ((onHandOpStockServiceable == 0) && (qtyCCA > 0)) {
      //Go for new value
      return true;
    }
    else if (qtyCCA > 0) {
      //Go for new value
      return true;
    }

    if ((onHandUnserviceable == 0) && (qtyCCF > 0)) {
      return true;
    }
    else return (qtyCCF > 0);
  }

  private boolean date1NewerThenDate2(String date1, String date2) {
    //* return true if date1 is later/greater than date2
    int year1 = Util.cleanInt(date1.substring(0, 1));
    int day1 = Util.cleanInt(date1.substring(1, 4));
    int year2 = Util.cleanInt(date2.substring(0, 1));
    int day2 = Util.cleanInt(date2.substring(1, 4));

    //* algorithm for 4 digit julian must be safe for years ending in 9
    //* and next years starting with 0
    if (year2 == 9) return true;

    if (year1 > year2) return true;

    return (day1 > day2);
  }

  private void deleteRefDataloadLogsAndReferences(SiteInfo siteInfo) {
    val now = OffsetDateTime.now();
    int gcssLogClear = siteInfo.getGcssmcLogClear() != null ? siteInfo.getGcssmcLogClear() : 7;
    int refLogClear = siteInfo.getRefLogClear() != null ? siteInfo.getRefLogClear() : 1;
    val gcssCreatedBefore = now.minusDays(gcssLogClear);
    val refCreatedBefore = now.minusDays(refLogClear);

    clearOldRefDataloadLogs(refCreatedBefore);
    clearOldGcssmcImportsData(gcssCreatedBefore);
    clearErrorQueuesForDasf();
  }

  private void clearOldRefDataloadLogs(OffsetDateTime refCreatedBefore) {
    val refDataloadCriteria = RefDataloadLogSearchCriteria.builder()
        .interfaceName(HOST_NAME)
        .createdBefore(refCreatedBefore)
        .build();

    refDataloadLogRepository.delete(refDataloadCriteria);
    EntityManagerUtils.flushAndClear();
  }

  private void clearOldGcssmcImportsData(OffsetDateTime gcssCreatedBefore) {
    val importsCriteriaForDelete = GcssMcImportsDataSearchCriteria.builder()
        .interfaceName(HOST_NAME)
        .createdBefore(gcssCreatedBefore)
        .statusesNotEqual(Collections.singletonList("READY"))
        .build();
    gcssMcImportsDataRepository.delete(importsCriteriaForDelete);
    EntityManagerUtils.flushAndClear();
  }

  private void clearErrorQueuesForDasf() {
    val errorQueueCriteriaForDelete = ErrorQueueCriteria.builder()
        .keyType("ref_dasf_id")
        .build();
    errorQueueRepository.delete(errorQueueCriteriaForDelete);
    EntityManagerUtils.flushAndClear();
  }

  //this runs in its own transaction so that the site Interface table is always saved for user interface notification of process status.
  private void setGabfStatus(String status) {
    val task = (Runnable) () -> {
      val siteInterface = siteInterfaceRepository.getByInterfaceName(HOST_NAME);
      if (siteInterface.isPresent()) {
        val site = siteInterface.get();
        site.setLastImpExpDate(OffsetDateTime.now());
        site.setStatus(status);
        siteInterfaceRepository.save(site);
      }
    };
    transactionExecutor.execute("txSetMatsStatus", task);
  }

  private void saveDataloadLog(String dataRow, String description, int lineNumber) {
    try {
      val refDataloadLog = RefDataloadLog.builder()
          .interfaceName(HOST_NAME)
          .createdDate(OffsetDateTime.now())
          .createdBy("1") //system user
          .dataRow(dataRow)
          .description(description)
          .lineNumber(lineNumber)
          .build();

      refDataloadLogRepository.save(refDataloadLog);
    }
    catch (Exception e) {
      log.error("Unable to log DataloadLog record {}", description);
    }
  }
}
