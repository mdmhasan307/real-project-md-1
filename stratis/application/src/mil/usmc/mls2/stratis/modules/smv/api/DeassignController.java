package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Equipment;
import mil.usmc.mls2.stratis.core.domain.model.SpaPostResponse;
import mil.usmc.mls2.stratis.core.domain.model.SpaResponse;
import mil.usmc.mls2.stratis.core.domain.model.Wac;
import mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyListManager;
import mil.usmc.mls2.stratis.core.manager.InventoryItemsListManager;
import mil.usmc.mls2.stratis.core.manager.PickingListManager;
import mil.usmc.mls2.stratis.core.manager.StowingListManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.InventoryItemService;
import mil.usmc.mls2.stratis.core.service.PickingService;
import mil.usmc.mls2.stratis.core.service.StowService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = "/mobile/deassign")
@RequiredArgsConstructor
public class DeassignController {

  private final EquipmentService equipmentService;
  private final StowService stowService;
  private final PickingService pickingService;
  private final InventoryItemService inventoryItemService;
  private final GlobalConstants globalConstants;

  @PostMapping("/{button}/{type}")
  public SpaPostResponse deassign(@PathVariable String button, @PathVariable String type, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());

      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val wac = workstation.getWac();

      String responsePath;
      if ("main".equals(button)) {
        responsePath = findResponsePath(type);
      }
      else {
        responsePath = MappingConstants.SMV_HOME;
      }

      //shipping and receiving may not have a wac. and nothing needs deassigned for shipping or receiving anyway.
      if (wac != null) {
        deassignWac(wac, type, user.getUserId());
      }
      if ("receiving".equals(type)) {
        request.getSession().removeAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());
      }
      return SMVUtility.processPostResponse(request.getSession(), responsePath);
    }
    catch (Exception e) {
      log.error("Error returning to main menu.", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  private String findResponsePath(String type) {
    if ("picking".equals(type)) {
      return MappingConstants.PICKING_HOME;
    }
    else if ("stowing".equals(type)) {
      return MappingConstants.STOWING_HOME;
    }
    else if ("inventory".equals(type)) {
      return MappingConstants.INVENTORY_HOME;
    }
    else if ("locationSurvey".equals(type)) {
      return MappingConstants.INVENTORY_HOME;
    }
    else if ("shipping".equals(type)) {
      return MappingConstants.SHIPPING_HOME;
    }
    else if ("receiving".equals(type)) {
      return MappingConstants.RECEIVING_HOME;
    }
    else {
      throw new StratisRuntimeException(String.format("Invalid Type: %s", type));
    }
  }

  private void deassignWac(Wac wac, String type, int userId) {
    //only need to deassign via db for non mechanized.  but will always remove from the managers.
    val nonMechanizedWac = "N".equals(wac.getMechanizedFlag());

    if ("picking".equals(type)) {
      if (nonMechanizedWac) pickingService.deAssignAllPicksForUser(userId);
      PickingListManager.removeAllPicksFromUser(userId);
    }
    else if ("stowing".equals(type)) {
      if (nonMechanizedWac) stowService.deAssignAllStowsForUser(userId);
      StowingListManager.removeAllStowsFromUser(userId);
    }
    else if ("inventory".equals(type)) {
      if (nonMechanizedWac) inventoryItemService.deAssignAllInventoryItemsForUser(userId);
      InventoryItemsListManager.removeAllInventoryFromUser(userId);
    }
    else if ("locationSurvey".equals(type)) {
      if (nonMechanizedWac) inventoryItemService.deAssignAllInventoryItemsForUser(userId);
      InventoryItemLocationSurveyListManager.removeAllLocationSurveyFromUser(userId);
    }
  }
}
