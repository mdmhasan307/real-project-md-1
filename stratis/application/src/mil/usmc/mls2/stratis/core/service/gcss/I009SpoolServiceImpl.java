package mil.usmc.mls2.stratis.core.service.gcss;

import exmlservice.I009.ObjectFactory;
import exmlservice.I009.ShipmentReceiptsInCollection;
import exmlservice.I009Marshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
class I009SpoolServiceImpl implements I009SpoolService {

  private final SpoolService spoolService;
  private final IssueService issueService;
  private final NiinInfoService niinInfoService;
  private final PickSerialLotNumService pickSerialLotNumService;
  private final PickingService pickingService;
  private final ShippingService shippingService;
  private final SiteInfoService siteInfoService;
  private final DateService dateService;

  @Override
  public void sendSROGCSSMCTransaction(SiteInfo siteInfo, Picking picking) {
    val pid = picking.getPid();
    log.info("Generate SRO for pid: {}", pid);
    ShipmentReceiptsInCollection.MRec record;
    ObjectFactory factory = new ObjectFactory();
    try {
      record = factory.createShipmentReceiptsInCollectionMRec();
      record.setDIC("SRO");
      record.setRIC(siteInfo.getRoutingId());
      record.setIPAAC(siteInfo.getAac());
    }
    catch (Exception e) {
      log.error("Error occurred while generating initial ShipmentReceiptsInCollection.MRec record", e);
      return;
    }

    int niinId = 0;
    try {
      val issueFound = issueService.findByScn(picking.getScn());
      if (issueFound.isPresent()) {
        val transactionTime = dateService.getXMLGregorianCalendarNow();
        val issue = issueFound.get();
        val niinInfoFound = niinInfoService.findById(issue.getNiinId());
        if (niinInfoFound.isPresent()) {
          val niinInfo = niinInfoFound.get();
          record.setNIIN(niinInfo.getNiin());
          record.setUOI(niinInfo.getUi());
          record.setSDN(issue.getDocumentNumber());
          record.setRDD(issue.getRdd());
          record.setPri(StringUtils.leftPad(issue.getIssuePriorityDesignator(), 2, "0"));
          val paddedQty = new BigInteger(StringUtils.leftPad(picking.getQtyPicked().toString(), 5, "0"));
          if (StringUtils.equalsIgnoreCase(issue.getCc(), "A"))
            record.setQCCA(paddedQty);
          else record.setQCCF(paddedQty);

          record.setSC(issue.getSignalCode());
          record.setSRN(issue.getEroNumber()); //* ero
          record.setSupADD(issue.getSupplementaryAddress());
          record.setProj(issue.getProjectCode());
          record.setJON(issue.getCostJon());
          record.setTxnDate(transactionTime);
          record.setKeyD(transactionTime);
          record.setSCN(issue.getScn());
          record.setPIN(picking.getPin());
          record.setSfx(issue.getSuffix());
          niinId = niinInfo.getNiinId();
        }
      }

      listControlledPid(record.getDetRec(), factory, pid);

      marshallAndSpool(record, siteInfo, niinId, 1);
    }
    catch (Exception e) {
      log.error("Exception occurred while sending SRO GCSSMC Transaction", e);
    }
  }

