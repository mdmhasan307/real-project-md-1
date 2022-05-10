package mil.usmc.mls2.stratis.modules.smv.api.picking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.PickingListManager;
import mil.usmc.mls2.stratis.core.processor.PickingProcessor;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.LabelPrintUtil;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import mil.usmc.mls2.stratis.modules.smv.utility.ValidationUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.PICKING_CONTROLLED_NIIN_DETAIL)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class PickingControlledNiinDetailController {

  private static final String DEFAULT_PAGE = "mobile/picking/controlledNiinDetail";

  private final EquipmentService equipmentService;
  private final IssueService issueService;
  private final PickingService pickingService;
  private final StowService stowService;
  private final PickingProcessor pickingProcessor;
  private final SerialLotNumTrackService serialLotNumTrackService;
  private final GlobalConstants globalConstants;
  private final LabelPrintUtil labelPrintUtil;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }
      //pickingDetail controller puts the input into session for use here.
      val pickingDetailInput = (PickingDetailInput) request.getSession().getAttribute("pickingDetailInput");

      val user = (UserInfo) request.getSession().getAttribute("userbean");
      val pid = PickingListManager.getCurrentPickForUser(user.getUserId(), pickingDetailInput.getPid());
      if (pid == null) {
        throw new StratisRuntimeException(("No PID found for processing"));
      }

      val pick = pickingService.findById(pid).orElseThrow(() -> new StratisRuntimeException(String.format("No Pick found for PID %s", pid)));

      val showInventory = (pick.getNiinLocation().getQty() - pick.getPickQty() <= 10);

      //get the issue matching the pick, otherwise return an empty issue.
      val issue = issueService.findByScn(pick.getScn()).orElseThrow(() -> new StratisRuntimeException("No issue found for Pick"));

      request.setAttribute("pick", pick);
      request.setAttribute("issue", issue);
      request.setAttribute("showInventory", showInventory);

      request.setAttribute(globalConstants.getPickingDetailSessionAttrib(), pickingDetailInput);
      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Controlled NIIN Details");
    }
    catch (Exception e) {
      log.error("Error occurred retrieving the Pick Details for Controlled NIIN", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping
  public SpaPostResponse post(PickingDetailInput pickingDetailInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }

      val user = (UserInfo) request.getSession().getAttribute("userbean");

      val pid = PickingListManager.getCurrentPickForUser(user.getUserId(), pickingDetailInput.getPid());
      if (pid == null) {
        throw new StratisRuntimeException(("No PID found for processing"));
      }

      val pick = pickingService.findById(pid).orElseThrow(() -> new StratisRuntimeException(String.format("No Pick found for PID %s", pid)));

      //get the issue matching the pick, otherwise return an empty issue.
      val issue = issueService.findByScn(pick.getScn()).orElseThrow(() -> new StratisRuntimeException("No issue found for Pick"));

      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), null);

      if (ValidationUtility.validateSerialNumberInput(pickingDetailInput.getSerials(), pickingDetailInput.getPickQty(), spaPostResponse)) {
        spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return spaPostResponse;
      }

      if (validateSerialNumbersDb(pickingDetailInput.getSerials(), pick, spaPostResponse)) {
        spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return spaPostResponse;
      }
      val partialPick = pickingDetailInput.getPickQty() < pick.getPickQty();
      val processorMessages = pickingProcessor.processPick(pick, pickingDetailInput, issue, user.getUserId(), partialPick);

      if (processorMessages.getErrorMessage() != null) {
        //an error was received.
        //in the main application this error is used:  JSFUtils.addFacesErrorMessage("INV_ERR_MSG", errorMessage)
        //Unsure where INV_ERR_MSG is used, so instead adding it to notifications for the user.
        spaPostResponse.addNotification(processorMessages.getErrorMessage());
      }

      if (processorMessages.getPrnMessage() != null) {
        if ("R".equalsIgnoreCase(issue.getIssueType())) {
          //rewarehouse
          val stowSearch = StowSearchCriteria.builder()
              .pid(pick.getPid())
              .build();
          val stows = stowService.search(stowSearch);
          labelPrintUtil.printLabel(user, processorMessages.getPrnMessage(), stows.stream().map(Stow::getSid).toArray(String[]::new), LabelType.STOW_LABEL);
        }
        else {
          labelPrintUtil.printLabel(user, processorMessages.getPrnMessage(), new String[] { pick.getPin() }, LabelType.PICK_LABEL);
        }
      }

      //remove picking detail input from session.
      request.getSession().removeAttribute(globalConstants.getPickingDetailSessionAttrib());
      spaPostResponse.addNotification("Pick processed successfully.");
      if (processorMessages.getPickPackMessage() != null) {
        spaPostResponse.addNotification(processorMessages.getPickPackMessage());
      }
      pickingProcessor.processForNext(spaPostResponse, user);
      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Controlled NIIN Details post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  private boolean validateSerialNumbersDb(List<String> serialNumbers, Picking pick, SpaPostResponse spaPostResponse) {
    val serialLotCriteria = SerialLotNumTrackSearchCriteria.builder()
        .niinId(pick.getNiinLocation().getNiinInfo().getNiinId())
        .locationId(pick.getNiinLocation().getLocation().getLocationId())
        .cc("A")
        .issueFlag("N").build();

    List<String> invalidSerialNumbers = new ArrayList<>();
    serialNumbers.forEach(x -> {
      serialLotCriteria.setSerialNumber(x);
      val matches = serialLotNumTrackService.search(serialLotCriteria);
      if (CollectionUtils.isEmpty(matches)) {
        invalidSerialNumbers.add(x);
      }
    });

    if (CollectionUtils.isEmpty(invalidSerialNumbers)) return false;

    spaPostResponse.addWarning(String.format("Invalid Serial numbers found.  Invalid serials(s): %s", String.join(",", invalidSerialNumbers)));
    return true;
  }
}
