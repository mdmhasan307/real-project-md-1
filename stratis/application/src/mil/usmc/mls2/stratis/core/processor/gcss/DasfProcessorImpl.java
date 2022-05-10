package mil.usmc.mls2.stratis.core.processor.gcss;

import exmlservice.I111.StratisDueInsOutCollection;
import exmlservice.I111UnMarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.domain.repository.*;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.DefaultTransactionExecutor;
import mil.usmc.mls2.stratis.core.infrastructure.util.EntityManagerUtils;
import mil.usmc.mls2.stratis.core.service.gcss.I136NiinService;
import mil.usmc.mls2.stratis.modules.dasf.application.model.DasfProcessResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.OffsetDateTime;
import java.util.Collections;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
class DasfProcessorImpl implements DasfProcessor {

  protected static final String HOST_NAME = "DASF";

  private final SiteInterfaceRepository siteInterfaceRepository;
  private final SiteInfoRepository siteInfoRepository;
  private final GcssMcImportsDataRepository gcssMcImportsDataRepository;
  private final ErrorQueueRepository errorQueueRepository;
  private final RefDataloadLogRepository refDataloadLogRepository;
  private final I136NiinService i136NiinService;
  private final RefDasfRepository refDasfRepository;
  private final DefaultTransactionExecutor transactionExecutor;

  @Value("${stratis.dasf.gcss.niin-info.single-call:true}")
  private boolean gcssSingleCall;

  public DasfProcessResult process() {
    var dasfProcessResult = new DasfProcessResult();

    try {
      val watch = new StopWatch();
      watch.start();

      //Note: deleteAll called first to mirror old process.
      //Note: deleteRefDataloadLogsAndReferences appears to do extra work since it was querying to get ref_dasf records....
      saveDataloadLog("INFO: REF_DASF  GCSS-MC STARTED", "INFO: REF_DASF GCSS-MC STARTED", 0);
      setDasfStatus("RUNNING");

      //* get the GCSSMC XML from the database for import
      val gcssMcImportsData = getLatestGcssMcImportsData();
      if (gcssMcImportsData == null) {
        saveDataloadLog("ERROR: REF_DASF FAILED TO LOAD DUE TO NO XML", "ERROR: REF_DASF GCSS-MC Missing XML", 0);
        setDasfStatus("NOXMLREADY");
        dasfProcessResult.success(false);
        return dasfProcessResult;
      }

      //set the gcssMcImportsDataId in the dasfProcessResult now that we know we have one in gcssMcImportsData
      dasfProcessResult.gcssmcDataImportsId(gcssMcImportsData.getId());

      //clear previous DASF data to allow import of new files data.
      refDasfRepository.truncate();

      //* clear previous logs for dasf
      deleteRefDataloadLogsAndReferences();
      saveDataloadLog("INFO: REF_DASF PREVIOUS DATA CLEARED, STARTING IMPORT", "INFO: REF_DASF GCSS-MC IN PROGRESS", 0);

      watch.stop();
      log.debug("DASF cleanup execution complete [durationSecs: {}]", watch.getTotalTimeSeconds());
      return processDasf(gcssMcImportsData, dasfProcessResult);
    }
    catch (Exception e) {
      log.error("Error occurred during Dasf Processing", e);
      dasfProcessResult.success(false);
      return dasfProcessResult;
    }
  }

  private GcssMcImportsData getLatestGcssMcImportsData() {
    val results = gcssMcImportsDataRepository.getMostRecentRecordForProcessing(HOST_NAME);
    return results.orElse(null);
  }

  private DasfProcessResult processDasf(GcssMcImportsData gcssMcImportsData, DasfProcessResult dasfProcessResult) {
    try {
      val watch = new StopWatch();
      watch.start();

      StratisDueInsOutCollection i111Collection = I111UnMarshaller.unmarshallDecompressedString(gcssMcImportsData.getXml());
      if (i111Collection == null) {
        saveDataloadLog("WARNING: REF_DASF FAILED TO LOAD DUE TO NO XML COLLECTION", "ERROR: REF_DASF GCSS-MC Missing XML COLLECTION", gcssMcImportsData.getId());
        setDasfStatus("XMLERR");

        dasfProcessResult.success(false);
        return dasfProcessResult;
      }

      val dasfList = i111Collection.getStratisDueInsOutRecord();
      val watch2 = new StopWatch();
      watch2.start();

      //FUTURE Could this be a group of child threads for faster processing.  But would need to wait till all threads are finished before moving to the next step.

      dasfProcessResult.totalRecordsFromGcss(dasfList.size());

      dasfList.forEach(dasf -> processRecord(dasf, dasfProcessResult));

      watch2.stop();
      log.debug("DASF inserts  complete [durationSecs: {}]", watch2.getTotalTimeSeconds());

      saveDataloadLog("INFO: REF_DASF LOADED", "INFO: REF_DASF LOADED", dasfProcessResult.totalRecordsFromGcss());

      val watch3 = new StopWatch();
      watch3.start();
      primeNiinInfos(dasfProcessResult);
      watch3.stop();
      log.debug("DASF Prime Niins  complete [durationSecs: {}]", watch3.getTotalTimeSeconds());

      saveDataloadLog("INFO: REF_DASF Complete." + dasfProcessResult.returnStatisticsAlways(), "INFO: REF_DASF COMPLETED", 0);

      //* set log statistics and status
      setDasfStatus("COMPLETED");
      gcssMcImportsData.processingComplete();
      gcssMcImportsDataRepository.save(gcssMcImportsData);
      ignoreAllOldGcssData(gcssMcImportsData);

      watch.stop();
      log.debug("DASF process execution complete [durationSecs: {}]", watch.getTotalTimeSeconds());

      dasfProcessResult.success(true);
      return dasfProcessResult;
    }
    catch (Exception e1) {
      saveDataloadLog("ERROR: SET STATUS TO FAILED " + e1, "ERROR: An exception occurred during load GCSS-MC", 0);
      setDasfStatus("FAILED");
      gcssMcImportsData.processingFailed();
      gcssMcImportsDataRepository.save(gcssMcImportsData);
      dasfProcessResult.success(false);
      return dasfProcessResult;
    }
  }

