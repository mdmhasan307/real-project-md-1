package mil.usmc.mls2.stratis.core.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.StringUtil;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.domain.repository.PackingConsolidationRepository;
import mil.usmc.mls2.stratis.core.domain.repository.PackingStationRepository;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.DefaultTransactionExecutor;
import mil.usmc.mls2.stratis.core.manager.PickingListManager;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.workload.service.WorkloadService;
import oracle.jbo.Row;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
class PickingProcessorImpl implements PickingProcessor {

  private final PickingService pickingService;
  private final PickSerialLotNumService pickSerialLotNumService;
  private final NiinLocationService niinLocationService;
  private final StowService stowService;
  private final ErrorQueueService errorQueueService;
  private final PackingConsolidationRepository consolidationRepository;
  private final PackingStationRepository packingStationRepository;
  private final EquipmentService equipmentService;
  private final SerialLotNumTrackService serialLotNumTrackService;
  private final DefaultTransactionExecutor transactionExecutor;
  private final WorkloadService workloadService;

  @Override
  @Transactional
  public ProcessorMessages processPick(Picking pick, PickingDetailInput pickingDetailInput, Issue issue, Integer userId, boolean partialPick) {
    String errorString = null;
    String prnStr = "";
    val inventorySetupAmService = SpringAdfBindingUtils.getInventorySetupService();
    val workloadManagerService = SpringAdfBindingUtils.getWorkloadManagerService();
    val gcssTransactionService = SpringAdfBindingUtils.getGCSSMCTransactionsService();

    val pin = StringUtil.isEmpty(pickingDetailInput.getPin()) ? pick.getPin() : pickingDetailInput.getPin();

    pick.allocatePick(pickingDetailInput.getPickQty(), pin, userId);
    Runnable saveAllocatedPick = () -> {
      pickingService.save(pick);
    };

    transactionExecutor.execute("txSaveAllocatedPick", saveAllocatedPick, e -> {
      throw new StratisRuntimeException("Error saving Pick", e);
    });

    if (!CollectionUtils.isEmpty(pickingDetailInput.getSerials())) {
      val serialLotCriteria = SerialLotNumTrackSearchCriteria.builder()
          .niinId(pick.getNiinLocation().getNiinInfo().getNiinId())
          .locationId(pick.getNiinLocation().getLocation().getLocationId())
          .cc("A")
          .issueFlag("N").build();

      val srvo = workloadManagerService.getSerialOrLotNumList1();

      pickingDetailInput.getSerials().forEach(x -> {
        serialLotCriteria.setSerialNumber(x);
        val serialLotNums = serialLotNumTrackService.search(serialLotCriteria);
        if (CollectionUtils.isEmpty(serialLotNums))
          throw new StratisRuntimeException(String.format("No Serial Lot Number found matching the serial number %s", x));
        if (serialLotNums.size() > 1) {
          throw new StratisRuntimeException("More then one Serial Lot Num record found.");
        }

        val serialLotNum = serialLotNums.get(0);
        Row srow = srvo.createRow();
        srow.setAttribute("SerialOrLoNum", x);
        srow.setAttribute("SrlLotNumTrackId", serialLotNum.getSerialLotNumTrackId());
        srow.setAttribute("LocationId", serialLotNum.getLocationId());
        srow.setAttribute("QtyLot", "1");
        srvo.insertRow(srow);
      });

      workloadManagerService.createSrlOrLotAndPickXref(pick.getPid().toString(), userId.toString());
      if (srvo != null) {
        srvo.clearCache();
      }
    }

    if ((issue.getIssueType() == null || (!"R".equals(issue.getIssueType()) && !"W".equals(issue.getIssueType())))
        && (gcssTransactionService.getSiteGCCSSMCFlag().equalsIgnoreCase("Y"))) {
      gcssTransactionService.sendSROGCSSMCTransaction(pick.getPid().toString());
    }

    String pickPackMessage = null;
    boolean rewarehouse = false;
    if (issue.getIssueType() == null || !"R".equals(issue.getIssueType())) {
      String packStation = null;
      String mcpxOnly = pickingDetailInput.getMcpx();
      if (pick.getPickQty() > pick.getQtyPicked() || (mcpxOnly.equalsIgnoreCase("Y"))) {
        //Get a differet packing station
        val packStationResult = workloadService.getPackingStation(pick, userId, pick.getQtyPicked(), 1);
        packStation = packStationResult.packingStationName();
      }
      else {
        if (pick.getPackingConsolidationId() != null)
          packStation = workloadManagerService.getAssignedPackingStationName(pick.getPackingConsolidationId().toString());
      }

      if (((packStation == null) || (packStation.length() <= 0) || (packStation.toUpperCase().contains("ERROR")))) {

        errorString = "Could not get packing station";
        //couldn't get the packing station, so reset the pick back to ready.
        pick.resetPick(pick.getQtyPicked() - pickingDetailInput.getPickQty());

        Runnable savePick = () -> {
          pickingService.save(pick);
        };

        transactionExecutor.execute("txSavePick", savePick, e -> {
          throw new StratisRuntimeException("Error saving Pick", e);
        });
      }
      else {
        pickPackMessage = "Send " + pin + " to " + packStation;
      }
      prnStr = workloadManagerService.generatePrintPINLabelMobile(pin, 0, 20);
    }
    else {
      rewarehouse = true;
    }

    val stowCriteria = StowSearchCriteria.builder()
        .pid(pick.getPid())
        .build();
    val stows = stowService.search(stowCriteria);
    val originalStow = !CollectionUtils.isEmpty(stows) && stows.size() == 1 ? stows.get(0) : new Stow();
    val originalStowSid = originalStow.getSid();

    if (errorString == null) {
      //update niinLocation
      val niinLocation = niinLocationService.findById(pick.getNiinLocation().getNiinLocationId()).orElseThrow(() -> new StratisRuntimeException("invalid"));
      niinLocation.allocatePick(pickingDetailInput.getPickQty(), userId);

      Runnable saveNiinLocation = () -> {
        niinLocationService.save(niinLocation);
      };

      transactionExecutor.execute("txSaveNiinLocation", saveNiinLocation, e -> {
        throw new StratisRuntimeException("Error saving Niin Location", e);
      });

      val finalQtyCheck = pick.getNiinLocation().getQty() - pick.getQtyPicked() - pickingDetailInput.getPickQty();
      if (finalQtyCheck < 10 || partialPick) { //Spot Check
        inventorySetupAmService.setUserId(userId);
        if (partialPick)
          errorString = inventorySetupAmService.verifyLocationSpotCheck((Object) pick.getNiinLocation().getNiinLocationId(), 0);
        else
          errorString = inventorySetupAmService.verifyLocationSpotCheck((Object) pick.getNiinLocation().getNiinLocationId(), pickingDetailInput.getInventoryCount());
        if (!errorString.toUpperCase().contains("ERROR"))
          errorString = null;
        else
          errorString = "<error value=\"" + errorString + "\"/>\n";
      }
      if (partialPick) { //Partial Pick
        if ((issue.getIssueType() == null || (!"R".equals(issue.getIssueType()) && !"W".equals(issue.getIssueType())))
            && (gcssTransactionService.getSiteGCCSSMCFlag().equalsIgnoreCase("Y"))) {
          //* send SRO transaction (for non-rewarehouse picks only)
          gcssTransactionService.sendSROGCSSMCTransaction(pick.getPid().toString());
        }
        workloadManagerService.returnBestPickLocation(null, pick.getNiinLocation().getNiinInfo().getNiinId(), pick.getPid(), pick.getNiinLocation().getCc());
      }

      //--------------
      //Process for ReWarehouse to create stow rows in serial/lot cases
      AtomicReference<Integer> totalQty = new AtomicReference<>(0);

      val pickSerialSearchCriteria = PickSerialLotNumSearchCriteria.builder()
          .pid(pick.getPid())
          .build();

      val pickSerials = pickSerialLotNumService.search(pickSerialSearchCriteria);

      val stowSearch = StowSearchCriteria.builder()
          .pid(pick.getPid())
          .build();
      val oldStows = stowService.search(stowSearch);

      if (!CollectionUtils.isEmpty(pickSerials) && !oldStows.isEmpty()) {
        val oldStow = oldStows.get(0);
        pickSerials.forEach(pickSerial -> {

          val newStow = Stow.builder()
              .stowId(0) //will cause an insert.
              .sid(workloadManagerService.assignSID())
              .qtyToBeStowed(pickSerial.getQty())
              .receipt(oldStow.getReceipt())
              .createdBy(1) //why is this set to 1, and not the user causing the creation?
              .pick(pick)
              .status(oldStow.getStatus())
              .expirationDate(pickSerial.getSerialLotNumTrack().getExpirationDate())
              .location(oldStow.getLocation())
              .lotConNum(pickSerial.getSerialLotNumTrack().getLotConNum())
              .caseWeightQty(oldStow.getCaseWeightQty())
              .packedDate(oldStow.getPackedDate())
              .serialNumber(pickSerial.getSerialLotNumTrack().getSerialNumber())
              .dateOfManufacture(oldStow.getDateOfManufacture())
              .stowQty(0)
              .createdDate(OffsetDateTime.now())
              .scanInd("N") //default is N
              .build();

          Runnable saveStow = () -> {
            stowService.update(newStow);
          };

          transactionExecutor.execute("txSaveStow", saveStow, e -> {
            throw new StratisRuntimeException("Error saving Stow", e);
          });
          totalQty.set(totalQty.get() + pickSerial.getQty());
        });

      }

      //Update Receipt
      //if the originalStow has no ID, no stow existed.
      if (originalStowSid != null && !CollectionUtils.isEmpty(pickSerials)) {
        if (originalStow.getReceipt() != null) {
          originalStow.getReceipt().processPickForReceipt(totalQty.get());
          originalStow.setStatus("STOW CANCEL");
          originalStow.setModifiedBy(1);

          //we run an update, then delete because the db triggers log the cancellation of the stow prior to us deleting it.
          Runnable updateStow = () -> {
            stowService.update(originalStow);
          };
          transactionExecutor.execute("txUpdateStow", updateStow, e -> {
            throw new StratisRuntimeException("Error updating Stow", e);
          });

          Runnable deleteStow = () -> {
            stowService.delete(originalStow);
          };

          transactionExecutor.execute("txDeleteStow", deleteStow, e -> {
            throw new StratisRuntimeException("Error deleting Stow", e);
          });
        }
      }
      if (rewarehouse) {
        prnStr = workloadManagerService.generatePrintAllSIDLabelUsingRcn(originalStowSid, 0, 20, true);
      }
    }

    PickingListManager.removePickFromUser(userId, pick.getPid());

    return ProcessorMessages.builder()
        .errorMessage(errorString)
        .prnMessage(prnStr)
        .pickPackMessage(pickPackMessage)
        .build();
  }