  @Override
  public void sendAE1GCSSMCTransaction(SiteInfo siteInfo, String scn) {
    /*
     * DESC     : This function creates a AE1 Long Transaction
     *            (Generated when an issue is completed or immediately after
     *            an issue is generated if the warehouse has a quantity of 0
     *            on-hand)
     *            This version of the AE1 function is for Issues that have been
     *            sent into the warehouse for processing.   If the warehouse had
     *            a quantity of 0 on-hand at the beginning of the day, the proper
     *            tables will not have values populated to be able to generate
     *            this transaction properly and will require data population from
     *            other tables.
     * PARAMS   : scnVal (The SCN value of the issue transaction)
     */
    val transactionTime = dateService.getXMLGregorianCalendarNow();

    ObjectFactory factory = new ObjectFactory();
    ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
    record.setDIC("AE1");
    record.setRIC(siteInfo.getRoutingId());
    record.setIPAAC(siteInfo.getAac());
    record.setTxnDate(transactionTime);
    record.setKeyD(transactionTime);
    record.setSCN(scn);

    try {
      val issueFound = issueService.findByScn(scn);
      if (issueFound.isPresent()) {
        val issue = issueFound.get();
        val niinInfoFound = niinInfoService.findById(issue.getNiinId());
        if (niinInfoFound.isPresent()) {
          val niinInfo = niinInfoFound.get();

          record.setNIIN(niinInfo.getNiin());
          record.setUOI(niinInfo.getUi());
          record.setSDN(issue.getDocumentNumber());
          record.setSfx(issue.getSuffix() == null ? "" : issue.getSuffix());
          record.setPri(StringUtils.leftPad(issue.getIssuePriorityDesignator(), 2, "0"));

          //Quantity Issued
          int qtyIssued;
          int qtyDenied;
          val pickingSearchCriteria = PickingSearchCriteria.builder()
              .scn(scn)
              .statuses(Collections.singletonList("PACKED"))
              .build();

          val picks = pickingService.search(pickingSearchCriteria);

          qtyIssued = issue.getQty();

          qtyDenied = qtyIssued;

          if (CollectionUtils.isNotEmpty(picks)) {
            qtyDenied -= picks.stream().mapToInt(Picking::getQtyPicked).sum();
          }

          val conditionCode = issue.getCc();
          if (conditionCode.equals("A")) record.setQCCA(new BigInteger("00000" + qtyDenied));
          else record.setQCCF(new BigInteger("00000" + qtyDenied));
          record.setStatus("BA");
          if (qtyDenied > 0) {
            if (conditionCode.equals("A")) record.setQCCA(new BigInteger("0" + qtyDenied));
            else record.setQCCF(new BigInteger("0" + qtyDenied));
            record.setStatus("M5");
          }

          if (qtyDenied > 0) {
            val marshalledSuccessfully = marshallAndSpool(record, siteInfo, niinInfo.getNiinId(), 1);
            if (!marshalledSuccessfully) return;
          }

          //Create the xml spool recs for issues
          if ((qtyIssued - qtyDenied) != 0) {
            val finalQty = qtyIssued - qtyDenied;
            if (conditionCode.equals("A")) record.setQCCA(BigInteger.valueOf(finalQty));
            else record.setQCCF(BigInteger.valueOf(finalQty));
            record.setStatus("BA");
            log.info("AE1 SCN and Suffix in before niin serial call XXXX  scn {}", scn);

            listControlledScn(record.getDetRec(), factory, scn);

            marshallAndSpool(record, siteInfo, niinInfo.getNiinId(), 1);
          }
        }
      }
    }
    catch (Exception e) {
      log.error("Exception occurred while sending AE1 GCSSMC Transactions", e);
    }
  }