  private void processRecord(StratisDueInsOutCollection.StratisDueInsOutRecord dasf, DasfProcessResult dasfProcessResult) {
    try {

      if (dasf.getSDN().length() == 14) {
        //create RefDasf record for insert.
        val refDasf = RefDasf.builder()
            .documentNumber(dasf.getSDN())
            .ric(dasf.getRIC())
            .niin(dasf.getNIIN())
            .fsc(dasf.getRecordFSC())
            .unitOfIssue(dasf.getUOI())
            .quantityInvoiced(dasf.getQuantityInvoiced() == null ? 1 : dasf.getQuantityInvoiced().intValue())
            .supplementaryAddress(dasf.getSupADD())
            .projectCode(dasf.getProj())
            .priorityDesignatorCode(dasf.getPri())
            .quantityDue(dasf.getQuantityDueIn() == null ? 1 : dasf.getQuantityDueIn().intValue())
            .hostSystem("GCSS")
            .build();

        refDasfRepository.save(refDasf);
        dasfProcessResult.incrementRefDasfRecordsCreated();
      }
      else {
        dasfProcessResult.incrementRefDasfRecordsFailed();
      }
    }
    catch (Exception eDASF) {
      dasfProcessResult.incrementRefDasfRecordsFailed();
      log.error("Failed to insert RefDasf Record for {}", dasf.getNIIN());
    }
  }

  private void primeNiinInfos(DasfProcessResult dasfProcessResult) {
    val niinsToFind = refDasfRepository.findMissingNiins();
    dasfProcessResult.niinsLookedUpInGcssMhif(niinsToFind.size());
    if (niinsToFind.size() > 0) {
      if (gcssSingleCall) {
        log.debug("Sending NIINS to GCSS using single-call: [count: {}, niins: {}]", niinsToFind.size(), niinsToFind);
        val result = i136NiinService.processI136Niins(niinsToFind);
        saveDataloadLog(String.format("INFO: REF_DASF LOADED NIINs from GCSS %s [Total NIINs: %s NIINs Processed: %s NIINs Skipped: %s NIINs Errored: %s]", result.status().getLabel(), result.totalNiins(), result.niinsProcessed(), result.niinsSkipped(), result.niinsErrored()),
            result.status().getLabel(), 0);
        dasfProcessResult.niinsLoadedFromGcssMhif(result.niinsProcessed());
      }
      else {
        log.debug("Sending NIINS to GCSS using multi-call: [count: {}, niins: {}]", niinsToFind.size(), niinsToFind);
        niinsToFind.forEach(record -> {
          val result = i136NiinService.processI136Niin(record);
          saveDataloadLog(String.format("INFO: REF_DASF ATTEMPTING TO LOAD NIIN %s from GCSS ", record),
              result.status().getLabel(), 0);
          if (result.niinsProcessed() == 1) dasfProcessResult.incrementNiinsLoadedFromGcssMhif();
        });
      }
    }
  }

  //this runs in its own transaction so that the site Interface table is always saved for user interface notification of process status.
  private void setDasfStatus(String status) {
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

  private void ignoreAllOldGcssData(GcssMcImportsData data) {
    gcssMcImportsDataRepository.updateIgnoreAllPreviousDataByInterface(data.getId(), HOST_NAME);
  }

  private void deleteRefDataloadLogsAndReferences() {
    val siteInfo = siteInfoRepository.getRecord().orElseThrow(() -> new StratisRuntimeException("Site Information not found"));

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

  private void saveDataloadLog(String dataRow, String description, int lineNumber) {
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
}