  @Override
  @Transactional
  public void processPickBypass(Picking pick, PickingDetailInput pickingDetailInput, Integer userId) {

    val modifiedDate = OffsetDateTime.now();
    pick.confirmBypassPick(userId);
    pickingService.save(pick);

    ErrorQueue errorQueue = ErrorQueue.builder()
        .status("Open")
        .createdBy(userId)
        .createdDate(modifiedDate)
        .eid(pickingDetailInput.getBypassReason())
        .keyType("PID")
        .keyNum(pick.getPid().toString())
        .notes(null)
        .build();
    errorQueueService.save(errorQueue);

    PickingListManager.removePickFromUser(userId, pick.getPid());
  }

  @Override
  @Transactional
  public void processPickRefuse(Picking pick, PickingDetailInput pickingDetailInput, Integer userId) {

    val modifiedDate = OffsetDateTime.now();
    pick.refusePick(userId);
    pickingService.save(pick);

    ErrorQueue errorQueue = ErrorQueue.builder()
        .status("Open")
        .createdBy(userId)
        .createdDate(modifiedDate)
        .eid(pickingDetailInput.getRefuseReason())
        .keyType("PID")
        .keyNum(pick.getPid().toString())
        .notes(null)
        .build();
    errorQueueService.save(errorQueue);

    PickingListManager.removePickFromUser(userId, pick.getPid());
  }

