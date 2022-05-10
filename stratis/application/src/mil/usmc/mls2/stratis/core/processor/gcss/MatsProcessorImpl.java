package mil.usmc.mls2.stratis.core.processor.gcss;

import com.mchange.lang.IntegerUtils;
import exmlservice.I112.SalesorderOutCollection;
import exmlservice.I112UnMarshaller;
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
import mil.usmc.mls2.stratis.core.service.IssueService;
import mil.usmc.mls2.stratis.core.service.gcss.I009SpoolService;
import mil.usmc.mls2.stratis.core.service.gcss.I136NiinService;
import mil.usmc.mls2.stratis.modules.mats.application.model.MatsProcessResult;
import mil.usmc.mls2.stratis.modules.workload.domain.model.PackingConsolidationInfo;
import mil.usmc.mls2.stratis.modules.workload.domain.model.PackingStationResult;
import mil.usmc.mls2.stratis.modules.workload.service.WorkloadService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
    //FIXME review transactions and cleanup.  This should be able to go full transactional now, not separate snow that there are no calls to ADF.
class MatsProcessorImpl implements MatsProcessor {

  private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
  private static final String HOST_NAME = "MATS";

  @Value("${stratis.mats.gcss.niin-info.single-call:true}")
  private boolean gcssSingleCall;

  @Value("${stratis.mats.packing-station-optimization.enabled:true}")
  private boolean packingStationOptimizationEnabled;

  private final I136NiinService i136NiinService;
  private final I009SpoolService i009SpoolService;
  private final IssueService issueService;
  private final SiteInterfaceRepository siteInterfaceRepository;
  private final SiteInfoRepository siteInfoRepository;
  private final NiinInfoRepository niinInfoRepository;
  private final RefMatsRepository refMatsRepository;
  private final GcssMcImportsDataRepository gcssMcImportsDataRepository;
  private final ErrorQueueRepository errorQueueRepository;
  private final RefDataloadLogRepository refDataloadLogRepository;
  private final CustomerRepository customerRepository;
  private final NiinLocationRepository niinLocationRepository;
  private final IssueRepository issueRepository;
  private final PickingRepository pickingRepository;
  private final DefaultTransactionExecutor transactionExecutor;
  private final WorkloadService workloadService;

  @PostConstruct
  private void postConstruct() {
    log.info("Mats Processor Initialized [GCSS Single Call:{}]", gcssSingleCall);
  }

  @Override
  @Transactional(readOnly = true)
  public MatsProcessResult process() {
    val gcssMcImportsData = gcssMcImportsDataRepository.getOldestRecordForProcessing(HOST_NAME).orElse(null);
    var matsProcessResult = new MatsProcessResult();
    try {
      val watch = new StopWatch();
      watch.start();

      txSaveDataloadLog("INFO: REF_MATS GCSS-MC STARTED", "INFO: REF_MATS GCSS-MC STARTED", 0);
      txSetMatsStatus("RUNNING");

      //* get the GCSSMC XML from the database for import
      if (gcssMcImportsData == null) {
        txSaveDataloadLog("ERROR: REF_MATS GCSS-MC FAILED TO LOAD DUE TO NO XML", "ERROR: REF_MATS GCSS-MC Missing XML, wait until MATS XML ready", 0);
        txSetMatsStatus("NOXMLREADY");
        matsProcessResult.success(false);
        return matsProcessResult;
      }

      //set the gcssMcImportsDataId in the matsProcessResult now that we know we have one in gcssMcImportsData
      matsProcessResult.gcssmcDataImportsId(gcssMcImportsData.getId());

      val i112Collection = I112UnMarshaller.unmarshallDecompressedString(gcssMcImportsData.getXml());
      if (i112Collection == null) {
        txSaveDataloadLog("ERROR: REF_MATS FAILED TO LOAD DUE TO NO XML COLLECTION", "ERROR: REF_MATS GCSS-MC Missing XML COLLECTION", gcssMcImportsData.getId());
        txSetMatsStatus("XMLERR");
        matsProcessResult.success(false);
        return matsProcessResult;
      }

      txTruncateRefMats();
      txDeleteRefDataloadLogsAndReferences();
      EntityManagerUtils.clear();

      txSaveDataloadLog("INFO: REF_MATS PREVIOUS DATA CLEARED, STARTING IMPORT", "INFO: REF_MATS GCSS-MC IN PROGRESS", 0);

      val result = processMatsFeed(i112Collection, matsProcessResult);

      // Finalize
      transactionExecutor.execute("gcssMcImportData-complete", () -> {
        gcssMcImportsData.processingComplete();
        gcssMcImportsDataRepository.save(gcssMcImportsData);
      });
      txSetMatsStatus(matsProcessResult.seeLog() ? "COMPLETED SEE LOG" : "COMPLETED");

      watch.stop();
      log.debug("MATS execution complete [durationSecs: {}]", watch.getTotalTimeSeconds());
      log.debug("Mats Results [{}]", result);
      return result;
    }
    catch (Exception e) {
      txSaveDataloadLog("ERROR: SET STATUS TO FAILED " + e, "ERROR: An exception occurred during load GCSS-MC", 0);
      txSetMatsStatus("FAILED");
      if (gcssMcImportsData != null) {
        transactionExecutor.execute("gcssMcImportData-failed", () -> {
          gcssMcImportsData.processingFailed();
          gcssMcImportsDataRepository.save(gcssMcImportsData);
        });
      }
      matsProcessResult.success(false);
      return matsProcessResult;
    }
  }

