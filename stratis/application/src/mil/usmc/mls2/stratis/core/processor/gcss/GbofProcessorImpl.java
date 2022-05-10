package mil.usmc.mls2.stratis.core.processor.gcss;

import exmlservice.I112.SalesorderOutCollection;
import exmlservice.I112UnMarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.stratis.common.util.Util;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.service.gcss.I136NiinService;
import mil.usmc.mls2.stratis.modules.gbof.application.model.GbofProcessResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
class GbofProcessorImpl implements GbofProcessor {

  protected static final String HOST_NAME = "GBOF";

  private final SiteInterfaceService siteInterfaceService;
  private final SiteInfoService siteInfoService;
  private final GcssMcImportsDataService gcssMcImportsDataService;
  private final RefDataloadLogService refDataloadLogService;
  private final ErrorQueueService errorQueueService;
  private final RefGbofService refGbofService;
  private final IssueService issueService;
  private final CustomerService customerService;
  private final NiinInfoService niinInfoService;
  private final I136NiinService i136NiinService;

  @Override
  public GbofProcessResult process() {
    var gbofProcessResult = new GbofProcessResult();
    try {
      saveDataloadLog("INFO: REF_GBOF  GCSS-MC STARTED", "INFO: REF_GBOF GCSS-MC STARTED", 0);
      setGbofStatus("RUNNING");

      //get the GCSSMC XML from the database for import
      val gcssMcImportsData = getLatestGcssMcImportsData();
      if (gcssMcImportsData == null) {
        saveDataloadLog("ERROR: REF_GBOF FAILED TO LOAD DUE TO NO XML", "ERROR: REF_GBOF GCSS-MC Missing XML", 0);
        setGbofStatus("NOXMLREADY");
        gbofProcessResult.success(false);
        return gbofProcessResult;
      }

      //clear previous GBOF data to allow import of new files data.
      refGbofService.deleteAll();

      //clear previous logs for gbof
      deleteRefDataloadLogsAndReferences();
      saveDataloadLog("INFO: REF_GBOF PREVIOUS DATA CLEARED, STARTING IMPORT", "INFO: REF_GBOF GCSS-MC IN PROGRESS", 0);

      //set the gcssMcImportsDataId in the gbofProcessResult now that we know we have one in gcssMcImportsData
      gbofProcessResult.gcssmcDataImportsId(gcssMcImportsData.getId());

      processGbof(gcssMcImportsData, gbofProcessResult);
    }
    catch (Exception e) {
      log.error("Error occurred during Gbof Processing", e);
      gbofProcessResult.success(false);
    }
    return gbofProcessResult;
  }

  private GbofProcessResult processGbof(GcssMcImportsData gcssMcImportsData, GbofProcessResult gbofProcessResult) {
    try {

      SalesorderOutCollection i112Collection = I112UnMarshaller.unmarshallDecompressedString(gcssMcImportsData.getXml());
      if (i112Collection == null) {
        saveDataloadLog("ERROR: REF_GBOF FAILED TO LOAD DUE TO NO XML COLLECTION", "ERROR: REF_GBOF GCSS-MC Missing XML COLLECTION", gcssMcImportsData.getId());
        setGbofStatus("XMLERR");
        gbofProcessResult.success(false);
        return gbofProcessResult;
      }

      val gbofList = i112Collection.getSalesorderOutRecord();

      gbofProcessResult.totalRecordsFromGcss(gbofList.size());
      gbofList.forEach(gbof -> processRecord(gbof, gbofProcessResult));

      saveDataloadLog("INFO: REF_GBOF Loading Complete (Part I of II)", "Complete", 0);
      setGbofStatus("GBOFLOADED");

      //get all the gbofrecords
      val gbofRecords = refGbofService.search(RefGbofSearchCriteria.builder().build());

      setGbofStatus("PROCESSING");

      val seeLog = processBackOrders(gbofRecords);

      setGbofStatus(seeLog ? "COMPLETED SEE LOG" : "COMPLETED");
      saveDataloadLog("INFO: REF_GBOF Complete." + gbofProcessResult.returnStatisticsAlways(), "INFO: REF_GBOF COMPLETED", 0);

      gcssMcImportsData.processingComplete();
      gcssMcImportsDataService.save(gcssMcImportsData);
      ignoreAllOldGcssData(gcssMcImportsData);
      gbofProcessResult.success(true);
      return gbofProcessResult;
    }
    catch (Exception e1) {
      saveDataloadLog("ERROR: SET STATUS TO FAILED " + e1, "ERROR: An exception occurred during load GCSS-MC", 0);
      setGbofStatus("FAILED");
      gcssMcImportsData.processingFailed();
      gcssMcImportsDataService.save(gcssMcImportsData);
      gbofProcessResult.success(false);
      return gbofProcessResult;
    }
  }

