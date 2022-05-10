package mil.usmc.mls2.stratis.modules.smv.api.stowing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.stratis.common.util.RegUtils;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.StowingListManager;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.STOWING_DETAILS)
@RequiredArgsConstructor
public class StowDetailController {

  private static final String DEFAULT_PAGE = "mobile/stowing/detail";
  private static final String ERROR_KEY = "SID";

  private final EquipmentService equipmentService;
  private final StowService stowService;
  private final NiinLocationService niinLocationService;
  private final NiinInfoService niinInfoService;
  private final LocationService locationService;
  private final SerialLotNumTrackService serialLotNumTrackService;
  private final ErrorQueueService errorQueueService;
  private final ErrorService errorService;
  private final PickSerialLotNumService pickSerialLotNumService;
  private final RefUiService refUiService;
  private final GlobalConstants globalConstants;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    //get current ssid ordered
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val wac = workstation.getWac();
      request.setAttribute("stowLoss", "false");
      request.setAttribute("stowRelocate", "false");
      //User might not be logged into a valid WAC.
      if (wac != null) {
        request.setAttribute("wac", wac.getWacNumber());

        if (StowingListManager.hasNext(user.getUserId())) {
          //set the data
          Stow entity = StowingListManager.getNextStowForUser(user.getUserId());
          assert entity != null;
          request.setAttribute("stowId", entity.getStowId());
          request.setAttribute("location", entity.getLocation().getLocationLabel());
          request.setAttribute("niin", entity.getReceipt().getNiinInfo().getNiin());
          request.setAttribute("sid", entity.getSid());
          request.setAttribute("Nomenclature", entity.getReceipt().getNiinInfo().getNomenclature());
          request.setAttribute("qtyToStow", entity.getQtyToBeStowed());
        }
        else {
          //no stows left
          val resp = SMVUtility.processGetResponse(request, "mobile/stowing/home", "Stowing Home");
          resp.addWarning("No stows to store");
          resp.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
          return resp;
        }
      }
    }
    catch (Exception e) {
      log.error("Error Retrieving stow record", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Stowing Detail");
  }

  @PostMapping("/submit")
  public SpaPostResponse submit(StowDetailsInput stowDetailsInput, HttpServletRequest request) {
    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
    val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
    val wac = workstation.getWac();
    val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.STOWING_DETAILS);

    Stow stow;
    String stowLocation = "";
    try {
      stow = StowingListManager.getCurrentStowForUser(user.getUserId(), stowDetailsInput.getStowId());
    }
    catch (StratisRuntimeException sre) {
      log.error(String.format("Failed to retrieve stowing record for id [%d]", stowDetailsInput.getStowId()), sre);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(sre));
    }

    boolean stowLoss = "loss".equals(stowDetailsInput.getStowLoss());
    boolean isAStowLoss = false;
    //validation
    int stowQtyEntered = 0;
    List<String> errors = new ArrayList<>();
    //check the qty entered
    boolean validNumber = validateQtyFormat(stowDetailsInput.getStowQty(), errors);
    if (validNumber) { //check that it passed basic validation before parsing
      try {
        stowQtyEntered = Integer.parseInt(stowDetailsInput.getStowQty());
        this.validateQty(stow, stowQtyEntered, errors);
        isAStowLoss = this.validateQtyWithLoss(stow, stowQtyEntered, stowLoss, errors);
      }
      catch (Exception e) {
        errors.add("Invalid Stow Quantity");
      }

      //check locations and is a user entered another valid location
      boolean stowRelocate = "relocate".equals(stowDetailsInput.getStowRelocate());
      if (!stowRelocate && !stow.getLocation().getLocationLabel().equalsIgnoreCase(stowDetailsInput.getLocation()) && !stowDetailsInput.getLocation().isEmpty()) {
        val resp = SMVUtility.processPostResponse(request.getSession(), MappingConstants.STOWING_DETAILS);
        resp.addFlag("locationMisMatch");
        resp.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return resp;
      }
      Location newLocation = validateLocationReturnNewLocation(stow, stowDetailsInput, errors);
      List<NiinLocation> locations = getLocationsForStow(stow, newLocation);
      validateStowingLocation(stow, wac, newLocation, locations, errors); //check

      this.validateNiin(stow, stowDetailsInput, errors);
      this.validateUiDifferences(locations, errors); //check
      if (!errors.isEmpty()) {
        val resp = SMVUtility.processPostResponse(request.getSession(), MappingConstants.STOWING_DETAILS);
        for (String e : errors) {
          resp.addWarning(e);
        }
        if (isAStowLoss) {
          resp.addFlag("stowLoss");
        }
        resp.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return resp;
      }

      //check nsn and post a message to user if needed
      String nsnMessage = this.checkNSN(locations); //check
      if (nsnMessage != null) { response.addNotification(nsnMessage); }
      String uiMessage = this.getUiRepackMessage(locations); //check
      if (uiMessage != null) { response.addNotification(uiMessage); }

      try {
        //save the record
        createOrUpdateNiinLocation(stow, user, newLocation, stowQtyEntered, locations);
        stowRowStatusChange(stow, user, newLocation, stowQtyEntered, stowLoss);

        if (!stowLoss) updateErrorQueueRecordStatus(stow, user);
        else createStowLossRecord(stow, user);

        stowLocation = newLocation != null ? newLocation.getLocationLabel() : stow.getLocation().getLocationLabel();
      }
      catch (StratisRuntimeException sre) {
        log.error("Error saving stow record", sre);
        response.addWarning(sre.getMessage());

        if (sre.getMessage().contains("Duplicate STW transaction found.")) determineRedirect(user, stow, response);

        return response;
      }
      catch (Exception e) {
        log.error("Error saving stow record", e);
        return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
      }
    }

    //decide to go to the next page or go home
    determineRedirect(user, stow, response);
    response.addNotification(String.format("Stow successfully processed and stowed to %s", stowLocation));
    return response;
  }

  @PostMapping("/bypass")
  public SpaPostResponse bypass(StowDetailsInput stowDetailsInput, HttpServletRequest request) {
    try {
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val stow = StowingListManager.getCurrentStowForUser(user.getUserId(), stowDetailsInput.getStowId());
      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.STOWING_BYPASS);

      if (stow.getBypassCount() != null && stow.getBypassCount() <= 0) {
        //first bypass update
        stow.setStatus("STOW BYPASS1");
        stow.setBypassCount(1);
        stow.setModifiedBy(user.getUserId());
        stow.setAssignToUser(null);
        stowService.update(stow);
        determineRedirect(user, stow, response);
        response.addNotification("Stow successfully bypassed");
      }
      //second bypass goto bypass page
      return response;
    }
    catch (StratisRuntimeException sre) {
      log.error(String.format("Failed to retrieve stowing record for id [%d]", stowDetailsInput.getStowId()), sre);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(sre));
    }
  }

  private void determineRedirect(UserInfo user, Stow stow, SpaPostResponse response) {

    StowingListManager.removeStowFromUser(user.getUserId(), stow.getStowId());
    if (StowingListManager.hasNext(user.getUserId())) response.setRedirectUrl(MappingConstants.STOWING_DETAILS);
    else response.setRedirectUrl(MappingConstants.STOWING_HOME);
  }

  private boolean hasMoreScannedStows(Integer uid, Wac wac) {
    val criteria = StowSearchCriteria.openStowsForUser(wac);
    val stows = stowService.createAssignedListOfStowsForUser(
        criteria,
        uid,
        wac,
        1L);

    return !stows.isEmpty();
  }

  private void createStowLossRecord(Stow stow, UserInfo user) {
    Error stowError = errorService.findByCode("STOW_LOSS_D9");
    if (stowError != null) {
      ErrorQueue error = ErrorQueue.builder()
          .status("Open")
          .createdBy(user.getUserId())
          .createdDate(OffsetDateTime.now())
          .eid(stowError.getId())
          .keyType(ERROR_KEY)
          .keyNum(stow.getSid()).build();
      errorQueueService.save(error);
    }
    else {
      log.error("Failed to find error code STOW_LOSS_D9 when creating stow loss");
    }
  }

  private void createOrUpdateNiinLocation(Stow stow, UserInfo user, Location newLocation, int stowQtyEntered, List<NiinLocation> locations) {
    int niinId = 0;
    int locId = newLocation != null ? newLocation.getLocationId() : stow.getLocation().getLocationId();
    String cc = "A";
    String prjCd = "";
    String pc = "A";
    Receipt receipt = stow.getReceipt();
    if (receipt != null) {
      niinId = receipt.getNiinInfo().getNiinId();
      cc = receipt.getCc();
      prjCd = receipt.getProjectCode();
      pc = receipt.getPc();
    }

    Integer niinLocId = 0;
    if (!locations.isEmpty()) {
      NiinLocation locEntity = locations.get(0);
      niinLocId = locEntity.getNiinLocationId();
    }

    //Note: moved validation logic from the original CreateOrUpdateNiinLocation in StowingAMImpl to ValidateLocation

    if (niinLocId == 0) {
      NiinLocation newNiinLocation = NiinLocation.builder()
          .location(locationService.findById(locId).orElseThrow(() -> new StratisRuntimeException(String.format("Failed to find location during niin location create %s", locId))))
          .niinInfo(niinInfoService.findById(niinId).orElseThrow(() -> new StratisRuntimeException("Failed to find NiinInfo during niin location create")))
          .qty(stowQtyEntered)
          .expirationDate(stow.getExpirationDate() == null ? DateConstants.maxLocalDate : stow.getExpirationDate())
          .dateOfManufacture(stow.getDateOfManufacture())
          .cc(cc)
          .locked("N")
          .createdBy(user.getUserId())
          .projectCode(prjCd)
          .pc(pc)
          .caseWeightQty(stow.getCaseWeightQty())
          .lotConNum(stow.getLotConNum())
          .serialNumber(stow.getSerialNumber())
          .packedDate(stow.getPackedDate())
          .numExtents(0)
          .numCounts(0)
          .underAudit("N")
          .build();
      niinLocationService.save(newNiinLocation);
    }
    else {
      //Update qty on NIIN_LOCATION
      //get niin location
      val niinLoc = niinLocationService.findById(niinLocId);
      if (niinLoc.isPresent()) {
        NiinLocation niinLocation = niinLoc.get();
        niinLocation.stowNiin(stowQtyEntered, user.getUserId());
        niinLocationService.save(niinLocation);
      }
      else {
        //throw error
        throw new StratisRuntimeException(String.format("Error updating the niin location for id %d.", niinLocId));
      }
    }

    //create serial_lot_num_track row
    int srlTrackId = 0;
    if (stow.getSerialNumber() != null || stow.getLotConNum() != null) {
      if ((stow.getSerialNumber() != null || stow.getSerialNumber().length() <= 0) && (stow.getLotConNum() != null)) {
        //get track id
        SerialLotNumTrackSearchCriteria trackSearch = SerialLotNumTrackSearchCriteria.builder()
            .lotConNum(stow.getLotConNum())
            .niinId(niinId)
            .cc(cc)
            .locationId(locId).build();
        List<SerialLotNumTrack> serials = serialLotNumTrackService.search(trackSearch);
        if (!serials.isEmpty()) {
          srlTrackId = serials.get(0).getSerialLotNumTrackId();
        }
        if (srlTrackId > 0) { //Case of existing lot number
          val serialEntity = serials.get(0);
          serialEntity.setQty(serialEntity.getQty() + stowQtyEntered);
          serialLotNumTrackService.update(serialEntity);
        }
      }
      if (srlTrackId <= 0) { //Case of serial or new lot number
        SerialLotNumTrack newSerial = new SerialLotNumTrack();
        newSerial.setNiinId(niinId);
        newSerial.setSerialNumber(stow.getSerialNumber());
        newSerial.setLotConNum(stow.getLotConNum());
        newSerial.setCc(cc);
        newSerial.setExpirationDate(stow.getExpirationDate());
        newSerial.setQty(stowQtyEntered);
        newSerial.setLocationId(locId);
        serialLotNumTrackService.update(newSerial);
      }
    }

    //Update last stow date on the Location and make sure the location is not available as new
    val locationOptional = locationService.findById(locId);
    if (locationOptional.isPresent()) {
      Location loc = locationOptional.get();
      loc.setModifiedBy(user.getUserId());
      loc.setAvailabilityFlag("U");
      loc.setLastStowDate(OffsetDateTime.now());
      locationService.update(loc);
    }
  }

  private void stowRowStatusChange(Stow stow, UserInfo user, Location newLocation, int stowQtyEntered, boolean stowLoss) {
    val gcssTransactionService = SpringAdfBindingUtils.getGCSSMCTransactionsService();

    // Save the value as it's updated in the next statement
    int stowQty = null != stow.getStowQty() ? stow.getStowQty() : 0;

    stow.stowStatusChange(
        newLocation != null ? newLocation : stow.getLocation(),
        stowQtyEntered + stowQty,
        user.getUserId());
    stowService.update(stow);

    //Update the stow status only if all the quantity is stowed
    if ((stowQty + stowQtyEntered) == stow.getQtyToBeStowed() || stowLoss) {
      stow.setStatus(stowLoss ? "STOW LOSS" : "STOWED");
      stow.setModifiedBy(user.getUserId());
      stowService.update(stow);
      //do we need to call transaction.sendSTWGCSSMCTransaction(sidL)
      if (gcssTransactionService.getSiteGCCSSMCFlag().equalsIgnoreCase("Y") && !(stow.getReceipt().getDocumentID().equals("YLL"))) {
        val returnCode = gcssTransactionService.sendSTWGCSSMCTransaction(stow.getSid());
        if (returnCode == -2) throw new StratisRuntimeException("Duplicate STW transaction found.");
      }

      val hasDocId = null != stow.getReceipt() && null != stow.getReceipt().getDocumentID();
      if (null != stow.getSerialNumber() && null != stow.getPick() && hasDocId && stow.getReceipt().getDocumentID().equals("YLL")) {
        val hasNiinInfo = null != stow.getReceipt() && null != stow.getReceipt().getNiinInfo();
        val hasPick = null != stow.getPick();
        //need to clean up Picking_lot_num_track and the serial_lot_num_track
        val serialCriteria = SerialLotNumTrackSearchCriteria.builder()
            .serialNumber(stow.getSerialNumber())
            .niinId(hasNiinInfo ? stow.getReceipt().getNiinInfo().getNiinId() : null)
            .pickPid(hasPick ? stow.getPick().getPid() : null)
            .notLocationId(stow.getLocation().getLocationId()) //during rewarehousing there are 2 serial rows one for this new stow location and one for the previous picked from location
            .build();

        val serialQuery = serialLotNumTrackService.search(serialCriteria).stream().findFirst();

        if (serialQuery.isPresent()) {
          //get picks
          val picks = pickSerialLotNumService.search(PickSerialLotNumSearchCriteria.builder().serialLotNumTrackId(serialQuery.get().getSerialLotNumTrackId()).build());
          for (PickSerialLotNum p : picks) {
            pickSerialLotNumService.delete(p);
          }
          // delete from serial_lot_num_track where serial_lot_num_track_id = ?
          serialLotNumTrackService.delete(serialQuery.get());
        }
      }
    }
  }

  private void updateErrorQueueRecordStatus(Stow stow, UserInfo user) {
    val errorSearch = ErrorQueueCriteria.builder()
        .keyType(ERROR_KEY)
        .keyNum(stow.getSid()).build();
    val errorQuery = errorQueueService.search(errorSearch);
    for (ErrorQueue q : errorQuery) {
      q.setStatus("CLOSED");
      q.setModifiedBy(user.getUserId());
      q.setModifiedDate(OffsetDateTime.now());
      errorQueueService.save(q);
    }
  }

  private String convertDateToString(LocalDate d) {
    String defaultString = "019999";
    if (d == null) {
      return defaultString;
    }
    val format = DateTimeFormatter.ofPattern("MMyyyy");
    return format.format(d);
  }

  private List<NiinLocation> getLocationsForStow(Stow stow, Location newLocation) {
    // if the user selected a new location use that to get the NIIN locations
    NiinLocationSearchCriteria niinSearch = NiinLocationSearchCriteria.builder()
        .locationId(newLocation != null ? newLocation.getLocationId() : stow.getLocation().getLocationId())
        .niinId(stow.getReceipt().getNiinInfo().getNiinId())
        .cc(stow.getReceipt().getCc()).build();

    return niinLocationService.search(niinSearch);
  }

  private String checkNSN(List<NiinLocation> locations) {
    String msgStr = null;
    if (!locations.isEmpty()) {
      NiinLocation niinLocation = locations.get(0);
      if ("Y".equals(niinLocation.getNsnRemark())) {
        if (niinLocation.getNiinInfo().getFsc() != null && !niinLocation.getNiinInfo().getFsc().isEmpty()) {
          msgStr = "Remark FSC on Location Inventory to " + niinLocation.getNiinInfo().getFsc();
        }
        else {
          msgStr = "Remark FSC on Location Inventory";
        }
        niinLocation.resetRemark();
        niinLocationService.save(niinLocation);
      }
    }
    return msgStr;
  }

  private String getUiRepackMessage(List<NiinLocation> locations) {
    if (!locations.isEmpty()) {
      NiinLocation niinLocation = locations.get(0);
      NiinInfo niinInfo = niinLocation.getNiinInfo();
      String newUI = niinInfo.getUi() != null ? niinInfo.getUi() : "X";
      int actQty = niinLocation.getQty() != null ? niinLocation.getQty() : 0;
      String oldUI = niinLocation.getOldUi() != null ? niinLocation.getOldUi() : "X";
      int oldQty = niinLocation.getOldQty() != null ? niinLocation.getOldQty() : 0;
      if (oldUI.trim().compareToIgnoreCase("X") == 0) {
        return null;
      }
      double convFac = conversionFactor(newUI, oldUI);
      if (convFac <= 1) {
        return null;
      }
      niinLocation.resetUi();
      niinLocationService.save(niinLocation);
      return String.format("Repack location inventory from %d %s to %d %s", oldQty, oldUI, actQty, newUI);
    }
    return null;
  }

  private void validateUiDifferences(List<NiinLocation> locations, List<String> errors) {
    if (!locations.isEmpty()) {
      NiinLocation niinLocation = locations.get(0);
      NiinInfo niinInfo = niinLocation.getNiinInfo();
      String newUI = niinInfo.getUi() != null ? niinInfo.getUi() : "X";
      String oldUI = niinLocation.getOldUi() != null ? niinLocation.getOldUi() : "X";
      if (niinLocation.getOldUi() == null) {
        return;
      }
      double convFac = conversionFactor(newUI, oldUI);
      if (convFac < 0) {
        errors.add("The U/I at this location is not re-packaged and STRATIS cannot guide you with the conversion. Please talk to supervisor.");
      }
    }
  }

  private double conversionFactor(String newUI, String oldUI) {
    double convFac = -1;
    RefUi refUi = refUiService.findSingleMatch(RefUiSearchCriteria.builder().uiFrom(oldUI).uiTo(newUI).build());
    if (refUi != null) {
      convFac = Double.parseDouble(refUi.getConversionFactor().toString());
    }
    return convFac;
  }

  private Location validateLocationReturnNewLocation(Stow stow, StowDetailsInput controllerInput, List<String> errors) {
    if (controllerInput.getLocation() == null || controllerInput.getLocation().length() <= 8) {
      errors.add("Invalid location label length.");
      //return as there is no sense in checking a mal-formed location label
      return null;
    }
    if (!stow.getLocation().getLocationLabel().equalsIgnoreCase(controllerInput.getLocation())) {
      //check if entered location would be valid
      String locationValidationError = "";
      val altLocation = locationService.search(LocationSearchCriteria.builder()
          .locationLabel(controllerInput.getLocation().toUpperCase())
          .build())
          .stream()
          .findFirst().orElse(null);
      if (altLocation != null) {
        val niinId = stow.getReceipt().getNiinInfo().getNiinId();

        // Check that the location didn't hold another niin
        val locationNinns = niinLocationService.search(NiinLocationSearchCriteria.builder()
            .locationId(altLocation.getLocationId())
            .build());

        val otherNiins = locationNinns.stream().filter(t -> !t.getNiinInfo().getNiinId().equals(niinId)).findAny().isPresent();

        var non9999Niin = locationNinns.stream()
            .anyMatch(t -> !t.getNiinInfo().getNiinId().equals(niinId) && t.getExpirationDate().getMonthValue() != 1 && t.getExpirationDate().getYear() != 9999);

        var niinExpDiff = locationNinns.stream()
            .anyMatch(t -> t.getNiinInfo().getNiinId().equals(niinId) && !t.getExpirationDate().equals(stow.getExpirationDate()));

        val secureNiin = niinInfoService.findById(niinId)
            .map(NiinInfo::getScc)
            .map(scc -> !scc.equalsIgnoreCase("U"))
            .orElseThrow(() -> new StratisRuntimeException("No NIIN information for provided NIIN."));
        val secureAltLocation = altLocation.getWac().getSecureFlag().equalsIgnoreCase("Y");

        var validAltLocation = true;
        if (otherNiins && stow.getExpirationDate().getYear() != 9999 && stow.getExpirationDate().getMonthValue() != 1) {
          locationValidationError = "Invalid Location - Stow expiration date is not 9999 and can not be stowed to a location with other NIINs";
          validAltLocation = false;
        }
        if (niinExpDiff) {
          locationValidationError = "Invalid Location - Location at which you are trying to stow has a NIIN with a different Expiration Date. Please bypass or pick a different location.";
          validAltLocation = false;
        }
        if (non9999Niin) {
          locationValidationError = "Invalid Location - location already assigned a NIIN with a expiration date other than 9999";
          validAltLocation = false;
        }
        if (secureNiin && !secureAltLocation) {
          locationValidationError = "Invalid Location - location must be secure";
          validAltLocation = false;
        }

        if (validAltLocation) return altLocation;
      }
      else {
        locationValidationError = "Invalid Location - Location not available";
      }
      errors.add(locationValidationError);
    }
    return null;
  }

  private void validateStowingLocation(Stow stow, Wac wac, Location newLocation, List<NiinLocation> locations, List<String> errors) {

    //Note: validation logic from the original CreateOrUpdateNiinLocation in StowingAMImpl
    // this may need to use the new location if the user selected a location that is valid but would still need validation
    int niinId = 0;
    int locId = newLocation != null ? newLocation.getLocationId() : stow.getLocation().getLocationId();
    if (stow.getReceipt() != null) {
      niinId = stow.getReceipt().getNiinInfo().getNiinId();
    }

    Integer niinLocId = 0;
    String lock = "N";
    String cCStr = "A";
    String expDtStr = "019999";
    String underAudit = "N";
    if (!locations.isEmpty()) {
      NiinLocation locEntity = locations.get(0);
      niinLocId = locEntity.getNiinLocationId();
      lock = locEntity.getLocked();
      underAudit = locEntity.getUnderAudit();
      cCStr = locEntity.getCc();
      expDtStr = convertDateToString(locEntity.getExpirationDate());
    }
    else { //If it is a new location then copy the values that are there on the stow so that they match
      if (stow.getReceipt().getCc() != null && stow.getReceipt().getCc().length() > 0) {
        cCStr = stow.getReceipt().getCc();
      }
      if (stow.getExpirationDate() != null && stow.getExpirationDate().toString().length() > 0) {
        expDtStr = convertDateToString(stow.getExpirationDate());
      }
    }

    if ((niinLocId == 0) && (wac.getMechanizedFlag().equals("Y"))) {
      // Check if some other NIIN exist is at that location (FOR MECH ONLY)
      int nlIdStr = 0;
      NiinLocationSearchCriteria search2 = NiinLocationSearchCriteria.builder()
          .niinId(niinId)
          .niinIdMatch(false)
          .locationId(locId).build();

      val niinCheck = niinLocationService.search(search2);
      if (!niinCheck.isEmpty()) {
        nlIdStr = niinCheck.get(0).getNiinLocationId();
      }

      //Check this building has any empty locations
      //get locations in the same warehouse as the wac that it is to be stored in
      long nLoc = locationService.count(LocationSearchCriteria.builder().availabilityFlag("A").warehouseId(wac.getWarehouseId()).build());
      if (nlIdStr != 0 && nLoc > 0) {
        errors.add("Some other NIIN exists at this location. " + "Multiple NIIN stowing is not allowed in Mechanized unless all mechanized locations are full. " + "Please do Bypass for now and do reassigning of location by going to ReAssign menu tab.");
      }
    }

    String expAtStow = convertDateToString(stow.getExpirationDate());
    if (!expAtStow.equalsIgnoreCase(expDtStr) || !(stow.getReceipt().getCc() == null ? "A" : stow.getReceipt().getCc()).equalsIgnoreCase(cCStr)) {
      errors.add("Location at which you are trying to stow has a NIIN with different CC and/or Expiration date. Please Bypass and go to ReAssign SID to pick a different location.");
    }
    if ("Y".equalsIgnoreCase(lock) || "Y".equalsIgnoreCase(underAudit)) {
      errors.add("NIIN_LOCATION is either locked or under audit.");
    }
  }

  private void validateNiin(Stow stow, StowDetailsInput controllerInput, List<String> errors) {
    if (controllerInput.getLastNiin() == null || controllerInput.getLastNiin().length() != 2) {
      errors.add("Invalid last two digits of NIIN length.");
      //return as there is no sense in checking a mal-formed niin
      return;
    }
    if (!stow.getReceipt().getNiinInfo().getNiin().endsWith(controllerInput.getLastNiin())) {
      errors.add("Invalid last two digits of NIIN.");
    }
  }

  private boolean validateQtyFormat(String qty, List<String> errors) {
    boolean validNumber = true;
    if (RegUtils.isDecimal(qty)) {
      errors.add("Quantity must be whole number.");
      validNumber = false;
    }
    if (RegUtils.isNotNumeric(qty)) {
      errors.add("Quantity can only be numeric.");
      validNumber = false;
    }
    return validNumber;
  }

  private void validateQty(Stow stow, int stowQtyEntered, List<String> errors) {

    int sQty = stow.getStowQty() != null ? stow.getStowQty() : 0;
    int qtyS = stow.getQtyToBeStowed();
    if ((stowQtyEntered <= 0)) {
      errors.add("Quantity cannot be less than 0.");
    }
    if (stowQtyEntered > (qtyS - sQty)) {
      errors.add("Quantity entered is greater than the receipted quantity.");
    }
  }

  private boolean validateQtyWithLoss(Stow stow, int stowQtyEntered, boolean stowLoss, List<String> errors) {
    int sQty = stow.getStowQty() != null ? stow.getStowQty() : 0;
    int qtyS = stow.getQtyToBeStowed();
    if (!stowLoss && stowQtyEntered < (qtyS - sQty)) {
      errors.add("Quantity entered is less than than the receipted quantity. Process as Loss and submit loss to supervisor?");
      return true;
    }
    return false;
  }
}