  private MatsProcessResult processMatsFeed(SalesorderOutCollection i112Collection, MatsProcessResult matsProcessResult) {
    try {
      val siteInfo = siteInfoRepository.getRecord().orElseThrow(() -> new StratisRuntimeException("Site Information not found"));
      val matsList = i112Collection.getSalesorderOutRecord();

      matsProcessResult.totalRecordsFromGcss(matsList.size());

      // Generate MATS Feed Records
      transactionExecutor.execute("create-ref-mats", () -> matsList.forEach(mats -> {
        val successful = processFeedRecords(mats, siteInfo);
        if (successful) matsProcessResult.incrementRefMatsRecordsCreated();
        else matsProcessResult.incrementRefMatsRecordsFailed();
      }));
      EntityManagerUtils.clear();

      txSaveDataloadLog(String.format("INFO: REF_MATS Loading Complete (Part I of II) [GCSS Count: %s Inserted: %s Failed: %s]", matsProcessResult.totalRecordsFromGcss(), matsProcessResult.refMatsRecordsCreated(), matsProcessResult.refMatsRecordsFailed()),
          "INFO: REF_MATS LOADED", matsProcessResult.totalRecordsFromGcss());

      processMatsRecords(siteInfo, matsProcessResult);
      EntityManagerUtils.clear();

      matsProcessResult.success(true);
      return matsProcessResult;
    }
    catch (Exception e) {
      txSaveDataloadLog("ERROR: SET STATUS TO FAILED " + e, "ERROR: An exception occurred during load GCSS-MC ", 0);
      log.error("An error occurred during the processing of MATS", e);
      txSetMatsStatus("FAILED");
      matsProcessResult.success(false);
      return matsProcessResult;
    }
  }

  private boolean processFeedRecords(SalesorderOutCollection.SalesorderOutRecord mats, SiteInfo siteInfo) {
    try {

      if (StringUtils.length(mats.getSDN()) != 14) {
        txSaveDataloadLog("SKIP: REF_MATS GCSS-MC skip row document number not long enough " + mats.getSDN(), "SKIP: REF_MATS GCSS-MC skip row document number not long enough", 0);
        return false;
      }

      if (!StringUtils.equalsIgnoreCase(mats.getRIC(), siteInfo.getRoutingId())) {
        txSaveDataloadLog("SKIP: REF_MATS GCSS-MC skip row RIC does not match site " + mats.getRIC(), "SKIP: REF_MATS GCSS-MC skip row RIC does not match site", 0);
        return false;
      }

      //Add a RefMats record
      val refMats = RefMats
          .builder()
          .documentIdentifier(mats.getDIC())
          .documentNumber(mats.getSDN())
          .routingIdentifierFrom(mats.getRIC())
          .niin(mats.getNIIN())
          .unitOfIssue(mats.getUOI())
          .signalCode(mats.getSC())
          .supplementaryAddress(mats.getSupADD())
          .projectCode(mats.getProj())
          .issuePriorityDesignator(mats.getPri())
          .adviceCode(mats.getAdviceCode())
          .conditionCode(mats.getCC())
          .requiredDeliveryDate(mats.getRDD())
          .shipToAddress1(mats.getShipToLine1())
          .shipToAddress2(mats.getShipToLine2())
          .shipToAddress3(mats.getShipToLine3())
          .shipToAddress4(mats.getShipToLine4())
          .shipToAddressCity(mats.getShipToCity())
          .shipToAddressState(mats.getShipToState())
          .shipToAddressZipCode(mats.getShipToZipCode())
          .shipToAddressCountry(mats.getShipToCountry())
          .transactionQuantity(mats.getQTr().intValue())
          .disposalCode(mats.getDisposalAuthority())
          .distributionCode(mats.getDistC())
          .fundCode(mats.getFund())
          .routingIdentifierTo(mats.getRICT())
          .demandSuffixCode(mats.getSfx())
          .demilCode(mats.getDemilC())
          .build();

      refMatsRepository.save(refMats);
      return true;
    }
    catch (Exception e) {
      log.error("Failed to insert RefMats Record for {}", mats.getNIIN());
      return false;
    }
  }

