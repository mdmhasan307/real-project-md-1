package mil.usmc.mls2.stratis.modules.smv.api.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.datatype.ship.ShippingFloor;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.ShippingManifestService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.SHIPPING_MANIFEST)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class ManifestController {

  private final GlobalConstants globalConstants;
  private static final String DEFAULT_PAGE = "mobile/shipping/manifest/manifest";

  private final EquipmentService equipmentService;
  private final ShippingManifestService shippingManifestService;

  @GetMapping
  public SpaGetResponse showManifestLocation(@RequestParam(required = false) String id, @RequestParam(required = false) String floor, HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }
    if (id == null) {
      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shipping Manifest");
    }
    try {
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val service = SpringAdfBindingUtils.getShippingService();

      List<String> ldcons = service.manifestWarehouseFloorForHandheld(id, user.getUserId());

      if (CollectionUtils.isNotEmpty(ldcons)) {
        request.setAttribute("ldCons", ldcons);
        request.setAttribute("floor", htmlEscape(floor));
      }

      else {
        val response = SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shipping Manifest");
        response.addWarning("No Manifest were generated for " + floor);
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shipping Manifest");
    }
    catch (Exception e) {
      log.error("Error occurred processing Manifest table get request", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping
  public SpaPostResponse processManifestLocation(ManifestControllerInput manifestControllerInput,
                                                 HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.SHIPPING_MANIFEST);

      val service = SpringAdfBindingUtils.getShippingService();

      if (manifestControllerInput.getFloorLocation().length() > 5) {
        response.addWarning("Floor location " + manifestControllerInput.getFloorLocation() + " must be 5 alphanumeric characters or less.");
        return response;
      }

      ShippingFloor shippingFloor = service.buildShippingFloor(manifestControllerInput.getFloorLocation(), user.getWorkstationId(), true);
      if (shippingFloor.getFloorLocationId() < 1) {
        int anotherFloorManifest = service.findAnotherFloorManifest(manifestControllerInput.getFloorLocation());
        if (anotherFloorManifest > 0) {
          response.addWarning("No floor " + manifestControllerInput.getFloorLocation() + "  to Manifest in this building- Relogin to warehouse building " + service.getBuildingName(anotherFloorManifest));
        }
        else {
          response.addWarning("Invalid floor location " + manifestControllerInput.getFloorLocation() + " entered- Verify building.");
        }
        return response;
      }
      if (shippingFloor.isInUse()) {
        response.addWarning("Floor location " + manifestControllerInput.getFloorLocation() + " already manifested.");
        return response;
      }

      val shipCriteria = ShippingManifestSearchCriteria.builder()
          .manifestDateNull(true)
          .equipmentNumber(workstation.getEquipmentNumber())
          .floorLocation(manifestControllerInput.getFloorLocation()).build();
      if (shippingManifestService.count(shipCriteria) == 0) {
        response.addWarning("Floor location " + manifestControllerInput.getFloorLocation() + " not available for manifestation.");
        return response;
      }

      response.setRedirectUrl(MappingConstants.SHIPPING_MANIFEST + "?id=" + shippingFloor.getFloorLocationId() + "&floor=" + manifestControllerInput.getFloorLocation());
      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Manifest submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }
}
