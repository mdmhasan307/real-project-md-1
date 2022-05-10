package mil.usmc.mls2.stratis.modules.smv.api.picking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.StringUtil;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.PICKING_DETAIL)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class PickingDetailController {

  private static final String DEFAULT_PAGE = "mobile/picking/detail";
  private static final String NO_PICK_FOUND = "No Pick found for PID %s";
  private static final String NO_ISSUE_FOUND = "No issue found for Pick";
  private static final String NO_PID = "No PID found for processing";

  private final EquipmentService equipmentService;
  private final IssueService issueService;
  private final PickingService pickingService;
  private final StowService stowService;
  private final PickingProcessor pickingProcessor;
  private final NiinLocationService niinLocationService;
  private final RefUiService refUiService;
  private final GlobalConstants globalConstants;
  private final LabelPrintUtil labelPrintUtil;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val pid = PickingListManager.getNextPickForUser(user.getUserId());

      if (pid == null) {
        //Assumption is you should not get here without having picks to work.
        throw new StratisRuntimeException("No Picks Available to process");
      }
      val pick = pickingService.findById(pid).orElseThrow(() -> new StratisRuntimeException(String.format(NO_PICK_FOUND, pid)));

      //if there will be 10 or less on the shelf after the pick, show the inventory inputs.
      val showInventory = (pick.getNiinLocation().getQty() - pick.getPickQty() <= 10);

      //get the issue matching the pick, otherwise return an empty issue.
      val issue = issueService.findByScn(pick.getScn()).orElseThrow(() -> new StratisRuntimeException(NO_ISSUE_FOUND));

      request.setAttribute("pick", pick);
      request.setAttribute("issue", issue);
      request.setAttribute("showInventory", showInventory);

      val pickingDetailInput = PickingDetailInput.builder().pid(pick.getPid()).build();
      request.setAttribute(globalConstants.getPickingDetailSessionAttrib(), pickingDetailInput);

      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Picking Details");
    }
    catch (Exception e) {
      log.error("Error occurred retrieving the Pick Details", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping
  public SpaPostResponse post(PickingDetailInput pickingDetailInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val pid = PickingListManager.getCurrentPickForUser(userId, pickingDetailInput.getPid());
      if (pid == null) {
        throw new StratisRuntimeException((NO_PID));
      }
      val pick = pickingService.findById(pid).orElseThrow(() -> new StratisRuntimeException(String.format(NO_PICK_FOUND, pid)));

      //get the issue matching the pick, otherwise return an empty issue.
      val issue = issueService.findByScn(pick.getScn()).orElseThrow(() -> new StratisRuntimeException(NO_ISSUE_FOUND));

      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), null);

      boolean foundValidationErrors = validate(pickingDetailInput, pick, user, issue, spaPostResponse);

      //possible to just have notifications, not validation warnings.
      if (foundValidationErrors) {
        spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return spaPostResponse;
      }

      if ("Y".equals(pick.getNiinLocation().getNiinInfo().getSerialControlFlag())) {
        spaPostResponse.setRedirectUrl(MappingConstants.PICKING_CONTROLLED_NIIN_DETAIL);
        request.getSession().setAttribute(globalConstants.getPickingDetailSessionAttrib(), pickingDetailInput);
      }
      else {
        val partialPick = pickingDetailInput.getPickQty() < pick.getPickQty();
        val processorMessages = pickingProcessor.processPick(pick, pickingDetailInput, issue, userId, partialPick);

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
            labelPrintUtil.printLabel(user, processorMessages.getPrnMessage(), new String[]{pick.getPin()}, LabelType.PICK_LABEL);
          }
        }
        spaPostResponse.addNotification("Pick processed successfully.");
        if (processorMessages.getPickPackMessage() != null) {
          spaPostResponse.addNotification(processorMessages.getPickPackMessage());
        }

        //If there is a next return to detail to process, otherwise go to the picking home page.
        pickingProcessor.processForNext(spaPostResponse, user);
      }

      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Picking Details post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping(path = "/bypass")
  public SpaPostResponse bypass(PickingDetailInput pickingDetailInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val pid = PickingListManager.getCurrentPickForUser(userId, pickingDetailInput.getPid());
      if (pid == null) {
        throw new StratisRuntimeException((NO_PID));
      }
      val pick = pickingService.findById(pid).orElseThrow(() -> new StratisRuntimeException(String.format(NO_PICK_FOUND, pid)));

      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), null);

      val currentByPassCount = pick.getBypassCount();
      if (currentByPassCount == 0) {
        //this means the pick has not yet been bypassed, so just set the status.
        pick.bypassPick(userId);
        pickingService.save(pick);
        PickingListManager.removePickFromUser(userId, pick.getPid());
        spaPostResponse.addNotification("Pick initial bypass processed successfully.");
        pickingProcessor.processForNext(spaPostResponse, user);
      }
      else {
        //this means the user must be sent to the bypass screen to enter a reason code.
        spaPostResponse.setRedirectUrl(MappingConstants.PICKING_BYPASS);
        request.getSession().setAttribute(globalConstants.getPickingDetailSessionAttrib(), pickingDetailInput);
      }
      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Picking Details Bypass", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping(path = "/refuse")
  public SpaPostResponse refuse(PickingDetailInput pickingDetailInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val pid = PickingListManager.getCurrentPickForUser(userId, pickingDetailInput.getPid());
      if (pid == null) {
        throw new StratisRuntimeException((NO_PID));
      }
      //double check to make sure the pick is still valid.
      pickingService.findById(pid).orElseThrow(() -> new StratisRuntimeException(String.format(NO_PICK_FOUND, pid)));

      request.getSession().setAttribute(globalConstants.getPickingDetailSessionAttrib(), pickingDetailInput);

      return SMVUtility.processPostResponse(request.getSession(), MappingConstants.PICKING_REFUSE);
    }
    catch (Exception e) {
      log.error("Error occurred processing Picking Details Refuse", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  private boolean validate(PickingDetailInput input, Picking pick, UserInfo user, Issue issue, SpaPostResponse spaPostResponse) {
    boolean validationWarningsFound = false;

    if ("Y".equals(pick.getNiinLocation().getLocked())) {
      spaPostResponse.addWarning("Location Locked - ByPass.");
      validationWarningsFound = true;
    }

    if (input.getLocation().length() <= 8) {
      spaPostResponse.addWarning("Invalid Location Label.");
      validationWarningsFound = true;
    }
    else if (!input.getLocation().equalsIgnoreCase(pick.getNiinLocation().getLocation().getLocationLabel())) {
      spaPostResponse.addWarning("Location entry mismatch, Re-enter location or bypass pick.");
      validationWarningsFound = true;
    }

    val niin = input.getNiin();
    val recordNiin = pick.getNiinLocation().getNiinInfo().getNiin();
    if (!StringUtil.isEmpty(niin)) {
      if (niin.length() != 2) {
        spaPostResponse.addWarning("Niin Length must be 2.");
        validationWarningsFound = true;
      }
      else if (!niin.equals(recordNiin.substring(recordNiin.length() - 2))) {
        spaPostResponse.addWarning("Invalid last two digits of NIIN.");
        validationWarningsFound = true;
      }
    }

    val qtyThatShouldBePicked = pick.getPickQty() - pick.getQtyPicked();
    if (input.getPickQty() == 0) {
      spaPostResponse.addWarning("Pick Quantity must be greater then 0.");
      validationWarningsFound = true;
    }
    else if (input.getPickQty() < qtyThatShouldBePicked) {
      if ("R".equals(issue.getIssueType())) {
        spaPostResponse.addWarning("Rewarehouse Quantity.  Bypass if quantity picked do not match.");
        validationWarningsFound = true;
      }
      else if (!input.isPartialPickAttempted()) {
        spaPostResponse.addWarning("Partial Quantity.  Re-enter Quantity, or submit again.");
        //set to true, so if they submit a second time with a partial this gets bypassed.
        spaPostResponse.addFlag("PartialPickAttempted");
        validationWarningsFound = true;
      }
    }
    else if (input.getPickQty() > qtyThatShouldBePicked) {
      spaPostResponse.addWarning(String.format("Pick Quantity must be less then or equal to the quantity requested (%s).", qtyThatShouldBePicked));
      validationWarningsFound = true;
    }

    val showInventory = (pick.getNiinLocation().getQty() - pick.getPickQty() <= 10);

    if (showInventory && !input.getInventoryCount().equals(input.getReInventoryCount())) {
      spaPostResponse.addWarning("Please reenter the inventory quantities.  They do not match.");
      validationWarningsFound = true;
    }

    if (!StringUtil.isEmpty((input.getPin()))) {
      if (input.getPin().length() < 5 || input.getPin().length() > 10) {
        spaPostResponse.addWarning("Pin must be between 5 and 10 character alphanumeric.");
        validationWarningsFound = true;
      }

      val pinAlreadyExists = !pickingService.search(
          PickingSearchCriteria.builder()
              .pin(input.getPin())
              .build())
          .isEmpty();

      if (pinAlreadyExists) {
        spaPostResponse.addWarning("Pin already exists, please enter a unique pin.");
        validationWarningsFound = true;
      }
    }

    //return here if any form validation has occurred, otherwise continue to check the UIDifferences.
    if (validationWarningsFound) return true;

    val nsnRemark = pick.getNiinLocation().getNsnRemark();
    if ("Y".equals(nsnRemark)) {
      val niinLocation = niinLocationService.findById(pick.getNiinLocation().getNiinLocationId()).orElseThrow(() -> new StratisRuntimeException("invalid"));
      niinLocation.resetRemark();
      niinLocationService.save(niinLocation);
      val fsc = pick.getNiinLocation().getNiinInfo().getFsc();
      val message = new StringBuilder().append("Remark FSC on Location Inventory");
      if (fsc != null) {
        message.append(String.format(" to %s", fsc));
      }
      spaPostResponse.addNotification(message.toString());
    }

    return validateUIDifferences(pick, user, spaPostResponse);
  }

  private boolean validateUIDifferences(Picking pick, UserInfo user, SpaPostResponse spaPostResponse) {

    val newUi = pick.getNiinLocation().getNiinInfo().getUi();
    val actQty = pick.getNiinLocation().getQty();
    val oldUi = pick.getNiinLocation().getOldUi();
    val oldQty = pick.getNiinLocation().getOldQty();
    if (oldUi != null) {
      val searchCriteria = RefUiSearchCriteria.builder()
          .uiFrom(oldUi)
          .uiTo(newUi)
          .build();
      val refUi = refUiService.findSingleMatch(searchCriteria);
      if (refUi == null) {
        spaPostResponse.addWarning("The U/I at this location is not re-packaged and STRATIS cannot guide you with the conversion.  Please talk to supervisor.");
        return true;
      }
      else if (refUi.getConversionFactor() == 1) {
        return false;
      }

      val niinLocation = niinLocationService.findById(pick.getNiinLocation().getNiinLocationId()).orElseThrow(() -> new StratisRuntimeException("invalid"));
      niinLocation.repack(user.getUserId());
      niinLocationService.save(pick.getNiinLocation());
      spaPostResponse.addNotification(String.format("Repack location inventory from %s %s %s %s", oldQty, oldUi, actQty, newUi));
      return false;
    }
    return false;
  }
}