  private void processMatsRecords(SiteInfo siteInfo, MatsProcessResult matsProcessResult) {
    try {
      txPrimeNiinInfos();

      txSaveDataloadLog("INFO: REF_MATS Processing Started", "", matsProcessResult.totalRecordsFromGcss());

      val matsRecords = new ArrayList<>(refMatsRepository.findAll());
      matsRecords.sort(Comparator.comparing(RefMats::getTransactionQuantity).reversed().thenComparing(RefMats::getRefMatsId)); //FUTURE verify the chained refMatsId comparator is meaningful

      val stationProcessingCache = new HashMap<Integer, PackingConsolidationInfo>();
      matsRecords.forEach(x -> processRecord(x, siteInfo, matsProcessResult, stationProcessingCache));

      txSaveDataloadLog(String.format("INFO: REF_MATS Processing Completed (Part II of II) %s", matsProcessResult.returnStatisticsAlways()),
          "INFO: REF_MATS COMPLETED", 0);
    }
    catch (Exception e) {
      matsProcessResult.seeLog(true);
      txSaveDataloadLog(String.format("INFO: REF_MATS Processing Completed With Errors (Part II of II) %s", matsProcessResult.returnStatisticsAlways()),
          "INFO: REF_MATS COMPLETED WITH ERRORS", 0);
    }
  }

  private void processRecord(RefMats matsRecord, SiteInfo siteInfo, MatsProcessResult matsProcessResult, HashMap<Integer, PackingConsolidationInfo> stationProcessingCache) {
    if (!matsRecord.isValidConditionCode()) {
      txCreateErrorQueueRecord(matsRecord.getRefMatsId());
      matsProcessResult.incrementErrorQueueCreated();
      return;
    }

    //resolve customer
    Customer customer;
    try {
      customer = createCustomerIfNecessary(matsRecord, matsProcessResult);
      if (StringUtils.isNotEmpty(matsRecord.getSignalCode())) {
        customer = checkSignalCodeAndCreateCustomerIfNecessary(matsRecord, customer, matsProcessResult);
      }
    }
    catch (Exception e) {
      log.error("Exception occurred while checking and creating Customer", e);
      txSaveDataloadLog(String.format("ERROR: Row not inserted (CUSTOMER CREATION FAILED) - document number %s", matsRecord.getDocumentNumber()), "ERROR: Row not inserted [REF_MATS_ID=" + matsRecord.getRefMatsId() + "]", 0);

      matsProcessResult.incrementRecordFailures();
      matsProcessResult.seeLog(true);
      matsProcessResult.addMessage(String.format("Record not processed, could not create necessary Customers for Document Number %s", matsRecord.getDocumentNumber()));
      return;
    }

    // process niin
    val niinId = niinInfoRepository.findByNiin(matsRecord.getNiin()).map(NiinInfo::getNiinId).orElse(null);
    if (niinId != null) {
      val niinOnHand = isNiinInWarehouse(matsRecord.getNiin(), matsRecord.getConditionCode());
      val scn = createIssueIfNotExists(matsRecord, siteInfo, customer, niinId, matsProcessResult);

      if (scn != null) {
        if ("WALKTHRUCONVERT".equalsIgnoreCase(scn)) {
          txSaveDataloadLog(String.format("WARNING: Processing stopped for record (WALKTHRU CONVERTED) - document number %s", matsRecord.getDocumentNumber()), "Issue not created for [REF_MATS_ID=" + matsRecord.getRefMatsId() + "]", 0);
        }
        else if ("WALKTHRUEXCEPTION".equalsIgnoreCase(scn)) {
          txSaveDataloadLog(String.format("WARNING: Processing stopped for record (WALKTHRU EXCEPTION NIIN MISMATCH) - document number %s", matsRecord.getDocumentNumber()), "Issue not created for [REF_MATS_ID=" + matsRecord.getRefMatsId() + "]", 0);
          matsProcessResult.seeLog(true);
        }
        else {
          // * issue creation successful, create picks for mats
          txCreatePicksForMATS(scn, matsRecord, niinOnHand, stationProcessingCache, matsProcessResult);
        }
      }
      else {
        txSaveDataloadLog(String.format("WARNING: Procesing stopped for record (ISSUE/ISSUE_HIST ALREADY EXIST) - document number %s", matsRecord.getDocumentNumber()), "Issue not created for [REF_MATS_ID=" + matsRecord.getRefMatsId() + "]", 0);
        matsProcessResult.seeLog(true);
      }
    }
    else {
      //* niin was not found in niin_location nor niin_info, and if found in ref_mhif, niin failed creation
      //* check issue and issue_hist for document number to ensure we don't send duplicate Z7KDenials for existing issue
      checkHistory(matsRecord.getDocumentNumber(), matsRecord.getDemandSuffixCode(), matsRecord.getRefMatsId());
      txSaveDataloadLog(String.format("ERROR: Row not inserted (Niin Not found %s)", matsRecord.getNiin()), "Row not Inserted", matsRecord.getRefMatsId());
      matsProcessResult.addMessage(String.format("Niin: %s Not found, Row will not be processed for RefMatsId: %s", matsRecord.getNiin(), matsRecord.getRefMatsId()));
      matsProcessResult.incrementRecordFailures();
      matsProcessResult.seeLog(true);
    }
  }

