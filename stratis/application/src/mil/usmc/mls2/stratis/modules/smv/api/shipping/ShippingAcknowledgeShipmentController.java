package mil.usmc.mls2.stratis.modules.smv.api.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.ShippingManifestHistService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.SHIPPING_ACKNOWLEDGE_SHIPMENT)
@RequiredArgsConstructor
public class ShippingAcknowledgeShipmentController {

  private static final String DEFAULT_PAGE = MappingConstants.SHIPPING_ACKNOWLEDGE_SHIPMENT + "/detail";

  private final GlobalConstants globalConstants;
  private final ShippingManifestHistService shippingManifestHistService;

  @GetMapping
  public SpaGetResponse showAcknowledgeShipment(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shipping - Acknowledge Shipment");
  }

  @PostMapping("/handle")
  public SpaPostResponse processForAcknowledgeShipment(ShippingAcknowledgeShipmentInput shippingAcknowledgeShipmentInput,
                                                       HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.SHIPPING_ACKNOWLEDGE_SHIPMENT);

      val service = SpringAdfBindingUtils.getShippingService();

      String shippingManifestId = service.getShippingManifestIdForLDCON(shippingAcknowledgeShipmentInput.getManifestNumber());
      if (Util.isEmpty(shippingManifestId)) {
        //check history
        val historySearch = ShippingManifestHistSearchCriteria.builder()
            .manifest(shippingAcknowledgeShipmentInput.getManifestNumber())
            .checkPickedupOrDelivered(true).build();
        val historyRecords = shippingManifestHistService.count(historySearch);
        if (historyRecords.intValue() == 1) {
          response.addWarning("LDCON " + shippingAcknowledgeShipmentInput.getManifestNumber() + " already shipped");
        }
        else {
          response.addWarning("Invalid LDCON " + shippingAcknowledgeShipmentInput.getManifestNumber() + " - Verify manifested and printed");
        }
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }

      if (!shippingAcknowledgeShipmentInput.getDeliveryType().equalsIgnoreCase("P") && !shippingAcknowledgeShipmentInput.getDeliveryType().equalsIgnoreCase("D")) {
        response.addWarning("Invalid Acknowledgement (" + shippingAcknowledgeShipmentInput.getDeliveryType() + ") must be a pickup or delivery");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }

      if (Util.isEmpty(shippingAcknowledgeShipmentInput.getDriverName())) {
        response.addWarning("Driver is required");
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }

      boolean success = service.submitAcknowledgeShipment(shippingManifestId, shippingAcknowledgeShipmentInput.getDriverName(), user.getUserId(), shippingAcknowledgeShipmentInput.getDeliveryType());

      if (success) {
        response.addNotification("Shipment Successful for manifest " + shippingAcknowledgeShipmentInput.getManifestNumber());
      }
      else {
        response.addWarning("An error occurred while try to submit -  Delivery Acknowledgement for LDCON " + shippingAcknowledgeShipmentInput.getManifestNumber());
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      }
      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Acknowledge Shipment submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }
}

