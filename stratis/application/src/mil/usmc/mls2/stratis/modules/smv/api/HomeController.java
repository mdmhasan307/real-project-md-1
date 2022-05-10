package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/mobile/home")
@RequiredArgsConstructor
public class HomeController {

  private static final String DEFAULT_PAGE = "mobile/home";

  private final EquipmentService equipmentService;
  private final StowService stowService;
  private final PickingService pickingService;
  private final InventoryItemService inventoryItemService;
  private final NiinLocationService niinLocationService;
  private final GlobalConstants globalConstants;
  private final DateService dateService;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }

      val user = (UserInfo) request.getSession().getAttribute("userbean");

      val workstation = equipmentService.findById(user.getWorkstationId()).orElseThrow(() -> new StratisRuntimeException("No Valid Workstation found"));
      val wac = workstation.getWac();
      //User might not be logged into a valid WAC.

      boolean receivingEnabled = (globalConstants.isReceivingEnabledBySystem() && user.isNotReceiving());
      boolean shippingEnabled = (globalConstants.isShippingEnabledBySystem() && user.isNotShipping());
      boolean stowEnabled = (globalConstants.isStowingEnabledBySystem() && user.isNotStowing());
      boolean pickEnabled = (globalConstants.isPickingEnabledBySystem() && user.isNotPicking());
      boolean inventoryEnabled = (globalConstants.isInventoryEnabledBySystem() || globalConstants.isLocationSurveyEnabledBySystem() || globalConstants.isShelfLifeEnabledBySystem())
          && user.isNotInventory();
      boolean printTestEnabled = globalConstants.isPrintTestEnabledBySystem();

      if (wac != null) {

        List<String> stowStatuses = new ArrayList<>();
        stowStatuses.add("STOW READY");
        stowStatuses.add("STOW BYPASS1");
        val stowCriteria = StowSearchCriteria.builder()
            .wacId(wac.getWacId())
            .statuses(stowStatuses)
            .build();
        val stowCount = stowService.count(stowCriteria);

        List<String> pickingStatuses = new ArrayList<>();
        pickingStatuses.add("PICK READY");
        pickingStatuses.add("PICK PARTIAL");
        pickingStatuses.add("PICK BYPASS1");
        val pickingCriteria = PickingSearchCriteria.builder()
            .wacId(wac.getWacId())
            .statuses(pickingStatuses)
            .build();
        val pickCount = pickingService.count(pickingCriteria);

        List<String> pendingInventoryStatuses = new ArrayList<>();
        pendingInventoryStatuses.add("INVENTORYPENDING");
        val pendingInventoryCriteria = InventoryItemSearchCriteria.builder()
            .wacId(wac.getWacId())
            .statuses(pendingInventoryStatuses)
            .build();
        val inventoryCount = inventoryItemService.count(pendingInventoryCriteria);

        List<String> locationSurveyPendingStatuses = new ArrayList<>();
        locationSurveyPendingStatuses.add("LOCSURVEYPENDING");
        val locationSurveyCriteria = InventoryItemSearchCriteria.builder()
            .wacId(wac.getWacId())
            .statuses(locationSurveyPendingStatuses)
            .build();
        val locationSurveyCount = inventoryItemService.count(locationSurveyCriteria);
        request.setAttribute("locationSurveyCount", locationSurveyCount);

        val niinLocationCriteria = NiinLocationSearchCriteria.builder()
            .wacId(wac.getWacId())
            .nsnRemark("Y")
            .build();

        val shelflifeCriteria = NiinLocationSearchCriteria.builder()
            .wacId(wac.getWacId())
            .expRemark("Y")
            .checkNumExtentsNull(true)
            .expirationDate(dateService.getLastDateOfThisMonth())
            .build();
        val shelfLifeCount = niinLocationService.count(shelflifeCriteria);

        stowEnabled = stowEnabled && stowCount > 0;
        pickEnabled = pickEnabled && pickCount > 0;
        inventoryEnabled = inventoryEnabled && (inventoryCount > 0 || locationSurveyCount > 0 || shelfLifeCount > 0);

        request.setAttribute("wac", wac.getWacNumber());
        request.setAttribute("stowCount", stowCount);
        request.setAttribute("pickCount", pickCount);
        request.setAttribute("inventoryCount", inventoryCount);
        request.setAttribute("niinUpdateCount", niinLocationService.count(niinLocationCriteria));
        request.setAttribute("shelfLifeCount", shelfLifeCount);
      }

      request.setAttribute("receivingEnabled", receivingEnabled);
      request.setAttribute("shippingEnabled", shippingEnabled);
      request.setAttribute("stowEnabled", stowEnabled);
      request.setAttribute("pickEnabled", pickEnabled);
      request.setAttribute("inventoryEnabled", inventoryEnabled);
      request.setAttribute("printTestEnabled", printTestEnabled);

      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Home");
    }
    catch (Exception e) {
      log.error("Error occurred processing Home show", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }
}