  private boolean isNiinInWarehouse(String niin, String conditionCode) {
    val niinCriteria = NiinLocationSearchCriteria
        .builder()
        .cc(conditionCode)
        .niin(niin)
        .build();

    return niinLocationRepository.count(niinCriteria) > 0;
  }

  private void checkHistory(String documentNumber, String suffix, int refMatsId) {
    String warningMessage = "";
    try {
      val issueSearchCriteria = IssueSearchCriteria
          .builder()
          .documentNumber(documentNumber)
          .suffix(suffix)
          .build();

      val issueCount = issueRepository.count(issueSearchCriteria);
      if (issueCount > 0) {
        warningMessage = " AND DOCUMENT NUMBER IS AN EXISTING ISSUE";  //* maybe document number for a different niin?
      }
      else {
        val historyCount = issueRepository.getHistoryCount(documentNumber, suffix, null);
        if (historyCount > 0) {
          warningMessage = " AND DOCUMENT NUMBER IS AN EXISTING ISSUE";  //* may be document number for a different niin
        }
      }
    }
    catch (Exception e) {
      log.error("Error calculating issue count", e);
    }

    txSaveDataloadLog("WARNING: Row not inserted (NIIN NOT FOUND" + warningMessage + ") - document number " + documentNumber, "WARNING: Row not inserted [REF_MATS_ID=" + refMatsId + "]", 0);
  }

  private void txCreatePicksForMATS(String scn, RefMats matsRecord, boolean niinOnHand, HashMap<Integer, PackingConsolidationInfo> stationProcessingCache, MatsProcessResult matsProcessResult) {
    val task = (Runnable) () -> {
      createPicksForMATS(scn, matsRecord, niinOnHand, stationProcessingCache, matsProcessResult);
    };

    transactionExecutor.execute("txCreatePicksForMATS", task);
  }

  private void createPicksForMATS(String scn, RefMats matsRecord, boolean niinOnHand, HashMap<Integer, PackingConsolidationInfo> stationProcessingCache, MatsProcessResult matsProcessResult) {
    int qtyNeededToCreatePicksFor = matsRecord.getTransactionQuantity();
    if (qtyNeededToCreatePicksFor == 0) return;

    val conditionCode = matsRecord.getConditionCode();
    int qtyOfPicksCreated = 0;
    int currentSuffix = 0;

    try {
      val niinLocationCriteria = NiinLocationSearchCriteria
          .builder()
          .cc(conditionCode)
          .niin(matsRecord.getNiin())
          .locked("N")
          .qtyGreaterThenZero(true)
          .build();

      val niinLocationResults = niinLocationRepository.search(niinLocationCriteria);
      niinLocationResults.sort(Comparator.comparing(NiinLocation::getQty).reversed());
      boolean errorFoundInLoop = false;

      //create all possible picks
      for (val niinLocation : niinLocationResults) {
        if (qtyOfPicksCreated < qtyNeededToCreatePicksFor) {
          val locationId = niinLocation.getNiinLocationId();
          Integer quantityAvailableForPick = niinLocation.getQty();

          //reduce actual availability by picks already reserved for this location
          val picks = findValidPicksByNiinLocation(niinLocation);
          if (CollectionUtils.isNotEmpty(picks)) {
            val alreadyAllocatedQtyForPicks = picks.stream().mapToInt(Picking::getPickQty).sum();
            quantityAvailableForPick -= alreadyAllocatedQtyForPicks;
          }

          int lineNumberForError = 1;

          //if the quantity able to be picked from this location is > 0 create then pick and select a packing station; otherwise do nothing, and try the next location.
          if (quantityAvailableForPick == 0) continue;

          //set Quantity to amount needed if its more then required.
          val qtyRemainingToCreate = qtyNeededToCreatePicksFor - qtyOfPicksCreated;

          var pickQty = Math.min(quantityAvailableForPick, qtyRemainingToCreate);

          if (quantityAvailableForPick >= pickQty) lineNumberForError = 2;

          val newPick = txInsertPicking(niinLocation, pickQty, scn, currentSuffix++, "PICK READY");
          if (newPick == null) {
            matsProcessResult.incrementRecordFailures();
            log.warn("Insert pick FAILED for locid: {} qty: {} scn: {}", locationId, pickQty, scn);
            errorFoundInLoop = true;
            break;
          }
          else {
            matsProcessResult.incrementPicksCreated();
            //FUTURE Backlog in another pass, convert this call from ADF to spring only.
            val systemUser = 1;
            PackingStationResult packingStationResult = null;
            if (packingStationOptimizationEnabled) {
              val lastProcessedProvider = (Function<Integer, PackingConsolidationInfo>) stationProcessingCache::get;
              packingStationResult = workloadService.getPackingStation(newPick, systemUser, newPick.getPickQty(), 0, lastProcessedProvider);
              if (packingStationResult.packingConsolidationInfo() != null) {
                stationProcessingCache.put(packingStationResult.packingConsolidationInfo().packingStationId(), packingStationResult.packingConsolidationInfo());
              }
            }
            else {
              packingStationResult = workloadService.getPackingStation(newPick, systemUser, newPick.getPickQty(), 0);
            }

            val packingStation = packingStationResult.packingStationName();
            if (packingStation.contains("Error")) {
              errorFoundInLoop = true;
              txSaveDataloadLog(String.format("PACKING STATION ERROR: %s", packingStation), "PID " + newPick.getPid(), lineNumberForError);
              // end this loop
              break;
            }
            else {
              qtyOfPicksCreated += newPick.getPickQty();
            }
          }
        }
        else {
          // end this loop
          break;
        }
      }

      //Create refusal pick entry if we don't have enough inventory for required picks
      if (!errorFoundInLoop && qtyOfPicksCreated < qtyNeededToCreatePicksFor) {
        createRefusalPickEntry(matsRecord, scn, currentSuffix, niinOnHand, qtyNeededToCreatePicksFor, qtyOfPicksCreated);
        matsProcessResult.incrementPickRefusalsCreated();
        matsProcessResult.seeLog(true);
      }
    }
    catch (Exception e) {
      log.error("Exception Occurred creating Picks for MATS", e);
      processErrorFromCreatePicksForMATS(scn, matsRecord, matsProcessResult);
    }
  }

