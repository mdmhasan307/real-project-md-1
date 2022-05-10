package mil.usmc.mls2.stratis.modules.smv.api.picking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.PickingListManager;
import mil.usmc.mls2.stratis.core.processor.PickingProcessor;
import mil.usmc.mls2.stratis.core.service.ErrorService;
import mil.usmc.mls2.stratis.core.service.IssueService;
import mil.usmc.mls2.stratis.core.service.PickingService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.PICKING_BYPASS)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class PickBypassController {

  private final PickingProcessor pickingProcessor;
  private final IssueService issueService;
  private final PickingService pickingService;
  private final ErrorService errorService;
  private static final String DEFAULT_PAGE = "mobile/picking/bypass";
  private final GlobalConstants globalConstants;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }
      val user = (UserInfo) request.getSession().getAttribute("userbean");
      val userId = user.getUserId();
      val pickingDetailInput = (PickingDetailInput) request.getSession().getAttribute(globalConstants.getPickingDetailSessionAttrib());
      val pid = PickingListManager.getCurrentPickForUser(userId, pickingDetailInput.getPid());
      if (pid == null) {
        throw new StratisRuntimeException(("No PID found for processing"));
      }

      val pick = pickingService.findById(pid).orElseThrow(() -> new StratisRuntimeException(String.format("No Pick found for PID %s", pid)));

      //get the issue matching the pick, otherwise return an empty issue.
      val issue = issueService.findByScn(pick.getScn()).orElseThrow(() -> new StratisRuntimeException("No issue found for Pick"));

      val errorSearchCriteria = ErrorSearchCriteria.builder()
          .code("PICK_BYPASS*")
          .build();
      val reasons = errorService.search(errorSearchCriteria);

      request.setAttribute("pick", pick);
      request.setAttribute("issue", issue);
      request.setAttribute("reasons", reasons);
      request.setAttribute(globalConstants.getPickingDetailSessionAttrib(), pickingDetailInput);

      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Bypass Detail");
    }
    catch (Exception e) {
      log.error("Error occurred processing Pick Bypass", e);
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
      val userId = user.getUserId();
      val pid = PickingListManager.getCurrentPickForUser(userId, pickingDetailInput.getPid());
      if (pid == null) {
        throw new StratisRuntimeException(("No PID found for processing"));
      }
      val pick = pickingService.findById(pid).orElseThrow(() -> new StratisRuntimeException(String.format("No Pick found for PID %s", pid)));

      pickingProcessor.processPickBypass(pick, pickingDetailInput, userId);
      //remove picking detail input from session.
      request.getSession().removeAttribute(globalConstants.getPickingDetailSessionAttrib());

      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), null);

      pickingProcessor.processForNext(spaPostResponse, user);
      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Pick Bypass post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }
}