  private void processRecord(SalesorderOutCollection.SalesorderOutRecord gbof, GbofProcessResult gbofProcessResult) {
    try {
      if (StringUtils.length(gbof.getSDN()) == 14) {
        String requiredDeliveryDate = Util.cleanString(gbof.getRDD());
        requiredDeliveryDate = (requiredDeliveryDate.length() > 3) ? requiredDeliveryDate.substring(1, 4) : requiredDeliveryDate;

        //insert a RefGbof record
        val refGbof = RefGbof.builder()
            .documentIdentifierCode(gbof.getDIC())
            .documentNumber(gbof.getSDN())
            .routingIdentifierCode(gbof.getRIC())
            .nationalStockNumber(gbof.getNIIN()) //The DB Field is called National_Stock_number, but it only stores the NIIN.  Note: the db field is char(15) so it rPads with spaces.
            .unitOfIssue(gbof.getUOI())
            .adviceCode(gbof.getAdviceCode())
            .supplementaryAddress(gbof.getSupADD())
            .projectCode(gbof.getProj())
            .priorityDesignatorCode(gbof.getPri())
            .requiredDeliveryDate(requiredDeliveryDate)
            .transactionQuantity(gbof.getQTr().toString())
            .build();

        refGbofService.save(refGbof);

        gbofProcessResult.incrementRefGbofRecordsCreated();
      }
      else {
        gbofProcessResult.incrementRecordSkipped();
        saveDataloadLog("SKIP: REF_GBOF GCSS-MC skip row document number not long enough " + gbof.getSDN(), "SKIP: REF_GBOF GCSS-MC skip row document number not long enough", 0);
      }
    }
    catch (Exception e) {
      gbofProcessResult.incrementRefGbofRecordsFailed();
      log.error("Failed to insert RefGbof Record for {}", gbof.getNIIN());
    }
  }

  public boolean processBackOrders(Set<RefGbof> gbofRecords) {

    AtomicInteger totalCountProcessed = new AtomicInteger();
    AtomicInteger totalCount = new AtomicInteger();
    AtomicInteger totalCountMissing = new AtomicInteger();

    val documentNumbers = gbofRecords.stream().map(RefGbof::getDocumentNumber).collect(Collectors.toList());
    cleanupOldBackorders(documentNumbers);

    AtomicBoolean seeLog = new AtomicBoolean(false);

    gbofRecords.forEach(gbofRecord -> {
      Integer refGbofId = gbofRecord.getRefGbofId();
      String documentNumber = gbofRecord.getDocumentNumber();
      String transactionQuantity = gbofRecord.getTransactionQuantity();
      String aac = documentNumber.substring(0, 6); //note gbofRecord has an aac field, however its not populated from GCSS, so pull the aac out of the document number.

      String niin = StringUtils.trim(gbofRecord.getNationalStockNumber());

      Integer niinId;
      Optional<Customer> customer;
      try {
        customer = customerService.getByAac(aac);
        if (!customer.isPresent()) {
          saveDataloadLog("ERROR: REF_GBOF Document Number " + documentNumber + " : AAC not found",
              "ERROR:  Row not inserted refGbofId=" + refGbofId, refGbofId);
          seeLog.set(true);
        }

        niinId = niinProcessing(niin, documentNumber, refGbofId, seeLog);
        if (niinId != null) {
          val boHistCount = issueService.findCountOfBackOrdersForGbofProcessing(documentNumber);

          if (boHistCount == 0) {
            int priDesignator = NumberUtils.toInt(gbofRecord.getPriorityDesignatorCode(), 1);
            int priDesignatorGroup = 3;

            if (priDesignator < 4) {
              priDesignatorGroup = 1;
            }
            else if (priDesignator < 9) {
              priDesignatorGroup = 2;
            }

            val generatedDocumentNumber = issueService.getNextIssueScn(false);
            val issue = Issue.builder()
                .documentId("A5A")
                .documentNumber(documentNumber)
                .qty(Util.cleanInt(transactionQuantity))
                .issueType("B")
                .issuePriorityDesignator(String.valueOf(priDesignator))
                .issuePriorityGroup(String.valueOf(priDesignatorGroup))
                .customer(customer.orElse(null))
                .niinId(niinId)
                .status("BACKORDER")
                .createdBy(1)
                .cc("A")
                .rdd(gbofRecord.getRequiredDeliveryDate())
                .fundCode(gbofRecord.getFundCode())
                .dateBackOrdered(Util.getCurrentJulian(4))
                .scn(generatedDocumentNumber)
                .routingIdFrom(gbofRecord.getRoutingIdentifierCode())
                .supplementaryAddress(gbofRecord.getSupplementaryAddress())
                .signalCode(gbofRecord.getSignalCode())
                .qtyIssued(0) //database defaulted to 0, ImportExportGbofThread did not set.
                .build();

            issueService.save(issue);

            totalCountProcessed.getAndIncrement();
          }
          else {
            // ELSE it was filled previously
            totalCountMissing.getAndIncrement();

            createErrorQueueRecord(refGbofId);
            saveDataloadLog("INFO: REF_GBOF Document Number " + documentNumber + " Backorder previously filled or duplicate (see ISSUE or ISSUE_HIST) ",
                "INFO:  Row not inserted refGbofId=" + refGbofId, refGbofId);
            seeLog.set(true);
          }
        }
      }
      catch (Exception e) {
        log.error("Exception occurred with GBOF processing", e);
        totalCountMissing.getAndIncrement();
        //* do nothing - not a valid niin or aac
        saveDataloadLog("ERROR: REF_GBOF Document Number " + documentNumber + " : AAC and/or NIIN not found",
            "ERROR:  Row not inserted refGbofId=" + refGbofId, refGbofId);
        seeLog.set(true);
      }

      totalCount.getAndIncrement();
    });
    return seeLog.get();
  }