  private PackingStationResult txFindPackingStation(Picking newPick, Integer systemUser, Function<Integer, PackingConsolidationInfo> lastProcessedProvider) {
    val task = (Callable<PackingStationResult>) () -> {
      return workloadService.getPackingStation(newPick, systemUser, newPick.getPickQty(), 0, lastProcessedProvider);
    };

    return transactionExecutor.execute("txFindPackingStation", task);
  }

  private List<Picking> findValidPicksByNiinLocation(NiinLocation niinLocation) {
    val pickingStatuses = new ArrayList<String>();
    pickingStatuses.add("PICK READY");
    pickingStatuses.add("PICK BYPASS1");
    pickingStatuses.add("PICK BYPASS2");

    val pickingCriteria = PickingSearchCriteria
        .builder()
        .statuses(pickingStatuses)
        .niinLocationId(niinLocation.getNiinLocationId())
        .build();

    return pickingRepository.search(pickingCriteria);
  }

  private void createRefusalPickEntry(RefMats matsRecord, String scn, int currentSuffix, boolean niinOnHand, int qtyNeededToCreatePicksFor, int qtyOfPicksCreated) {
    val quantityToRefuse = (qtyNeededToCreatePicksFor - qtyOfPicksCreated);
    val newPick = txInsertPicking(null, quantityToRefuse, scn, currentSuffix, "PICK REFUSED");
    if (newPick == null) throw new IllegalStateException("Unable to create refusal pick");

    final String label;
    if (!niinOnHand) {
      //* this was not an existing issue, ok to send Z7K denial
      label = "INFO: NIIN NOT FOUND ON HAND REFUSAL - document number " + matsRecord.getDocumentNumber();
    }
    else {
      //* may be a partial refusal or full refusal
      label = "INFO: NIIN ON HAND PARTIAL REFUSAL - document number " + matsRecord.getDocumentNumber();
    }

    txSaveDataloadLog(label, "INFO: Go to Refusal Buffer [REF_MATS_ID=" + matsRecord.getRefMatsId() + "]", 0);
  }

  private void processErrorFromCreatePicksForMATS(String scn, RefMats matsRecord, MatsProcessResult matsProcessResult) {
    txSaveDataloadLog(String.format("ERROR: Row not inserted (INSERT PICKING FAILED) - document number %s", matsRecord.getDocumentNumber()), "ERROR: Row not inserted [REF_MATS_ID=" + matsRecord.getRefMatsId() + "]", 0);
    txDeletePickingIssueAndIssueHist(scn);
    matsProcessResult.incrementRecordFailures();
    matsProcessResult.seeLog(true);
    matsProcessResult.addMessage(String.format("Insert Picking failed, so rolling back Issues and picking inserts for Document Number: %s", matsRecord.getDocumentNumber()));
  }

  private void txDeletePickingIssueAndIssueHist(String scn) {
    val task = (Runnable) () -> {
      val createdDateThreshold = OffsetDateTime.now().minusDays(1);
      pickingRepository.deleteByScn(scn);
      issueRepository.deleteByScn(scn);
      pickingRepository.deleteHistoryByScnAndDate(scn, createdDateThreshold);
      issueRepository.deleteHistoryByScnAndDate(scn, createdDateThreshold);
    };

    transactionExecutor.execute("txDeletePickingIssueAndIssueHist", task);
  }