  @Override
  public void sendAsxTrans(SiteInfo siteInfo, Issue issue, Integer transNumber) {
    log.info("Generate ASx Transaction to GCSS for SCN {}", issue.getScn());
    //* 01-24-2012 update made to ASx/SRO transaction GCSS mode to address when
    //* scn from previous years overlap
    try {
      ObjectFactory factory = new ObjectFactory();
      ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
      String conditionCode = "";

      val sTrans = transNumber == 3 ? "AS1" : "AS";

      record.setDIC(sTrans);
      record.setRIC(siteInfo.getRoutingId());
      record.setIPAAC(siteInfo.getAac());

      val shippingCriteria = ShippingSearchCriteria.builder()
          .scn(issue.getScn())
          .build();

      shippingCriteria.setSort("createdDate", SortOrder.DESC);
      val shippingRecords = shippingService.search(shippingCriteria);

      String leadTcn = null;
      //if there is a shipping record, no need to look at history.  when a record gets deleted from shipping, thats when it moves into history.
      //so shipping records are always newer.
      if (CollectionUtils.isNotEmpty(shippingRecords)) {
        //we have shipping records...
        val shippingRecord = shippingRecords.get(0);
        leadTcn = shippingRecord.getShippingManifest().getLeadTcn();
        record.setTCN(shippingRecord.getTcn());
      }

      if (StringUtils.isEmpty(leadTcn) && transNumber != 3) {
        return;
      }

      //we already have an issue... no need to check again.
      val niinInfoFound = niinInfoService.findById(issue.getNiinId());
      if (niinInfoFound.isPresent()) {
        val transactionTime = dateService.getXMLGregorianCalendarNow();
        val niinInfo = niinInfoFound.get();
        record.setNIIN(niinInfo.getNiin());
        record.setUOI(niinInfo.getUi());
        record.setSDN(issue.getDocumentNumber());
        record.setSfx(issue.getSuffix());
        record.setPri(StringUtils.leftPad(issue.getIssuePriorityDesignator(), 2, "0"));

        conditionCode = issue.getCc();
        if (conditionCode.equals("A")) record.setQCCA(BigInteger.valueOf(issue.getQtyIssued()));
        else record.setQCCF(BigInteger.valueOf(issue.getQtyIssued()));

        record.setSupADD(issue.getSupplementaryAddress());
        record.setFund(issue.getFundCode());
        record.setRCN(issue.getRcn());
        record.setSCN(issue.getScn());

        record.setTxnDate(transactionTime);
        record.setKeyD(transactionTime);
      }

      List<String> possibleStatusBasedOnTransNumber = new ArrayList<>();
      possibleStatusBasedOnTransNumber.add("PACKED");
      possibleStatusBasedOnTransNumber.add("SHIPPED");
      possibleStatusBasedOnTransNumber.add("WALKTHRU");

      val statusBasedOnTransNumber = possibleStatusBasedOnTransNumber.get(transNumber);
      val pickingSearchCriteria = PickingSearchCriteria.builder()
          .scn(issue.getScn())
          .statuses(Collections.singletonList(statusBasedOnTransNumber))
          .build();

      val picks = pickingService.search(pickingSearchCriteria);
      if (CollectionUtils.isNotEmpty(picks)) {
        val sumQuantityPicked = BigInteger.valueOf(picks.stream().mapToInt(Picking::getQtyPicked).sum());
        if (conditionCode.equals("A")) record.setQCCA(sumQuantityPicked);
        else record.setQCCF(sumQuantityPicked);
      }
      else {
        val createdDate = OffsetDateTime.now().minusDays(60);
        val packedCountFound = pickingService.findCountOfPickingHistory(issue.getScn(), statusBasedOnTransNumber, createdDate);
        if (packedCountFound.isPresent()) {
          val packedCount = BigInteger.valueOf(packedCountFound.get());

          if (conditionCode.equals("A")) record.setQCCA(packedCount);
          else record.setQCCF(packedCount);
        }
      }
      listControlledScn(record.getDetRec(), factory, issue.getScn());

      marshallAndSpool(record, siteInfo, issue.getNiinId(), 1);
    }
    catch (Exception e) {
      log.error("Error Processing I009 spool record", e);
    }
  }

  @Override
  public boolean marshallAndSpool(ShipmentReceiptsInCollection.MRec record, Integer niinId, Integer userId) {
    val siteInfo = siteInfoService.getRecord();
    return marshallAndSpool(record, siteInfo, niinId, userId);
  }

  @Override
  public Optional<String> marshallRecord(ShipmentReceiptsInCollection.MRec record) {
    return Optional.ofNullable(I009Marshaller.marshal(record));
  }

  private boolean marshallAndSpool(ShipmentReceiptsInCollection.MRec record, SiteInfo siteInfo, Integer niinId, Integer userId) {
    val marshalledXml = marshallRecord(record);
    if (marshalledXml.isPresent()) {
      insertSpoolRecord(siteInfo, record.getDIC(), marshalledXml.get(), record, niinId, userId);
      return true;
    }

    return false;  // problem marshalling the record
  }

  @Override
  public void insertSpoolRecord(String transactionType,
                                String i136Xml, ShipmentReceiptsInCollection.MRec record, Integer niinId, Integer userId) {
    val siteInfo = siteInfoService.getRecord();
    insertSpoolRecord(siteInfo, transactionType, i136Xml, record, niinId, userId);
  }

  private void insertSpoolRecord(
      SiteInfo siteInfo,
      String transactionType,
      String i136Xml,
      ShipmentReceiptsInCollection.MRec record,
      Integer niinId,
      Integer userId) {

    try {
      val currentTime = OffsetDateTime.now();
      val spool = Spool.builder()
          .spoolDefMode("G")
          .timestamp(currentTime)
          .status("PENDING")
          .transactionType(transactionType)
          .xml(i136Xml)
          .niinId(niinId)
          .modById(userId)
          .recallFlag("N")
          .correctionFlag("N")
          .documentNumber(record.getSDN())
          .suffix(record.getSfx())
          .sid(record.getSID())
          .build();

      spoolService.save(spool);

      val siteNumber = getSiteNumber(siteInfo);

      val now = OffsetDateTime.now();
      DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yDDD");

      String refSpoolId = siteNumber + now.format(dateFormat) + spool.getSpoolId();

      // redmine 18496 GCSS Spool Duplicate
      record.setSpoolID(new BigInteger(refSpoolId));

      val xMLRecString = marshallRecord(record);
      if (xMLRecString.isPresent())
        spool.updateToReady(Long.valueOf(refSpoolId), xMLRecString.get());

      spoolService.save(spool);
    }
    catch (Exception e) {
      log.error("Exception occurred creating spool record", e);
    }
  }

