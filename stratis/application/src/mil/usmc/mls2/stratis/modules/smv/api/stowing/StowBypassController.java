package mil.usmc.mls2.stratis.modules.smv.api.stowing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.StowingListManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.ErrorQueueService;
import mil.usmc.mls2.stratis.core.service.ErrorService;
import mil.usmc.mls2.stratis.core.service.StowService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.STOWING_BYPASS)
@RequiredArgsConstructor
public class StowBypassController {

  private static final String DEFAULT_PAGE = "mobile/stowing/bypass";

  private final StowService stowService;
  private final EquipmentService equipmentService;
  private final ErrorService errorService;
  private final ErrorQueueService errorQueueService;
  private final GlobalConstants globalConstants;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    List<Error> errors = errorService.search(ErrorSearchCriteria.builder().code("STOW_BYPASS*").build());
    request.setAttribute("errors", errors);

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Bypass Stow");
  }

  @PostMapping
  public SpaPostResponse post(StowBypassInput stowBypassInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.STOWING_BYPASS);

      if (null == stowBypassInput.getError()) {
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        response.addWarning("Please select a reason.");
        return response;
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val wac = workstation.getWac();
      val stow = StowingListManager.getNextStowForUser(user.getUserId());

      //stowRowByPass
      assert stow != null;
      stow.setStatus("STOW BYPASS2");
      stow.setBypassCount(2);
      stow.setModifiedBy(user.getUserId());
      stowService.update(stow);

      //createErrorQueueRecord run if insert was successful
      ErrorQueue newErrorQ = ErrorQueue.builder()
          .status("Open")
          .createdBy(user.getUserId())
          .createdDate(OffsetDateTime.now())
          .eid(stowBypassInput.getError())
          .keyType("SID")
          .keyNum(stow.getSid())
          .notes(null)
          .build();
      errorQueueService.save(newErrorQ);

      StowingListManager.removeStowFromUser(user.getUserId(), stow.getStowId());
      if (StowingListManager.hasNext(user.getUserId())) response.setRedirectUrl(MappingConstants.STOWING_DETAILS);
      else if (hasMoreScannedStows(user.getUserId(), wac)) response.setRedirectUrl(MappingConstants.STOWING_HOME);
      else response.setRedirectUrl(MappingConstants.SMV_HOME);
      response.addNotification("Stow successfully bypassed");
      return response;
    }
    catch (StratisRuntimeException sre) {
      log.error("Failed to retrieve stowing record");
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(sre));
    }
    catch (Exception e) {
      log.error("Error occurred processing stow bypass post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
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
}