  private Integer niinProcessing(String niin, String documentNumber, Integer refGbofId, AtomicBoolean seeLog) {
    Integer niinId = null;
    if (niin != null && niin.length() == 9) {
      val niinInfo = niinInfoService.findByNiin(niin);

      if (!niinInfo.isPresent()) {
        val result = i136NiinService.processI136Niin(niin);
        if (!result.status().isSuccess()) {
          saveDataloadLog("ERROR: REF_GBOF Document Number " + documentNumber + " : NIIN not found - " + niin,
              "ERROR:  Row not inserted refGbofId=" + refGbofId, refGbofId);
          //We did not find the niin!  How can we create this backorder?
          //Create Exception for Exception Processing for this GBOF Record
          createErrorQueueRecord(refGbofId);
          seeLog.set(true);
        }
        else {
          //niin processed from gcss/migs successfully, so load it up now.
          val newNiinInfo = niinInfoService.findByNiin(niin);
          if (newNiinInfo.isPresent())
            niinId = newNiinInfo.get().getNiinId();
          else {
            saveDataloadLog("ERROR: REF_GBOF Document Number " + documentNumber + " : NIIN not found - " + niin,
                "ERROR:  Row not inserted refGbofId=" + refGbofId, refGbofId);
            //We did not find the niin!  How can we create this backorder?
            //Create Exception for Exception Processing for this GBOF Record
            createErrorQueueRecord(refGbofId);
            seeLog.set(true);
          }
        }
      }
      else {
        niinId = niinInfo.get().getNiinId();
      }
    }
    else {
      log.info("NIIN provided by GBOF is invalid {}", niin);
    }
    return niinId;
  }

  private void cleanupOldBackorders(List<String> documentNumbers) {
    try {

      val backOrderIssueSearchCriteria = IssueSearchCriteria.builder()
          .status("BACKORDER")
          .scns(documentNumbers)
          .build();

      issueService.deleteMultiple(backOrderIssueSearchCriteria);

      val createdBefore = OffsetDateTime.now().minusDays(1);
      val issuesCreatedBeforeSearchCriteria = IssueSearchCriteria.builder()
          .status("BACKORDER")
          .createdBefore(createdBefore)
          .build();

      issueService.deleteMultiple(issuesCreatedBeforeSearchCriteria);
    }
    catch (Exception clean) {
      log.error("Error Deleting Backorder Issues", clean);
    }
  }

  private void createErrorQueueRecord(Integer refGbofId) {
    try {
      val errorQueue = ErrorQueue.builder()
          .status("Open")
          .eid(27)
          .keyType("ref_gbof_id")
          .keyNum(refGbofId.toString())
          .createdBy(1)
          .createdDate(OffsetDateTime.now())
          .notes(null)
          .build();

      errorQueueService.save(errorQueue);
    }
    catch (Exception e) {
      log.error("Error creating Error Queue Record for {}", refGbofId, e);
    }
  }

  private void ignoreAllOldGcssData(GcssMcImportsData data) {
    gcssMcImportsDataService.updateIgnoreAllPreviousDataByInterface(data.getId(), HOST_NAME);
  }

  private void setGbofStatus(String status) {
    val siteInterface = siteInterfaceService.getByInterfaceName(HOST_NAME);
    if (siteInterface.isPresent()) {
      val site = siteInterface.get();
      site.setLastImpExpDate(OffsetDateTime.now());
      site.setStatus(status);
      siteInterfaceService.save(site);
    }
  }

  private GcssMcImportsData getLatestGcssMcImportsData() {
    val results = gcssMcImportsDataService.getMostRecentRecordForProcessing(HOST_NAME);
    return results.orElse(null);
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

    val refGbofCriteria = RefGbofSearchCriteria.builder()
        .build();
    val refGbofRecords = refGbofService.search(refGbofCriteria);
    val keysToNotMatch = refGbofRecords.stream()
        .map(s -> String.valueOf(s.getRefGbofId()))
        .collect(Collectors.toList());

    val importsDataToDelete = gcssMcImportsDataService.search(importsCriteriaForDelete);
    importsDataToDelete.forEach(gcssMcImportsDataService::delete);

    val errorQueueCriteriaForDelete = ErrorQueueCriteria.builder()
        .keyType("ref_gbof_id")
        .keyNumsToNotMatch(keysToNotMatch)
        .build();

    val errorQueuesToDelete = errorQueueService.search(errorQueueCriteriaForDelete);
    errorQueuesToDelete.forEach(errorQueueService::delete);
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
}
