package mil.usmc.mls2.stratis.modules.smv.api.picking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.PickingListManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.PickingService;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/mobile/picking/byNormal")
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class PickByNormalController {

  private final EquipmentService equipmentService;
  private final PickingService pickingService;

  @PostMapping
  public SpaPostResponse post(HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), MappingConstants.PICKING_DETAIL);
      val user = (UserInfo) request.getSession().getAttribute("userbean");

      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val wac = workstation.getWac();

      val pickingSearchCriteria = new PickingSearchCriteria();
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
      log.error("Error occurred processing Pick By Normal post", e);
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