  private String createIssueIfNotExists(RefMats matsRecord, SiteInfo siteInfo, Customer customer, Integer niinId, MatsProcessResult matsProcessResult) {
    String scn = null;

    try {
      val issueCriteria = IssueSearchCriteria.builder()
          .documentNumber(matsRecord.getDocumentNumber())
          .suffix(matsRecord.getDemandSuffixCode())
          .issueType("W")
          .allowNullIssueType(true)
          .build();

      val issuesFound = issueRepository.search(issueCriteria);
      val issueHistoryCount = issueRepository.getHistoryCount(matsRecord.getDocumentNumber(), matsRecord.getDemandSuffixCode(), "W");

      if (CollectionUtils.isNotEmpty(issuesFound)) {
        //Making same assumption as legacy logic via this note: //MCF: Making the assumption that there will be no duplicate Document_Numbers. There are checks in pace to make sure this doesn't happen in the Issue table.
        val issue = issuesFound.get(0);
        if ("W".equals(issue.getIssueType())) {
          if (issue.getNiinId().equals(niinId)) {
            txConvertWalkThru(issue, siteInfo);
            scn = "WALKTHRUCONVERT";
          }
          else {
            scn = "WALKTHRUEXCEPTION";
            //FUTURE INNOV Backlog await answer from Kane on this.  Error ID of 82 is invalid according to the database.
            //            createErrorQueueRecord(82);
            //            createException(oldniinid, Integer.parseInt(niinid.toString()), documentnumber.toString());
          }
        }
      }
      else if (issueHistoryCount == 0) {
        val priDesignator = IntegerUtils.parseInt(matsRecord.getIssuePriorityDesignator(), 1);
        val priDesignatorGroup = calcPriorityDesignatorGroup(priDesignator);
        scn = txCreateIssue(matsRecord, siteInfo, customer, niinId, priDesignatorGroup, matsProcessResult);
      }
    }
    catch (Exception e) {
      matsProcessResult.addMessage(String.format("Issue not created for Document Number %s", matsRecord.getDocumentNumber()));
      log.error("Error during issue creation. Issue not created.", e);
    }
    return scn;
  }

  private String txCreateIssue(RefMats matsRecord, SiteInfo siteInfo, Customer customer, Integer niinId, String priorityDesignatorGroup, MatsProcessResult matsProcessResult) {
    val task = (Callable<String>) () -> {
      val scn = issueService.getNextIssueScn(false);
      val issue = Issue
          .builder()
          .documentId(matsRecord.getDocumentIdentifier())
          .documentNumber(matsRecord.getDocumentNumber())
          .qty(matsRecord.getTransactionQuantity())
          .issueType(null)
          .issuePriorityDesignator(matsRecord.getIssuePriorityDesignator())
          .issuePriorityGroup(priorityDesignatorGroup)
          .customer(customer)
          .niinId(niinId)
          .status("PICK READY")
          .createdBy(1)
          .suffix(matsRecord.getDemandSuffixCode())
          .cc(StringUtils.isEmpty(matsRecord.getConditionCode()) ? "A" : matsRecord.getConditionCode())
          .rdd(StringUtils.isEmpty(matsRecord.getRequiredDeliveryDate()) ? Util.getCurrentJulian(3) : matsRecord.getRequiredDeliveryDate())
          .fundCode(matsRecord.getFundCode())
          .scn(scn)
          .routingIdFrom(StringUtils.isEmpty(matsRecord.getRoutingIdentifierFrom()) ? siteInfo.getRoutingId() : matsRecord.getRoutingIdentifierFrom())
          .supplementaryAddress(matsRecord.getSupplementaryAddress())
          .signalCode(StringUtils.isEmpty(matsRecord.getSignalCode()) ? "A" : matsRecord.getSignalCode())
          .qtyIssued(matsRecord.getTransactionQuantity())
          .adviceCode(matsRecord.getAdviceCode())
          .costJon(null)
          .mediaAndStatusCode(matsRecord.getMediaAndStatusCode())
          .demilCode(matsRecord.getDemilCode())
          .build();

      issueRepository.save(issue);
      matsProcessResult.incrementIssuesCreated();
      return scn;
    };

    return transactionExecutor.execute("txCreateIssue", task);
  }

  //SROG, AE1, Asx order may be important when sending data to GCSS (i009Spooling)
  private void txConvertWalkThru(Issue issue, SiteInfo siteInfo) {
    val task = (Runnable) () -> {
      val scn = issue.getScn();
      val picks = pickingRepository.findByScn(scn);

      //Send SROG for each pick
      picks.forEach(pick -> i009SpoolService.sendSROGCSSMCTransaction(siteInfo, pick));

      //Send AE1
      i009SpoolService.sendAE1GCSSMCTransaction(siteInfo, scn);

      //Send AS1
      i009SpoolService.sendAsxTrans(siteInfo, issue, 3);

      //Flag as walkthrough issue
      issue.convertWalkthrough(1);
      issueRepository.save(issue);

      //Delete all associated picks
      picks.forEach(pickingRepository::delete);
      issueRepository.delete(issue);
    };

    transactionExecutor.execute("txConvertWalkThru", task);
  }

