package mil.usmc.mls2.stratis.modules.smv.api.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.datatype.ship.ShippingBarcode;
import mil.stratis.model.datatype.ship.ShippingFloor;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.SHIPPING_ADD_CONTAINER)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class AddContainerController {

  private final GlobalConstants globalConstants;
  private static final String DEFAULT_PAGE = "mobile/shipping/addcontainer/single";

  private final EquipmentService equipmentService;

  @GetMapping
  public SpaGetResponse showAddContainer(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Add Container");
  }

  /**
   * The main processor method. As a placeholder, the default template has a drop-down with "single" or "multi". Depending
   * on which is selected, this method will dispatch to one form or the other.
   */
  @PostMapping
  public SpaPostResponse process(AddContainerInput addContainerInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val response = SMVUtility.processPostResponse(request.getSession(), "mobile/shipping/addcontainer");

      val service = SpringAdfBindingUtils.getShippingService();

      //check barcode
      ShippingBarcode shippingBarcode = service.buildShippingBarcode(addContainerInput.getBarcode());
      ShippingFloor warehouseShippingFloor = service.buildShippingFloor(addContainerInput.getStageLoc(), user.getWorkstationId(), true);
      ShippingFloor allShippingFloor = service.buildShippingFloor(addContainerInput.getStageLoc(), user.getWorkstationId(), false); //to check if the location is elsewhere

      boolean barValid = validateBarcode(shippingBarcode, addContainerInput.getBarcode(), workstation.getWarehouseId(), response);
      boolean locValid = false;
      if (!addContainerInput.getStageLoc().isEmpty()) {
        locValid = validateLocation(shippingBarcode, warehouseShippingFloor, allShippingFloor, addContainerInput.getStageLoc(), user.getWorkstationId(), workstation.getWarehouseId(), response);
      }

      if (barValid && locValid) {
        //save
        int result = service.assignMaterial(shippingBarcode, warehouseShippingFloor, user.getUserId(), user.getWorkstationId());
        if (result <= 0) {
          log.error("Error occurred saving Add Container submit post");
          return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, "Unable to add container " + shippingBarcode.getBarcode());
        }
        else {
          response.addNotification("Successfully added container");
        }
      }
      else if (!locValid && barValid && addContainerInput.getRecommendedLocation().isEmpty()) {
        //recommend location
        response.setRedirectUrl(null);
        response.addFlag("recommend");
      }
      else if (!addContainerInput.getRecommendedLocation().isEmpty() && addContainerInput.getStageLoc().isEmpty()) {
        response.addWarning("Please enter a location");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      }

      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Add Container submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  @PostMapping("/recommend")
  public RecommendLocationResponse recommendLocation(AddContainerInput addContainerInput, HttpServletRequest request) {
    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());

    val service = SpringAdfBindingUtils.getShippingService();

    //check barcode
    val shippingBarcode = service.buildShippingBarcode(addContainerInput.getBarcode());
    Map<Integer, String> hm = service.getBestFloorLocations(shippingBarcode.getCustomerId(), user.getWorkstationId());
    String bestLocation = "No Best Stage Loc Found";
    if (!hm.isEmpty() && hm.get(1) != null) {
      bestLocation = hm.get(1);
    }
    return new RecommendLocationResponse(bestLocation);
  }

  private boolean validateBarcode(ShippingBarcode shippingBarcode, String barcode, int warehouseId, SpaPostResponse response) {
    val service = SpringAdfBindingUtils.getShippingService();
    if (!shippingBarcode.hasShippingItems()) {
      //* display ERROR - UNKNOWN CONTAINER
      response.addWarning("Unknown container " + barcode);
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    int packedWarehouseId = service.packedInWarehouseHH(shippingBarcode.getPackingConsolidationId());
    String warehouse = service.getBuildingName(packedWarehouseId);
    if (shippingBarcode.isAssignedToAnotherWarehouse(warehouseId)) {
      response.addWarning("Container already assigned in another warehouse building " + warehouse);
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (service.isNotAllowedToShipHH(warehouseId, packedWarehouseId)) {
      response.addWarning("Container in another warehouse- Relogin to warehouse building " + warehouse);
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (shippingBarcode.isAlreadyAssigned()) {
      response.addWarning("Container already assigned " + barcode);
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }
    return true;
  }

  private boolean validateLocation(ShippingBarcode shippingBarcode, ShippingFloor warehouseShippingFloor, ShippingFloor allShippingFloor, String floorLocation, int workstationId, int warehouseId, SpaPostResponse response) {
    val service = SpringAdfBindingUtils.getShippingService();

    if (floorLocation.length() > 5) {
      response.addWarning("Stage location " + floorLocation + " must be 5 alphanumeric characters or less.");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (warehouseShippingFloor.getFloorLocationId() < 1 && allShippingFloor.getFloorLocationId() < 1) {
      response.addWarning("Location " + floorLocation + " does not exist.");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (warehouseShippingFloor.isInUse()) {
      response.addWarning("Stage Location " + floorLocation + " in use by manifestation.");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (warehouseShippingFloor.getWarehouseId() != warehouseId && allShippingFloor.getWarehouseId() != warehouseId) {
      response.addWarning("Stage Location " + floorLocation + " is not in this warehouse.");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (warehouseShippingFloor.hasShippingContainers() && warehouseShippingFloor.getContainer().getCustomerId() != shippingBarcode.getCustomerId()) {
      response.addWarning("Location " + floorLocation + " already in use -by another customer");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    String customerCurrFloor = service.getCustomerCurrentFloorLocation(shippingBarcode.getCustomerId(), workstationId);
    if (!customerCurrFloor.isEmpty() && !customerCurrFloor.equals(warehouseShippingFloor.getFloorLocation())) {
      response.addWarning("AAC already assigned to floor location " + customerCurrFloor + "- Add container to current floor location.");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    return true;
  }
}

