package mil.usmc.mls2.stratis.modules.smv.api.picking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.PickingListManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.IssueService;
import mil.usmc.mls2.stratis.core.service.PickingService;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/mobile/picking/byPriority")
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class PickByPriorityController {

  private static final String DEFAULT_PAGE = "mobile/picking/pickByPriority";
  private static final String PAGE_TITLE = "Picking - By Priority";

  private final EquipmentService equipmentService;
  private final PickingService pickingService;
  private final IssueService issueService;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    try {

      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }

      val user = (UserInfo) request.getSession().getAttribute("userbean");

      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val wac = workstation.getWac();

      val pickingSearchCriteria = new PickingSearchCriteria();
      populateBaseSearchCriteria(user, wac, pickingSearchCriteria);

      request.setAttribute("pickingCriteria", pickingSearchCriteria);

      val pickings = pickingService.search(pickingSearchCriteria);
      if (CollectionUtils.isEmpty(pickings)) {
        val spaGetResponse = SMVUtility.processGetResponse(request, DEFAULT_PAGE, PAGE_TITLE);
        spaGetResponse.addWarning("No Picks available for processing, or all picks assigned to other users.");
        spaGetResponse.setResult(SpaResponse.SpaResponseResult.REDIRECT_HOME);
        return spaGetResponse;
      }

      val issueCriteria = IssueSearchCriteria.builder()
          .scns(pickings.stream().map(Picking::getScn).collect(Collectors.toList()))
          .build();

      val issues = issueService.search(issueCriteria);

      val priorityGroups = issues.stream().map(Issue::getIssuePriorityGroup).distinct().collect(Collectors.toList());
      List<KeyValue<String, String>> priorities = new ArrayList<>();
      priorityGroups.forEach(x -> {
        switch (x) {
          case "1":
            priorities.add(new DefaultKeyValue<>(x, "Group 1 - Due 36 Hours"));
            break;
          case "2":
            priorities.add(new DefaultKeyValue<>(x, "Group 2 - Due 72 Hours"));
            break;
          case "3":
            priorities.add(new DefaultKeyValue<>(x, "Group 3 - Due 10 days"));
            break;
          default:
            break;
        }
      });

      if (CollectionUtils.isEmpty(priorities)) {
        val spaGetResponse = SMVUtility.processGetResponse(request, DEFAULT_PAGE, PAGE_TITLE);
        spaGetResponse.addWarning("No Priorities available.");
        spaGetResponse.setResponseBody(null);
        return spaGetResponse;
      }
      request.setAttribute("priorities", priorities);

      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, PAGE_TITLE);
    }
    catch (Exception e) {
      log.error("Error occurred processing PickByPriority show", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping
  public SpaPostResponse post(PickingSearchCriteria pickingSearchCriteria, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), MappingConstants.PICKING_DETAIL);
      val user = (UserInfo) request.getSession().getAttribute("userbean");

      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val wac = workstation.getWac();
      populateBaseSearchCriteria(user, wac, pickingSearchCriteria);

      val pickings = pickingService.getPickingListForProcessing(pickingSearchCriteria, wac);
      if (CollectionUtils.isEmpty(pickings)) {
        spaPostResponse.addNotification("No Picks available for processing, or all picks assigned to other users.");
        spaPostResponse.setResult(SpaResponse.SpaResponseResult.REDIRECT_HOME);
        return spaPostResponse;
      }
      PickingListManager.assignPicksToUser(user.getUserId(), pickings.stream().map(Picking::getPid).collect(Collectors.toList()));

      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Pick By Priority post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  private void populateBaseSearchCriteria(UserInfo user, Wac wac, PickingSearchCriteria pickingSearchCriteria) {
    List<String> pickingStatuses = new ArrayList<>();
    pickingStatuses.add("PICK READY");
    pickingStatuses.add("PICK PARTIAL");
    pickingStatuses.add("PICK BYPASS1");

    pickingSearchCriteria.setAssignedUserId(user.getUserId());
    pickingSearchCriteria.setWacId(wac.getWacId());
    pickingSearchCriteria.setStatuses(pickingStatuses);
    pickingSearchCriteria.setAllowNullAssignedUserIds(true);
  }
}