  private void txPrimeNiinInfos() {
    val task = (Runnable) () -> {
      val niinsToFind = refMatsRepository.findMissingNiins();

      if (niinsToFind.size() > 0) {
        if (gcssSingleCall) {
          val result = i136NiinService.processI136Niins(niinsToFind);
          txSaveDataloadLog(String.format("INFO: REF_MATS LOADED NIINs from GCSS %s [Total NIINs: %s NIINs Processed: %s NIINs Skipped: %s NIINs Errored: %s]", result.status().getLabel(), result.totalNiins(), result.niinsProcessed(), result.niinsSkipped(), result.niinsErrored()),
              result.status().getLabel(), 0);
          log.debug("Sending NIINS to GCSS using single-call: [count: {}, niins: {}]", niinsToFind.size(), niinsToFind);
        }
        else {
          log.debug("Sending NIINS to GCSS using multi-call: [count: {}, niins: {}]", niinsToFind.size(), niinsToFind);
          niinsToFind.forEach(niin -> {
            val result = i136NiinService.processI136Niin(niin);
            txSaveDataloadLog(String.format("INFO: REF_MATS LOADED NIIN %s from GCSS", niin),
                result.status().getLabel(), 0);
          });
        }
      }
    };

    transactionExecutor.execute("txProcessNiinInfoWithGCSS", task);
  }

  private String calcPriorityDesignatorGroup(int issuePriorityDesignator) {
    if ((issuePriorityDesignator == 1) || (issuePriorityDesignator == 2) ||
        (issuePriorityDesignator == 3) || (issuePriorityDesignator == 7) ||
        (issuePriorityDesignator == 8)) {
      return "1";
    }
    else if ((issuePriorityDesignator == 4) || (issuePriorityDesignator == 5) ||
        (issuePriorityDesignator == 6) || (issuePriorityDesignator == 9) || (issuePriorityDesignator == 10)) {
      return "2";
    }
    return "3";
  }

  private Customer checkSignalCodeAndCreateCustomerIfNecessary(RefMats matsRecord, Customer customer, MatsProcessResult matsProcessResult) {
    val signalCode = matsRecord.getSignalCode();
    val supplementaryAddress = StringUtils.isNotEmpty(matsRecord.getSupplementaryAddress()) ? matsRecord.getSupplementaryAddress().toUpperCase().trim() : null;
    Customer customerToReturn = customer;
    String warningMessage = null;

    val relevantSignalCodes = new HashSet<>(Arrays.asList("J", "K", "M", "X"));
    if (relevantSignalCodes.contains(signalCode.toUpperCase())) {
      //* use supplementary address for customer
      if (!StringUtils.isEmpty(supplementaryAddress)) {
        if (!supplementaryAddress.startsWith("Y") && supplementaryAddress.length() == 6) {
          //return existing customer on matching AAC.
          if (customer.getAac().equalsIgnoreCase(supplementaryAddress)) return customer;

          val foundSupplementaryCustomer = customerRepository.getByAac(supplementaryAddress);
          customerToReturn = foundSupplementaryCustomer.orElseGet(() -> txCreateCustomer(supplementaryAddress, "TBD", "TBD", "TBD", "TBD", "TBD", "TBD", matsProcessResult));
        }
        else {
          warningMessage = String.format("WARNING: Not inserting Supplementary Address Customer (SUPP ADDR STARTS WITH Y or NOT 6 LENGTH) - document number %s", matsRecord.getDocumentNumber());
        }
      }
      else {
        warningMessage = String.format("WARNING: Not inserting Supplementary Address Customer (SUPP ADDR MISSING) - document number %s", matsRecord.getDocumentNumber());
      }
    }

    if (warningMessage != null && customer.getCustomerId() != null) {
      txSaveDataloadLog(warningMessage, "WARNING: Not inserting Supplementary Address Customer [REF_MATS_ID=" + matsRecord.getRefMatsId() + "]", matsProcessResult.totalRecordsFromGcss());
    }

    return customerToReturn;
  }

  private Picking txInsertPicking(NiinLocation niinLocation, int qty, String scn, int suffix, String status) {
    val task = (Callable<Picking>) () -> {
      val newPick = Picking
          .builder()
          .scn(scn)
          .niinLocation(niinLocation)
          .createdBy(1)
          .createdDate(OffsetDateTime.now())
          .pickQty(qty)
          .qtyPicked(0)
          .status(status)
          .bypassCount(0)
          .suffixCode(Integer.toString(suffix))
          .pin(calculatePin(scn, suffix))
          .refusedFlag("N")
          .securityMarkClass("FOUO")
          .build();
      pickingRepository.save(newPick);
      return newPick;
    };

    return transactionExecutor.execute("txInsertPicking", task);
  }

