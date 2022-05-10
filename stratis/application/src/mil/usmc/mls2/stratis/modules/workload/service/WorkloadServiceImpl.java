package mil.usmc.mls2.stratis.modules.workload.service;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.domain.repository.*;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.modules.workload.domain.model.NewCartonParams;
import mil.usmc.mls2.stratis.modules.workload.domain.model.PackingConsolidationInfo;
import mil.usmc.mls2.stratis.modules.workload.domain.model.PackingStationResult;
import mil.usmc.mls2.stratis.modules.workload.domain.model.PackingStationType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * MCPX - Single/Multi Station
 * CPCX - Consolidation Station
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkloadServiceImpl implements WorkloadService {

  private final PickingRepository pickingRepository;
  private final PackingConsolidationRepository packingConsolidationRepository;
  private final EquipmentRepository equipmentRepository;
  private final PackingStationRepository packingStationRepository;
  private final IssueRepository issueRepository;
  private final SiteInfoRepository siteInfoRepository;
  private final SiteSecurityRepository siteSecurityRepository;
  private final CustomerRepository customerRepository;

  /**
   * This method is used from legacy where we will not have a real picking object, just the PID.
   */
  @Override
  public PackingStationResult getPackingStation(Integer pid, int userId, Integer qtyPicked, int mcpxPref) {
    val pick = pickingRepository.findById(pid).orElse(null);
    return getPackingStation(pick, userId, qtyPicked, mcpxPref, x -> null);
  }

  @Override
  public PackingStationResult getPackingStation(Picking pick, int userId, Integer qtyPicked, int mcpxPref) {
    return getPackingStation(pick, userId, qtyPicked, mcpxPref, x -> null);
  }

  @Override
  public PackingStationResult getPackingStation(Picking pick, int userId, Integer qtyPicked, int mcpxPref, Function<Integer, PackingConsolidationInfo> lastProcessedPackageProvider) {
    val watch = new StopWatch();
    watch.start();

    boolean exceptionOccurred = false;
    try {
      val packingBypassValues = getBypassValues();

      val issue = issueRepository.findById(pick.getScn()).orElse(null);
      if (issue == null) {
        val packingStation = "Error - Could not find an Issue for the given SCN.";
        return PackingStationResult.ofName(packingStation);
      }

      var packingStationType = PackingStationType.INVALID;
      if (mcpxPref == 1) {
        val removedPackingStation = removePackingStation(pick, userId);
        if (!"true".equals(removedPackingStation)) {
          // return the name of the previous packing station.  It was an MCPX station already.
          return PackingStationResult.ofName(removedPackingStation);
        }
        packingStationType = PackingStationType.SINGLE;
      }
      else {
        packingStationType = getPackingStationType(pick, packingBypassValues, qtyPicked);
      }

      if (!packingStationType.isValid()) {
        //Error getting packing station type!
        val packingStation = "Error - Could not determine the Packing Station type for this Pick.";
        return PackingStationResult.ofName(packingStation);
      }

      val wac = pick.getNiinLocation().getLocation().getWac();
      val wacPackArea = wac.getPackArea(); //WAC Pack Area maps to Equip Packing Group (no FK though)
      List<Equipment> equipForPackingGroup = new ArrayList<>();

      if (packingStationType.isConsolidated()) {
        val equipSearchCriteria = EquipmentSearchCriteria.builder()
            .packingGroup(wacPackArea)
            .description(PackingStationType.CONSOLIDATION.stationName())
            .build();
        equipForPackingGroup = equipmentRepository.search(equipSearchCriteria);
        // Override-Check: ensure consolidation is supported by the warehouse (if consolidation is the calculated means)
        if (CollectionUtils.isEmpty(equipForPackingGroup)) packingStationType = PackingStationType.SINGLE;
      }

      if (packingStationType.isSingle()) {
        val packingGroupFound = getPackingGroup(pick);
        val packingGroupToUse = packingGroupFound == null ? wacPackArea : packingGroupFound;
        //if what was found doesn't match, we need to load up a new equipment list.
        val equipForPackingGroupSearchCriteria = EquipmentSearchCriteria.builder()
            .packingGroup(packingGroupToUse)
            .description(PackingStationType.SINGLE.stationName())
            .build();
        equipForPackingGroup = equipmentRepository.search(equipForPackingGroupSearchCriteria);
      }

      if (CollectionUtils.isEmpty(equipForPackingGroup)) {
        //No packing stations of this type were found for this building.
        final String packingStation;
        if (packingStationType.isConsolidated()) {
          packingStation = "Error - There are no Consolidation Packing Stations in this building to assign this issue to.";
        }
        else {
          packingStation = "Error - There are no Single Packing Stations in this building to assign this issue to.";
        }
        return PackingStationResult.ofName(packingStation);
      }
      List<PackingStation> packingStations = new ArrayList<>();
      equipForPackingGroup.forEach(equip -> {
        packingStationRepository.findByEquipmentNumber(equip.getEquipmentNumber()).ifPresent(packingStations::add);
      });

      if (packingStationType.isSingle()) {
        val result = processSingleStationType(pick, issue, packingStationType, qtyPicked, packingStations, userId, lastProcessedPackageProvider);
        return result;
      }
      else {
        return processConsolidationStationType(pick, issue, packingStationType, qtyPicked, packingStations, userId, packingBypassValues, wacPackArea, lastProcessedPackageProvider);
      }
    }
    catch (Exception e) {
      exceptionOccurred = true;
      log.error("Error occurred getting Packing Station", e);
      return PackingStationResult.ofName("Error - Exception:  " + e.getMessage());
    }
    finally {
      watch.stop();
      log.debug("GET-PACKING-STATION execution complete [durationSecs: {}, exceptionOccurred: {}]", watch.getTotalTimeSeconds(), exceptionOccurred);
    }
  }

  private PackingStationResult processSingleStationType(Picking pick, Issue issue, PackingStationType packingStationType, Integer
      qtyPicked, List<PackingStation> packingStations, Integer
                                                            userId, Function<Integer, PackingConsolidationInfo> lastProcessedPackageProvider) {
    //MCPX Station.   - Single/Multipack Station

    //need all picks associated to the SCN where its not this pick.
    val allPicksForScn = pickingRepository.findByScn(pick.getScn());
    val firstPickWithConsolidationId = allPicksForScn.stream()
        .filter(otherPick -> !otherPick.getPid().equals(pick.getPid()))
        .filter(otherPick -> otherPick.getPackingConsolidationId() != null)
        .findFirst().orElse(null);
    val niinInfo = pick.getNiinLocation().getNiinInfo();

    if (firstPickWithConsolidationId != null) {
      val packingConsolidationId = firstPickWithConsolidationId.getPackingConsolidationId();
      val packingConsolidation = updateConsolRow(niinInfo.getCube(), niinInfo.getWeight(), qtyPicked, userId, packingConsolidationId);

      if (packingConsolidation == null) {
        return PackingStationResult.ofName("Error - Could not resolve Packing Station name - MCPX (Partial Issue)");
      }
      pick.addPackingConsolidationId(packingConsolidationId, userId);
      try {
        pickingRepository.save(pick);
        val packingConsolidationInfo = PackingConsolidationInfo.builder()
            .column(packingConsolidation.getPackColumn())
            .packingStationId(packingConsolidation.getPackingStationId())
            .packingConsolidationId(packingConsolidation.getPackingConsolidationId())
            .level(packingConsolidation.getPackLevel())
            .singleUseStation(true)
            .build();
        return getEquipNameForPackingConsolidationId(packingConsolidationId, packingConsolidationInfo, "MCPX  (Partial Issue)");
      }
      catch (Exception e) {
        return PackingStationResult.ofName("Error - Could not resolve Packing Station name - MCPX (Partial Issue)");
      }
    }
    else {
      //go through the packingStations....
      for (PackingStation packingStation : packingStations) {
        val packingStationId = packingStation.getPackingStationId();
        val newCartonParams = NewCartonParams.builder()
            .stationId(packingStationId)
            .stationLevels(packingStation.getLevels())
            .stationColumns(packingStation.getColumns())
            .lastPackedItem(lastProcessedPackageProvider.apply(packingStationId))
            .build();

        val packingConsolidationInfo = openNewCarton(newCartonParams, issue.getCustomer(), niinInfo.getCube(), niinInfo.getWeight(), pick.getPickQty(), userId, packingStationType, issue.getQty(), issue.getIssuePriorityGroup());
        val packingConsolidationId = packingConsolidationInfo.packingConsolidationId();

        //Set the pickView to filter to the PICKING row we want to update
        if (packingConsolidationId > 0) {
          try {
            pick.addPackingConsolidationId(packingConsolidationId, userId);
            pickingRepository.save(pick);
            return getEquipNameForPackingConsolidationId(packingConsolidationId, packingConsolidationInfo, "MCPX (Partial Issue)");
          }
          catch (Exception e) {
            return PackingStationResult.of("Error - NameEmpty (MCPX)", packingConsolidationInfo);
          }
        }
      }

      // Unable to locate valid packing station
      return PackingStationResult.ofName("Error - All Slots maxed out for Single Packing Stations in this Building");
    }
  }

  private PackingStationResult processConsolidationStationType(Picking pick, Issue issue, PackingStationType packingStationType, Integer
      qtyPicked, List<PackingStation> packingStations, Integer userId, PackingBypassValues packingBypassValues, String
                                                                   packArea, Function<Integer, PackingConsolidationInfo> lastProcessedPackageProvider) {
    // CPCX Station - Route to a consolidation station and triwall bin.

    //need all picks associated to the SCN (will contain current pick as well)
    val allPicksForScn = pickingRepository.findByScn(pick.getScn());
    val firstPickWithConsolidationId = allPicksForScn.stream()
        .filter(otherPick -> otherPick.getPackingConsolidationId() != null)
        .findFirst().orElse(null);
    val niinInfo = pick.getNiinLocation().getNiinInfo();

    if (firstPickWithConsolidationId != null) {
      val packingConsolidationId = firstPickWithConsolidationId.getPackingConsolidationId();
      val packingConsolidation = updateConsolRow(niinInfo.getCube(), niinInfo.getWeight(), pick.getPickQty(), userId, packingConsolidationId);

      if (packingConsolidation == null) {
        return PackingStationResult.ofName("Error - Could not resolve packing station name - CPCX (Partial Issue)");
      }

      pick.addPackingConsolidationId(packingConsolidationId, userId);
      try {
        pickingRepository.save(pick);
        val packingConsolidationInfo = PackingConsolidationInfo.builder()
            .column(packingConsolidation.getPackColumn())
            .packingStationId(packingConsolidation.getPackingStationId())
            .packingConsolidationId(packingConsolidation.getPackingConsolidationId())
            .level(packingConsolidation.getPackLevel())
            .singleUseStation(false)
            .build();
        return getEquipNameForPackingConsolidationId(packingConsolidationId, packingConsolidationInfo, "CPCX (Partial Issue)");
      }
      catch (Exception e) {
        return PackingStationResult.ofName("Error - Could not resolve packing station name - CPCX (Partial Issue)");
      }
    }

    val cubeOfIssue = niinInfo.getCube();
    val weightOfIssue = niinInfo.getWeight();
    val customer = issue.getCustomer();
    val totalCube = niinInfo.getCube().multiply(new BigDecimal(qtyPicked));
    val totalWeight = niinInfo.getWeight().multiply(new BigDecimal(qtyPicked));

    val tempPackingStationId = getPackingStationIdByCustId(customer.getCustomerId(), packingStationType, packArea);
    val packingConsolidation = getPackingConsolidationByCustId(tempPackingStationId, customer.getCustomerId(), issue.getIssuePriorityGroup(), totalCube, totalWeight, packingBypassValues);

    if (packingConsolidation != null) {
      //We found a matching station id!   Lookup the stationId and get the packingStation value
      //then assign the PIN to the consolidation station and update the proper weights of that bin
      //and increment the total issues of the bin.
      val consolidationRow = updateConsolRow(cubeOfIssue, weightOfIssue, qtyPicked, userId, packingConsolidation.getPackingConsolidationId());

      if (consolidationRow == null)
        return PackingStationResult.ofName("Error - Could not resolve Packing Station Name (Bin with matching AAC Found)");

      pick.addPackingConsolidationId(packingConsolidation.getPackingConsolidationId(), userId);
      try {
        pickingRepository.save(pick);
        val packingConsolidationInfo = PackingConsolidationInfo.builder()
            .column(consolidationRow.getPackColumn())
            .packingStationId(consolidationRow.getPackingStationId())
            .packingConsolidationId(consolidationRow.getPackingConsolidationId())
            .level(consolidationRow.getPackLevel())
            .singleUseStation(false)
            .build();
        return getEquipNameForPackingConsolidationId(packingConsolidation.getPackingConsolidationId(), packingConsolidationInfo, " (Bin with matching AAC Found)");
      }
      catch (Exception e) {
        return PackingStationResult.ofName("Error - Exception:  " + e);
      }
    }
    else {
      //find the temp station
      val tempStation = packingStationRepository.findById(tempPackingStationId).orElse(null);

      //if the pick can be placed in the temp station, do it.
      if (tempStation != null) {
        val packingConsolidationInfo = processPackingStation(tempStation, customer, niinInfo, pick, issue, packingStationType, qtyPicked, userId, lastProcessedPackageProvider);
        if (packingConsolidationInfo.packingConsolidationId() > 0) {
          try {
            pick.addPackingConsolidationId(packingConsolidationInfo.packingConsolidationId(), userId);
            pickingRepository.save(pick);
            return getEquipNameForPackingConsolidationId(packingConsolidationInfo.packingConsolidationId(), packingConsolidationInfo, "MCPX (Partial Issue)");
          }
          catch (Exception e) {
            return PackingStationResult.of("Error - NameEmpty (MCPX)", packingConsolidationInfo);
          }
        }
      }

      //if we couldn't populate the temp station, loop over the existing packing stations and try to use one.
      for (PackingStation packingStation : packingStations) {
        val packingConsolidationInfo = processPackingStation(packingStation, customer, niinInfo, pick, issue, packingStationType, qtyPicked, userId, lastProcessedPackageProvider);

        //Set the pickView to filter to the PICKING row we want to update
        if (packingConsolidationInfo.packingConsolidationId() > 0) {
          try {
            pick.addPackingConsolidationId(packingConsolidationInfo.packingConsolidationId(), userId);
            pickingRepository.save(pick);
            return getEquipNameForPackingConsolidationId(packingConsolidationInfo.packingConsolidationId(), packingConsolidationInfo, "MCPX (Partial Issue)");
          }
          catch (Exception e) {
            return PackingStationResult.of("Error - NameEmpty (MCPX)", packingConsolidationInfo);
          }
        }
      }
    }
    return PackingStationResult.ofName("Error - All Slots maxed out for Single Packing Stations in this Building");
  }

  private PackingConsolidationInfo processPackingStation(PackingStation station, Customer customer, NiinInfo niinInfo, Picking
      pick, Issue issue, PackingStationType packingStationType, Integer qtyPicked, Integer
                                                             userId, Function<Integer, PackingConsolidationInfo> lastProcessedPackageProvider) {
    val newCartonParams = NewCartonParams.builder()
        .stationId(station.getPackingStationId())
        .stationLevels(station.getLevels())
        .stationColumns(station.getColumns())
        .lastPackedItem(lastProcessedPackageProvider.apply(station.getPackingStationId()))
        .build();
    return openNewCarton(newCartonParams, customer, niinInfo.getCube(), niinInfo.getWeight(), qtyPicked, userId, packingStationType, pick.getPickQty(), issue.getIssuePriorityGroup());
  }

  private PackingStationResult getEquipNameForPackingConsolidationId(Integer packingConsolidationId, PackingConsolidationInfo
      info, String errorString) {
    val packingConsolidation = packingConsolidationRepository.findById(packingConsolidationId).orElse(null);
    if (packingConsolidation == null)
      return PackingStationResult.ofName(String.format("Error - Could not resolve Packing Station name - %s", errorString));
    val packingStation = packingStationRepository.findById(packingConsolidation.getPackingStationId()).orElse(null);
    if (packingStation == null)
      return PackingStationResult.ofName(String.format("Error - Could not resolve Packing Station name - %s", errorString));
    val equip = equipmentRepository.findById(packingStation.getEquipmentNumber()).orElse(null);
    if (equip == null)
      return PackingStationResult.ofName(String.format("Error - Could not resolve Packing Station name - %s", errorString));
    return PackingStationResult.of(equip.getName(), info);
  }

  private int getPackingStationIdByCustId(int customerId, PackingStationType stationType, String packArea) {
    val packingConsolidationRecord = packingConsolidationRepository.findPackingConsolidationByEquipmentAndCustomer(stationType.stationName(), packArea, customerId).orElse(null);
    if (packingConsolidationRecord == null) return -1;

    return packingConsolidationRecord.getPackingStationId();
  }

  private PackingConsolidation getPackingConsolidationByCustId(Integer stationId, int customerId, String issuePriorityGroup, BigDecimal
      issueCube, BigDecimal issueWeight, PackingBypassValues packingBypassValues) {
    try {
      if (stationId == null) return null;
      val packingConsolidationSearchCriteria = PackingConsolidationSearchCriteria.builder()
          .packingStationId(stationId)
          .closeCarton("N")
          .issuePriorityGroup(issuePriorityGroup)
          .customerId(customerId)
          .nullBarcode(true)
          .build();

      val searchResults = packingConsolidationRepository.search(packingConsolidationSearchCriteria);

      //find the first packing consolidation record that can fit this item (by not exceeding the cube and weight thresholds.
      for (PackingConsolidation packingConsolidation : searchResults) {
        val locationCube = packingConsolidation.getConsolidationCube().add(issueCube);
        val locationWeight = packingConsolidation.getConsolidationWeight().add(issueWeight);
        if (locationCube.compareTo(packingBypassValues.cubeThreshold) < 0 &&
            locationWeight.compareTo(packingBypassValues.weightThreshold) < 0) {
          return packingConsolidation;
        }
      }
      //no location found
      return null;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * This method is used from legacy where we will not have a real Customer object, just the customerId.
   */
  @Override
  public PackingConsolidationInfo openNewCarton(NewCartonParams newCartonParams, Integer customerId, BigDecimal
      weight, BigDecimal cube, Integer pickQty, int userId, PackingStationType packingStationType, Integer issueQty, String priorityGroup) {
    val customer = customerRepository.findById(customerId).orElse(null);

    return openNewCarton(newCartonParams, customer, weight, cube, pickQty, userId, packingStationType, issueQty, priorityGroup);
  }

  private PackingConsolidationInfo openNewCarton(NewCartonParams newCartonParams, Customer customer, BigDecimal
      weight, BigDecimal cube, Integer pickQty, int userId, PackingStationType packingStationType, Integer issueQty, String priorityGroup) {
    val watch = new StopWatch();
    watch.start();

    val stationLevels = newCartonParams.stationLevels();
    val stationColumns = newCartonParams.stationColumns();
    val stationId = newCartonParams.stationId();

    boolean exceptionOccurred = false;
    try {

      var maxLev = stationLevels;
      var maxCol = stationColumns;

      if (maxLev < 1 || maxCol < 1) {
        maxLev = 1;
        maxCol = 1;
      }

      var minLevel = 1;
      var minColumn = 1;

      //optimized starting seed point based upon last picked item if any
      if (newCartonParams.lastPackedItem() != null) {
        //FUTURE optimize further to shift to next position
        minLevel = newCartonParams.lastPackedItem().level();
        minColumn = newCartonParams.lastPackedItem().column();
      }

      if (packingStationType.isSingle() && (Integer.parseInt(pickQty.toString()) == Integer.parseInt(issueQty.toString()))) {
        //This is a MCPX Station that does not get assigned to a holding bin

        val packingConsolidation = savePackingConsolidationRecord(stationId, customer, cube, weight, pickQty, priorityGroup, 0, 0, null, userId);

        return PackingConsolidationInfo.builder()
            .packingConsolidationId(packingConsolidation.getPackingConsolidationId())
            .packingStationId(stationId)
            .singleUseStation(true)
            .level(0)
            .column(0)
            .build();
      }
      else {
        //Loop through all of the possible carton slots for this packing station
        //and see if there are any queries in the PACKING_CONSOLIDATION table that
        //match our criteria for an available carton to be opened.

        val dlp = DoubleLoopParams.builder()
            .customer(customer)
            .cube(cube)
            .weight(weight)
            .minColumn(minColumn)
            .maxColumn(maxCol)
            .minLevel(minLevel)
            .maxLevel(maxLev)
            .pickQty(pickQty)
            .priorityGroup(priorityGroup)
            .stationId(stationId)
            .packingStationType(packingStationType)
            .build();

        return generatePackingConsolidationInfo(dlp, userId);
      }
    }
    catch (Exception e) {
      exceptionOccurred = true;
      log.error("Error in {}", e.getStackTrace()[0].getMethodName(), e);
    }
    finally {
      watch.stop();
      log.debug("OPEN-NEW-CARTON-OPTIMIZED execution complete [durationSecs: {}, exceptionOccurred: {}]", watch.getTotalTimeSeconds(), exceptionOccurred);
    }

    return PackingConsolidationInfo.empty();
  }

  private PackingConsolidationInfo generatePackingConsolidationInfo(DoubleLoopParams dlp, int userId) {
    val minLevel = dlp.minLevel;
    val maxLevel = dlp.maxLevel;
    var minColumn = dlp.minColumn;
    var maxColumn = dlp.maxColumn;
    val stationId = dlp.stationId;
    val customer = dlp.customer;
    val cube = dlp.cube;
    val weight = dlp.weight;
    val pickQty = dlp.pickQty;
    val priorityGroup = dlp.priorityGroup;
    val packingStationType = dlp.packingStationType;

    boolean bDone = false;

    while (!bDone) {

      // * Find available packing location
      for (int levelIndex = minLevel; levelIndex <= maxLevel; levelIndex++) {
        for (int columnIndex = minColumn; columnIndex <= maxColumn; columnIndex++) {
          //Starting at the lowest level, check if any columns are free and work upwards
          val packingConsolidationSearchCriteria = PackingConsolidationSearchCriteria.builder()
              .level(levelIndex)
              .column(columnIndex)
              .closeCarton("N")
              .packingStationId(stationId)
              .build();
          val packingStationRecordCount = packingConsolidationRepository.count(packingConsolidationSearchCriteria);

          if (packingStationRecordCount < 1) {
            String barcode = null;
            if (packingStationType.isConsolidated()) {
              //Create a Packing Station Consolidation Carton Barcode Field
              // ** The algorithm for this is always the same for a given Packing Station, Level, and Slot **
              //Convert the level number to a character
              if (columnIndex < 10) barcode = "0";
              barcode = barcode + columnIndex + (char) (levelIndex + 64);
            }

            val packingConsolidation = savePackingConsolidationRecord(stationId, customer, cube, weight, pickQty, priorityGroup, columnIndex, levelIndex, barcode, userId);

            return PackingConsolidationInfo
                .builder()
                .packingConsolidationId(packingConsolidation.getPackingConsolidationId())
                .packingStationId(stationId)
                .level(levelIndex)
                .column(columnIndex)
                .build();
          }
        }
      }

      // * No packing location has been found. Determine how processing should continue.
      if (packingStationType.isSingle()) {
        // Extend the MCPX by creating a "temporary" column for usage
        maxColumn++;
        minColumn = maxColumn;
      }
      else {
        // CPCX does not support extension (unlike MCPX) - cannot create new or temporary CPCX Bins - processing complete
        bDone = true;
      }
    }

    return PackingConsolidationInfo.empty();
  }

  private PackingConsolidation savePackingConsolidationRecord(Integer stationId, Customer customer, BigDecimal cube, BigDecimal
      weight, Integer pickQty, String priorityGroup, Integer packColumn, Integer packLevel, String barcode, Integer userId) {
    val packingConsolidation = PackingConsolidation.builder()
        .packingStationId(stationId)
        .numberOfIssues(1)
        .customer(customer)
        .packColumn(packColumn)
        .packLevel(packLevel)
        .consolidationCube(cube.multiply(new BigDecimal(pickQty)))
        .consolidationWeight(weight.multiply(new BigDecimal(pickQty)))
        .createdBy(userId)
        .createdDate(OffsetDateTime.now())
        .modifiedBy(userId)
        .issuePriorityGroup(priorityGroup)
        .packLocationBarcode(barcode)
        .closedCarton("N")
        .partialRelease("N")
        .build();
    packingConsolidationRepository.save(packingConsolidation); //the save updates the packingConsolidationId in the object.
    return packingConsolidation;
  }

  private PackingConsolidation updateConsolRow(BigDecimal issueCube, BigDecimal issueWeight,
                                               int qtyPicked, int userId, int packingConsolidationId) {
    try {
      val packingConsolidationRecord = packingConsolidationRepository.findById(packingConsolidationId).orElseThrow(() -> new IllegalStateException(String.format("Packing Consolidation Record not found for id %s", packingConsolidationId)));
      packingConsolidationRecord.addPackingConsolidationData(issueCube.multiply(new BigDecimal(qtyPicked)), issueWeight.multiply(new BigDecimal(qtyPicked)), userId);
      packingConsolidationRepository.save(packingConsolidationRecord);
      return packingConsolidationRecord;
    }
    catch (Exception e) {
      return null;
    }
  }

  public String removePackingStation(Picking pick, Integer userId) {
    try {
      val packingConsolidationRecord = packingConsolidationRepository.findById(pick.getPackingConsolidationId()).orElse(null);
      if (packingConsolidationRecord == null) return "true";

      val packingStation = packingStationRepository.findById(packingConsolidationRecord.getPackingStationId()).orElse(null);
      if (packingStation == null) return "true";

      val equip = equipmentRepository.findById(packingStation.getEquipmentNumber()).orElse(null);
      if (equip == null) return "true";

      val description = equip.getDescription();
      if (description == null) return "true";

      //We only need to Re-Assign this station if it is a Consolidation station, otherwise we return the name of the single pack station it was previously assigned to
      //NOTE: Currently no production system has any Consolidation stations. 03/15/2021
      if (PackingStationType.CONSOLIDATION.stationName().equals(description)) {
        PickingSearchCriteria criteria = PickingSearchCriteria.builder()
            .scn(pick.getScn())
            .packingConsolidationId(pick.getPackingConsolidationId())
            .build();

        //all pickings for this SCN at the station for the current pick.
        val pickings = pickingRepository.search(criteria);
        var cubeTotal = new AtomicReference<BigDecimal>();
        var weightTotal = new AtomicReference<BigDecimal>();

        pickings.forEach(searchedPick -> {
          val qty = searchedPick.getQtyPicked() == 0 ? searchedPick.getPickQty() : searchedPick.getQtyPicked();
          val niinLocation = searchedPick.getNiinLocation();
          val niinInfo = niinLocation.getNiinInfo();

          val cube = BigDecimal.valueOf(qty).multiply(niinInfo.getCube());
          cubeTotal.accumulateAndGet(cube, BigDecimal::add);

          val weight = BigDecimal.valueOf(qty).multiply(niinInfo.getWeight());
          weightTotal.accumulateAndGet(weight, BigDecimal::add);

          //Unassign the packing station in the picking table
          searchedPick.removePackingStation(userId);
          pickingRepository.save(searchedPick);
        });

        val issue = issueRepository.findById(pick.getScn()).orElse(null);

        if (issue != null) {
          issue.removePackingConsolidationId(userId);
          issueRepository.save(issue);
        }

        val pickingSearchCriteria = PickingSearchCriteria.builder()
            .packingConsolidationId(packingConsolidationRecord.getPackingConsolidationId())
            .build();

        val totalPicksAtPackingConsolidation = pickingRepository.count(pickingSearchCriteria);

        //There are no picks still assigned to this bin, we can delete the row.
        if (totalPicksAtPackingConsolidation == 0) {
          packingConsolidationRepository.delete(packingConsolidationRecord.getPackingConsolidationId());
        }
        else {
          packingConsolidationRecord.removeIssueDataForPick(cubeTotal.get(), weightTotal.get(), userId);
          packingConsolidationRepository.save(packingConsolidationRecord);
        }
        //it was a successful re-assignment, simply return true
        return "true";
      }

      return equip.getName();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      return "Error";
    }
  }

  /**
   * This function determines if an item is sutible for consolidation.
   * It is assumed that by default that an item is destined for consolidation, and that the
   * class checks for every case where it can try to deny an item to go to a consolidation
   * station.
   */
  public PackingStationType getPackingStationType(Picking pick, PackingBypassValues packingBypassValues, Integer qtyPicked) {
    try {
      val issue = issueRepository.findById(pick.getScn()).orElse(null);
      if (issue != null) {

        if (packingBypassValues.packingBypass) return PackingStationType.SINGLE;

        //search for a count of PICKS for the SCN of the pick being worked.
        val pickingSearchCriteria = PickingSearchCriteria.builder()
            .scn(pick.getScn())
            .build();

        val countOfPicksForScn = pickingRepository.count(pickingSearchCriteria);
        if (countOfPicksForScn > 1) return PackingStationType.SINGLE;

        if (issue.getQty() > qtyPicked) return PackingStationType.SINGLE;

        val niinLocation = pick.getNiinLocation();
        val niinInfo = niinLocation.getNiinInfo();

        val cube = niinInfo.getCube();
        val weight = niinInfo.getWeight();

        val qty = issue.getQty();
        //big decimal compare to -1, 0, or 1 as this is numerically less than, equal to, or greater than {@code val}.
        val cubeThresholdExceeded = cube.multiply(new BigDecimal(qty)).compareTo(packingBypassValues.cubeThreshold) > 0;
        val weightThresholdExceeded = weight.multiply(new BigDecimal(qty)).compareTo(packingBypassValues.weightThreshold) > 0;

        if (cubeThresholdExceeded || weightThresholdExceeded) return PackingStationType.SINGLE;

        //setup defaults/or values from issue.
        val rdd = issue.getRdd() == null ? "   " : issue.getRdd();
        val documentNumber = issue.getDocumentNumber() == null ? "                    " : issue.getDocumentNumber();
        int issuePriorityDesignator = Integer.parseInt(issue.getIssuePriorityDesignator());
        val issueType = issue.getIssueType() == null ? " " : issue.getIssueType();
        val scc = niinInfo.getScc() == null ? " " : niinInfo.getScc();

        //Check if item is considered "High Priority"
        if (documentNumber.charAt(10) == 'W' || documentNumber.charAt(10) == 'G') return PackingStationType.SINGLE;
        else if (rdd.equals("999") || rdd.equals("N01") || ((rdd.charAt(0) == 'N' || rdd.charAt(0) == 'E') && issuePriorityDesignator <= 8))
          return PackingStationType.SINGLE;
          //Issue is a Walk-Tru.  High Priority.  Do not Consolidate
        else if (issueType.equals("W")) return PackingStationType.SINGLE;
        else {
          val siteSecuritySearchCriteria = SiteSecuritySearchCriteria.builder()
              .codeName("*SCC*")
              .codeValue(scc)
              .build();
          val siteSecurityRecordCount = siteSecurityRepository.count(siteSecuritySearchCriteria);
          if (siteSecurityRecordCount > 0) return PackingStationType.SINGLE;
        }

        return PackingStationType.CONSOLIDATION;
      }
      //No SCN match was found
      else {
        return PackingStationType.INVALID;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return PackingStationType.INVALID;
  }

  // Was named getPickedWarehouse
  private String getPackingGroup(Picking pick) {
    try {
      val scn = pick.getScn();

      val pickingSearchCriteria = PickingSearchCriteria.builder()
          .scn(scn)
          .build();

      val otherPicks = pickingRepository.search(pickingSearchCriteria);
      val consolidationId = new AtomicInteger();

      otherPicks.forEach(otherPick -> {
        if (!otherPick.getPid().equals(pick.getPid()) && otherPick.getPackingConsolidationId() != null)
          consolidationId.set(otherPick.getPackingConsolidationId());
      });

      if (consolidationId.get() != 0) {
        val packingConsolidation = packingConsolidationRepository.findById(consolidationId.get()).orElse(null);
        if (packingConsolidation == null) return null;
        val packingStation = packingStationRepository.findById(packingConsolidation.getPackingStationId()).orElse(null);
        if (packingStation == null) return null;
        val equip = equipmentRepository.findById(packingStation.getEquipmentNumber()).orElse(null);
        if (equip == null) return null;
        return equip.getPackingGroup();
      }
      return null;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  private PackingBypassValues getBypassValues() {
    val siteInfo = siteInfoRepository.getRecord().orElseThrow(() -> new IllegalStateException("No Site Information record found"));

    return PackingBypassValues.builder()
        .cubeThreshold(new BigDecimal(siteInfo.getIssueCubeThreshold()))
        .weightThreshold(new BigDecimal(siteInfo.getIssueWeightThreshold()))
        .packingBypass(siteInfo.getPackingBypass().equals("Y"))
        .build();
  }

  @Builder
  @Accessors(fluent = true)
  private static class PackingBypassValues {

    private final BigDecimal cubeThreshold;
    private final BigDecimal weightThreshold;
    private final boolean packingBypass;
  }

  @Builder
  @Value
  static class DoubleLoopParams {

    int minLevel;
    int maxLevel;
    int minColumn;
    int maxColumn;
    int stationId;
    Customer customer;
    BigDecimal cube;
    BigDecimal weight;
    int pickQty;
    PackingStationType packingStationType;
    String priorityGroup;
  }
}
