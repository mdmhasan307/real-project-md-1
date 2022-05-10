package mil.usmc.mls2.stratis.modules.smv.api.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.datatype.ship.ShippingFloor;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.ShippingManifestService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.SHIPPING_PALLET_RELOCATION)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class ShippingPalletRelocationController {

  private final GlobalConstants globalConstants;

  private static final String DEFAULT_PAGE = MappingConstants.SHIPPING_PALLET_RELOCATION + "/detail";

  private final EquipmentService equipmentService;
  private final ShippingManifestService shippingManifestService;

  @GetMapping
  public SpaGetResponse showPalletRelocation(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shipping - Pallet Relocation");
  }

  @PostMapping("/handle")
  public SpaPostResponse processForPalletRelocation(ShippingPalletRelocationInput shippingPalletRelocationInput,
                                                    HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.SHIPPING_PALLET_RELOCATION);
      val service = SpringAdfBindingUtils.getShippingService();

      val shippingFloor = service.buildShippingFloor(shippingPalletRelocationInput.getOldLocation(), user.getWorkstationId(), true);
      val shippingFloorNew = service.buildShippingFloor(shippingPalletRelocationInput.getNewLocation(), user.getWorkstationId(), true);

      val locationsValid = validateLocation(shippingPalletRelocationInput, shippingFloor, shippingFloorNew, response);
      if (!locationsValid) {
        return response;
      }

      val manifestCriteria = ShippingManifestSearchCriteria.builder()
          .floorLocation(shippingPalletRelocationInput.getOldLocation())
          .equipmentNumber(workstation.getEquipmentNumber()).build();
      val pallets = shippingManifestService.search(manifestCriteria);

      val palletsValid = validatePallet(shippingPalletRelocationInput, pallets, shippingFloorNew, response);
      if (!palletsValid) {
        return response;
      }

      val shippingManifestId = pallets.get(0).getShippingManifestId().toString();
      val success = service.relocatePallet(shippingManifestId, String.valueOf(shippingFloorNew.getFloorLocationId()), user.getUserId());
      if (!success) {
        response.addWarning("An error occurred while relocating pallets from floor " + shippingPalletRelocationInput.getOldLocation());
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else {
        response.addNotification("Pallet relocated successfully");
        if (shippingFloor.isInUse() || shippingFloorNew.isInUse()) {
          StringBuilder manifests = new StringBuilder();
          manifests.append("List of Manifest(s) to Re-Print: ");
          if (shippingFloor.isInUse()) {
            manifests.append(shippingFloor.getContainer().getLdcon());
          }
          if (shippingFloorNew.isInUse()) {
            if (shippingFloor.isInUse() && shippingFloorNew.isInUse()) manifests.append(", ");
            manifests.append(shippingFloorNew.getContainer().getLdcon());
          }
          response.addNotification(manifests.toString());
        }
      }
      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Pallet Relocation submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  @PostMapping("/search")
  public ShippingPalletRelocationSearchResult processForPalletSearch(ShippingPalletRelocationInput shippingPalletRelocationInput,
                                                                     HttpServletRequest request) {
    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
    val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
    if (shippingPalletRelocationInput.getOldLocation().isEmpty()) {
      return ShippingPalletRelocationSearchResult.builder()
          .foundLocation(false).build();
    }
    val manifestCriteria = ShippingManifestSearchCriteria.builder()
        .floorLocation(shippingPalletRelocationInput.getOldLocation())
        .equipmentNumber(workstation.getEquipmentNumber())
        .build();
    val oldLocResults = shippingManifestService.search(manifestCriteria);

    if (oldLocResults.size() > 0) {
      val oldLoc = oldLocResults.get(0);
      return ShippingPalletRelocationSearchResult.builder()
          .aac(oldLoc.getCustomer().getAac())
          .floorLocation(oldLoc.getFloorLocation().getFloorLocation())
          .leadTcn(oldLoc.getLeadTcn())
          .foundLocation(true).build();
    }
    return ShippingPalletRelocationSearchResult.builder()
        .foundLocation(false).build();
  }

  private boolean validateLocation(ShippingPalletRelocationInput input, ShippingFloor shippingFloor, ShippingFloor shippingFloorNew, SpaPostResponse response) {

    val service = SpringAdfBindingUtils.getShippingService();

    if (input.getOldLocation().isEmpty()) {
      response.addWarning("Old location is required");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (input.getNewLocation().isEmpty()) {
      response.addWarning("New location is required");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (shippingFloor.getFloorLocationId() < 1) {
      if (Util.isEmpty(service.findFloorLocation(input.getOldLocation(), 0, false))) {
        response.addWarning("Old Floor location " + input.getOldLocation() + " is invalid");
      }
      else {
        response.addWarning("Old Floor location " + input.getOldLocation() + " (not in warehouse)");
      }
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (shippingFloorNew.getFloorLocationId() < 1) {
      if (Util.isEmpty(service.findFloorLocation(input.getNewLocation(), 0, false))) {
        response.addWarning("New Floor location " + input.getNewLocation() + " is invalid");
      }
      else {
        response.addWarning("New Floor location " + input.getNewLocation() + " (not in warehouse)");
      }
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    return true;
  }

  private boolean validatePallet(ShippingPalletRelocationInput input, List<ShippingManifest> pallets, ShippingFloor shippingFloorNew, SpaPostResponse response) {

    if (pallets.isEmpty()) {
      response.addWarning("Old Floor location " + input.getOldLocation() + " has no pallets to relocate");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    //* compare the customers
    Integer customerId = pallets.get(0).getCustomer().getCustomerId();
    if (shippingFloorNew.hasShippingContainers() && customerId != shippingFloorNew.getContainer().getCustomerId()) {
      response.addWarning("New location " + input.getNewLocation() + " currently in use for another AAC.");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    return true;
  }
}