  private void txCreateErrorQueueRecord(Integer refMatsId) {
    val task = (Runnable) () -> {
      val errorQueue = ErrorQueue.builder()
          .status("Open")
          .eid(55)
          .keyType("ref_mats_id")
          .keyNum(refMatsId.toString())
          .createdBy(1)
          .createdDate(OffsetDateTime.now())
          .notes(null)
          .build();

      errorQueueRepository.save(errorQueue);
    };

    transactionExecutor.execute("txCreateErrorQueueRecord", task, e -> log.error("Error creating Error Queue Record for {}", refMatsId, e));
  }

  private Customer createCustomerIfNecessary(RefMats matsRecord, MatsProcessResult matsProcessResult) {
    val aac = matsRecord.getDocumentNumber().substring(0, 6);
    val customer = customerRepository.getByAac(aac).orElse(null);
    if (customer != null) return customer;

    //Note: MatsRecord erroneously names columns.  ShipToAddress1 is actually the name.  and 2 -> 1, and 3 -> 2 for customer records.
    val stateToUser = StringUtils.isNotEmpty(matsRecord.getShipToAddressCountry()) ? matsRecord.getShipToAddressCountry() : matsRecord.getShipToAddressState();
    return txCreateCustomer(aac, matsRecord.getShipToAddress1(), matsRecord.getShipToAddress2(), matsRecord.getShipToAddress3(), matsRecord.getShipToAddressCity(), stateToUser, matsRecord.getShipToAddressZipCode(), matsProcessResult);
  }

  private Customer txCreateCustomer(String aac, String name, String address1, String address2, String city, String state, String zipCode, MatsProcessResult matsProcessResult) {
    val task = (Callable<Customer>) () -> {
      val newCustomer = Customer.builder()
          .aac(aac)
          .name(StringUtils.isEmpty(name) ? "TBD" : name)
          .address1(address1)
          .address2(address2)
          .city(city)
          .state(state)
          .zipCode(zipCode)
          .restrictShip("N")
          .build();

      customerRepository.save(newCustomer);
      matsProcessResult.incrementCustomersCreated();
      return newCustomer;
    };

    return transactionExecutor.execute("txCreateCustomer", task);
  }

  private void txSetMatsStatus(String status) {
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

  private void txTruncateRefMats() {
    transactionExecutor.execute("txTruncateRefMats", refMatsRepository::truncate);
  }

  private void txSaveDataloadLog(String dataRow, String description, int lineNumber) {
    val task = (Runnable) () -> {
      val refDataloadLog = RefDataloadLog.builder()
          .interfaceName(HOST_NAME)
          .createdDate(OffsetDateTime.now())
          .createdBy("1") //system user
          .dataRow(dataRow)
          .description(description)
          .lineNumber(lineNumber)
          .build();

      refDataloadLogRepository.save(refDataloadLog);
    };

    transactionExecutor.execute("txSaveDataloadLog", task);
  }

  private void txDeleteRefDataloadLogsAndReferences() {
    val task = (Runnable) () -> {
      val siteInfo = siteInfoRepository.getRecord().orElseThrow(() -> new StratisRuntimeException("Site Information not found"));

      val now = OffsetDateTime.now();
      int gcssLogClear = siteInfo.getGcssmcLogClear() != null ? siteInfo.getGcssmcLogClear() : 7;
      int refLogClear = siteInfo.getRefLogClear() != null ? siteInfo.getRefLogClear() : 1;
      val gcssCreatedBefore = now.minusDays(gcssLogClear);
      val refCreatedBefore = now.minusDays(refLogClear);

      clearOldRefDataloadLogs(refCreatedBefore);
      clearOldGcssmcImportsData(gcssCreatedBefore);
      clearErrorQueuesForMats();
    };

    transactionExecutor.execute("txDeleteRefDataloadLogsAndReferences", task);
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

  private void clearErrorQueuesForMats() {
    val errorQueueCriteriaForDelete = ErrorQueueCriteria.builder()
        .keyType("ref_mats_id")
        .build();
    errorQueueRepository.delete(errorQueueCriteriaForDelete);
    EntityManagerUtils.flushAndClear();
  }

  private String calculatePin(String scn, Integer suffix) {
    //should return A-Z based on numbers 10 - 35 (if > 10)
    val alphaSuffix = (suffix > 10) ? String.valueOf(ALPHABET.charAt(suffix - 9)).toUpperCase() : "";
    val pinSuffix = (suffix > 0 && suffix < 10) ? Integer.toString(suffix) : alphaSuffix;
    return scn + pinSuffix;
  }
}