  private void listControlledScn(List<ShipmentReceiptsInCollection.MRec.DetRec> details, ObjectFactory factory, String scn) {
    log.info("inside ListControlledScn scn: {}", scn);
    try {
      if (scn == null) {
        log.error("No scn specified to process listControlledScn");
        return;
      }

      val pickSerialLotNumSearchCriteria = PickSerialLotNumSearchCriteria.builder()
          .scn(scn)
          .build();
      val pickSerials = pickSerialLotNumService.search(pickSerialLotNumSearchCriteria);

      processListControlledPicks(details, factory, pickSerials);
    }
    catch (Exception e) {
      log.error("Exception occurred during listControlledScn", e);
    }
  }

  private void listControlledPid(List<ShipmentReceiptsInCollection.MRec.DetRec> details, ObjectFactory factory, Integer pid) {
    log.info("inside listControlledPid pid: {}", pid);
    try {
      if (pid == null) {
        log.error("No pid specified to process listControlledPid");
        return;
      }

      val pickSerialLotNumSearchCriteria = PickSerialLotNumSearchCriteria.builder()
          .pid(pid)
          .build();
      val pickSerials = pickSerialLotNumService.search(pickSerialLotNumSearchCriteria);

      processListControlledPicks(details, factory, pickSerials);
    }
    catch (Exception e) {
      log.error("Exception occurred during listControlledPid", e);
    }
  }

  private void processListControlledPicks(List<ShipmentReceiptsInCollection.MRec.DetRec> details, ObjectFactory factory, List<PickSerialLotNum> pickSerials) {
    if (CollectionUtils.isNotEmpty(pickSerials)) {
      pickSerials.forEach(pickSerial -> {
        val serialLotNumTrack = pickSerial.getSerialLotNumTrack();
        //serial number and lotConNum used from serial Lot Num Track, or Pick Serial, or empty string
        val serialNumber = Stream.of(serialLotNumTrack.getSerialNumber(), pickSerial.getSerialNumber(), "").filter(Objects::nonNull).findFirst();
        val lotConNum = Stream.of(serialLotNumTrack.getLotConNum(), pickSerial.getLotConNum(), "").filter(Objects::nonNull).findFirst();

        val conditionCode = StringUtils.isEmpty(serialLotNumTrack.getCc()) ? "A" : serialLotNumTrack.getCc();

        int quantity;
        if (pickSerial.getQty() != null && pickSerial.getQty() < 1) {
          quantity = serialLotNumTrack.getQty() != null ? serialLotNumTrack.getQty() : 0;
        }
        else {
          quantity = pickSerial.getQty() != null ? pickSerial.getQty() : 0;
        }

        val maxDate = DateConstants.maxLocalDate;

        val pickExpDate = pickSerial.getExpirationDate() == null ? maxDate : pickSerial.getExpirationDate();
        val serialExpDate = serialLotNumTrack.getExpirationDate() == null ? maxDate : serialLotNumTrack.getExpirationDate();
        LocalDate recordDate = pickExpDate.isBefore(serialExpDate) ? pickExpDate : serialExpDate;

        ShipmentReceiptsInCollection.MRec.DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
        detail.setSerN(serialNumber.get());
        detail.setLotN(lotConNum.get());
        if (Util.isNotEmpty(detail.getLotN()))
          detail.setLotED(dateService.getXMLGregorianCalendarFromDateOrElseToday(recordDate));
        detail.setCC(conditionCode);  // can only walk thru on cc A
        detail.setQLot(BigInteger.valueOf(quantity));

        details.add(detail);
      });
    }
  }

  String getSiteNumber(SiteInfo siteInfo) {
    switch (siteInfo.getAac()) {
      case "MMR100":
        return "10";
      case "MMK100":
        return "20";
      case "MML100":
        return "30";
      case "MMC100":
        return "40";
      case "MMV200":
        return "50";
      case "MMX800":
        return "60";
      default:
        return "0";
    }
  }
}
