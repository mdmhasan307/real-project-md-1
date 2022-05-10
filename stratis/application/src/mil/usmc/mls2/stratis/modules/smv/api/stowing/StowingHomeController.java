package mil.usmc.mls2.stratis.modules.smv.api.stowing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.StowingListManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.StowService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.STOWING_HOME)
@RequiredArgsConstructor
public class StowingHomeController {

  private final GlobalConstants globalConstants;
  private static final String DEFAULT_PAGE = "mobile/stowing/home";

  private final StowService stowService;
  private final EquipmentService equipmentService;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession()))
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);

    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
    val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
    val criteria = StowSearchCriteria.openStowsForUser(workstation.getWac());
    val stows = stowService.createAssignedListOfStowsForUser(
        criteria,
        user.getUserId(),
        workstation.getWac(),
        globalConstants.getTasksPerTrip().longValue());

    StowingListManager.assignStowsToUser(user.getUserId(), stows);

    request.setAttribute("stows", stows);
    request.setAttribute("stowSearchCriteria", new StowSearchCriteria());

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Stowing");
  }

  @PostMapping
  public SpaPostResponse submit(StowSearchCriteria stowSearchCriteria, HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession()))
      return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

    val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.STOWING_HOME);

    if (stowSearchCriteria.getSid().equalsIgnoreCase("")) {
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      response.addWarning("Please enter a SID.");
      return response;
    }

    try {
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());

      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val flag = stowService.updateStowScannedFlag(
          workstation.getWac(),
          stowSearchCriteria.getSid(),
          user.getUserId(),
          true);

      switch (flag) {
        case 3:
          log.warn(String.format("SID [%s] is assigned to a different WAC.", stowSearchCriteria.getSid()));
          response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
          response.addWarning("SID is assigned to a different WAC.");
          break;
        case 1:
          if (StowingListManager.getStowCountForUser(user.getUserId()) >= globalConstants.getTasksPerTrip()) {
            log.info(String.format("SID [%s] successfully added.", stowSearchCriteria.getSid()));
            log.warn(String.format("User[%d] has scanned in more than %d SIDs", user.getUserId(), globalConstants.getTasksPerTrip()));
            log.warn("Stows will be flagged in the DB, but not displayed to the user");
            response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
            response.addWarning(String.format("More than %d SIDS have been scanned in. Please process current stows before scanning more SIDs.", globalConstants.getTasksPerTrip()));
          }
          else {
            log.info(String.format("SID [%s] successfully added.", stowSearchCriteria.getSid()));
            response.setResult(SpaResponse.SpaResponseResult.SUCCESS);
          }
          break;
        case 0:
          log.warn(String.format("SID [%s] previously scanned in.", stowSearchCriteria.getSid()));
          response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
          response.addWarning("SID previously scanned in.");
          break;
        case -1:
          log.warn(String.format("SID [%s] part of rewarehouse, picks incomplete.", stowSearchCriteria.getSid()));
          response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
          response.addWarning("SID part of rewarehouse, picks incomplete.");
          break;
        case -2:
          log.warn(String.format("SID [%s] already assigned.", stowSearchCriteria.getSid()));
          response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
          response.addWarning("SID already assigned.");
          break;
        case -3:
          log.warn(String.format("Invalid SID [%s].", stowSearchCriteria.getSid()));
          response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
          response.addWarning("Invalid SID.");
          break;
        case -4:
          log.warn(String.format("SID [%s]in Exception Processing.", stowSearchCriteria.getSid()));
          response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
          response.addWarning("SID in Exception Processing.");
          break;
        default:
          log.warn(String.format("Unknown return flag for SID [%s].", stowSearchCriteria.getSid()));
          response.setResult(SpaResponse.SpaResponseResult.EXCEPTION);
          response.addWarning("Unable to process SID.");
      }
    }
    catch (Exception e) {
      log.error("Error occurred while processing SID", e);
      response.setResult(SpaResponse.SpaResponseResult.EXCEPTION);
      response.addWarning("Unable to process SID.");
    }
    return response;
  }
}
