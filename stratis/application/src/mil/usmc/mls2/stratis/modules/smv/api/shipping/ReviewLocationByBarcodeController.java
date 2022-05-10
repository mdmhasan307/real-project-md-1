package mil.usmc.mls2.stratis.modules.smv.api.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
@RequestMapping(value = MappingConstants.SHIPPING_REVIEW_LOCATION_BY_BARCODE)
@RequiredArgsConstructor
public class ReviewLocationByBarcodeController {

  private final GlobalConstants globalConstants;
  private static final String DEFAULT_PAGE = "mobile/shipping/reviewlocation/barcode";

  private final EquipmentService equipmentService;

  @GetMapping
  public SpaGetResponse showReviewLocation(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Review Location By Barcode");
  }

  /**
   * The main processor method. As a placeholder, the default template has a drop-down with "single" or "multi". Depending
   * on which is selected, this method will dispatch to one form or the other.
   */
  @PostMapping("/handle")
  public SpaPostResponse processForReviewLocation(ReviewLocationInput reviewLocationInput,
                                                  HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val response = SMVUtility.processPostResponse(request.getSession(), DEFAULT_PAGE);
      val service = SpringAdfBindingUtils.getShippingService();

      if (reviewLocationInput.getBarcode().isEmpty()) {
        //* display ERROR - Empty Barcode
        response.addWarning("Barcode is required");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }

      if (reviewLocationInput.getLocation().isEmpty()) {
        //* display ERROR - Empty Barcode
        response.addWarning("Location is required");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }

      if (reviewLocationInput.getLocation().length() > 5) {
        //* display ERROR - Empty Barcode
        response.addWarning("Location to review " + reviewLocationInput.getLocation() + " must be 5 alphanumeric characters or less.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }

      Map<String, String> hm = service.fillAllBarcodeReviewHH(reviewLocationInput.getLocation(), reviewLocationInput.getBarcode(), user.getUserId(), user.getWorkstationId(), workstation.getWarehouseId());

      String message = hm.get("message");
      if (message.equals("-1")) {
        response.addWarning("Unknown, location not found.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.equals("-2")) {
        response.addWarning("Unknown, container not found.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.equals("-3")) {
        response.addWarning("Location does not exist in warehouse.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.equals("-4")) {
        response.addWarning("Barcode is assigned to another warehouse.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.equals("-5")) {
        response.addWarning("Unprocessed, barcode's customer currently assigned.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.equals("-6")) {
        response.addWarning("Unprocessed, customer conflict.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.equals("-7")) {
        response.addWarning("Unprocessed, floor already in use by manifestation.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.contains("-8")) {
        String packedAnotherWarehouse = message.split(",")[1];
        response.addWarning("Unable to review- packed in another warehouse building " + packedAnotherWarehouse + "- Relogin to warehouse ");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.equals("-999")) {
        response.addWarning("Unable to review.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.equals("0")) {
        response.addWarning("Already picked up or delivered.");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else if (message.equals("1")) {
        response.addWarning("CONFLICT " + "Current AAC " + hm.get("ScnAac") + ",- Expected " + hm.get("Aac")); //also Scn, ShippingId
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        response.addFlag("overrideAAC");
        response.addFlag("aac:" + hm.get("Aac"));
        response.addFlag("scn:" + hm.get("Scn"));
        response.addFlag("shippingId:" + hm.get("ShippingId"));
        return response;
      }
      else if (message.equals("3")) {
        response.addNotification("Added to location.");
        return response;
      }
      else if (message.equals("4")) {
        response.addWarning("Already included at location");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      else {
        response.addWarning("CONFLICT " + "At Location " + hm.get("FloorLocation") + ", Expected " + message); //ShippingManifestId, Aac, ShippingId,
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        response.addFlag("overrideLoc");
        response.addFlag("aac:" + hm.get("Aac"));
        response.addFlag("shippingManifestId:" + hm.get("ShippingManifestId"));
        response.addFlag("shippingId:" + hm.get("ShippingId"));
        response.addFlag("floorLocation:" + hm.get("message")); //in stratis the message is the floor location that was used by the handheld code for location overrides
        //see HandHeldAMImpl line 9446
        return response;
      }
    }
    catch (Exception e) {
      log.error("Error occurred processing Add Container submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  @PostMapping("/override")
  public SpaPostResponse processForOverride(ReviewLocationInput reviewLocationInput,
                                            HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val response = SMVUtility.processPostResponse(request.getSession(), DEFAULT_PAGE);
      val service = SpringAdfBindingUtils.getShippingService();

      if (reviewLocationInput.getOverrideCommand().equalsIgnoreCase("overrideAAC")) {
        //override AAC
        int result = service.overrideAAC(reviewLocationInput.getAac(), reviewLocationInput.getScn(), null, user.getUserId());
        if (result > 0) {
          response.addNotification("Successfully updated AAC.");
        }
        else {
          response.addWarning("Override AAC failed.");
          response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        }
      }
      else if (reviewLocationInput.getOverrideCommand().equalsIgnoreCase("dontOverrideAAC")) {
        //override Loc
        service.donotOverrideAAC(reviewLocationInput.getShippingId(), user.getUserId());
      }
      else if (reviewLocationInput.getOverrideCommand().equalsIgnoreCase("overrideLoc")) {
        //override Loc
        int result = service.overrideLocation(reviewLocationInput.getFloorLocation(), reviewLocationInput.getAac(), "", reviewLocationInput.getShippingManifestId(), reviewLocationInput.getShippingId(), user.getUserId(), user.getWorkstationId());
        if (result > 0) {
          response.addNotification("Successfully updated location.");
        }
        else {
          switch (result) {
            case -2:
              response.addWarning("Unable to Override Location - Customer conflict on floor.");
              response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
              break;
            case -3:
              response.addWarning("Unable to Override Location - Already Manifested.");
              response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
              break;
            default:
              response.addWarning("Override location failed.");
              response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
              break;
          }
        }
      }
      else if (reviewLocationInput.getOverrideCommand().equalsIgnoreCase("dontOverrideLoc")) {
        //override Loc
        service.donotOverrideLocation(reviewLocationInput.getShippingId(), user.getUserId());
      }
      response.addFlag("resetOverride");
      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Add Container submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }
}