  public void processForNext(SpaPostResponse spaPostResponse, UserInfo user) {
    val userId = user.getUserId();
    //If there is a next return to detail to process, otherwise go to the picking home page.
    if (PickingListManager.hasNext(userId)) {
      spaPostResponse.setRedirectUrl(MappingConstants.PICKING_DETAIL);
    }
    else {
      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val wacId = workstation.getWac().getWacId();

      //lets check to see if there are more picks to be assigned.
      List<String> pickingStatuses = new ArrayList<>();
      pickingStatuses.add("PICK READY");
      pickingStatuses.add("PICK PARTIAL");
      pickingStatuses.add("PICK BYPASS1");
      val pickingCriteria = PickingSearchCriteria.builder()
          .wacId(wacId)
          .statuses(pickingStatuses)
          .build();
      val pickCount = pickingService.count(pickingCriteria);

      spaPostResponse.addNotification("All Picks have been processed.");
      if (pickCount <= 0) {
        spaPostResponse.setRedirectUrl(MappingConstants.SMV_HOME);
      }
      else {
        spaPostResponse.setRedirectUrl(MappingConstants.PICKING_HOME);
      }
    }
  }

  @Transactional(readOnly = true)
  public String getPackingStationName(Picking picking) throws RuntimeException {
    val consolidation = consolidationRepository.findById(picking.getPackingConsolidationId());
    if (!consolidation.isPresent()) {
      throw new RuntimeException("Cannot find packing consolidation");
    }
    val packStation = packingStationRepository.findById(consolidation.get().getPackingStationId());
    if (!packStation.isPresent()){
      throw new RuntimeException("Cannot find packing station");
    }
    val equipment = equipmentService.findById(packStation.get().getEquipmentNumber());
    if (!equipment.isPresent()){
      throw new RuntimeException("Cannot find equipment");
    }
    return equipment.get().getName();
  }
}
