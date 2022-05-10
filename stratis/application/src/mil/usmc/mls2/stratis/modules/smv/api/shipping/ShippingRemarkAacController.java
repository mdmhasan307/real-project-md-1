package mil.usmc.mls2.stratis.modules.smv.api.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import mil.usmc.mls2.stratis.modules.smv.validation.MobileViewValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.SHIPPING_REMARK_AAC)
@RequiredArgsConstructor
public class ShippingRemarkAacController {

  private static final String DEFAULT_PAGE = MappingConstants.SHIPPING_REMARK_AAC + "/detail";

  private final GlobalConstants globalConstants;
  private final EquipmentService equipmentService;

  @GetMapping
  public SpaGetResponse showRemarkAac(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shipping - Remark AAC");
  }

  @PostMapping("/handle")
  public SpaPostResponse processForRemarkAAC(ShippingRemarkAacInput shippingRemarkAacInput,
                                             HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.SHIPPING_REMARK_AAC);
      val service = SpringAdfBindingUtils.getShippingService();

      val valid = validate(shippingRemarkAacInput, workstation.getWarehouseId(), response);
      if (!valid) {
        return response;
      }
      val customerId = service.getCustomerId(shippingRemarkAacInput.getRemarkAac());
      val ldconsToReprint = service.remarkShipment(shippingRemarkAacInput.getLeadTcn(), customerId, user.getUserId());
      response.addNotification("Successfully remarked shipment");
      if (ldconsToReprint != null && !ldconsToReprint.isEmpty()) {
        String ldConList = "";
        for (String ldcon : ldconsToReprint) {
          ldConList = Util.addCommasForList(ldConList, ldcon);
        }
        response.addNotification("List of Manifest(s) to Re-Print: " + ldConList);
      }
      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Remark AAC submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  private boolean validate(ShippingRemarkAacInput shippingRemarkAacInput, Integer warehouseId, SpaPostResponse response) {
    val service = SpringAdfBindingUtils.getShippingService();

    if (shippingRemarkAacInput.getLeadTcn().isEmpty()) {
      response.addWarning("Lead TCN is required");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }
    if (shippingRemarkAacInput.getRemarkAac().isEmpty()) {
      response.addWarning("AAC is required");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    String customerId = service.getCustomerId(shippingRemarkAacInput.getRemarkAac());
    if (Util.isEmpty(customerId)) {
      response.addWarning("Invalid AAC " + shippingRemarkAacInput.getRemarkAac());
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }
    else if (service.isCustomerRestricted(Integer.parseInt(customerId))) {
      response.addWarning(MobileViewValidator.getAACValidationErrorMessage(shippingRemarkAacInput.getRemarkAac()));
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    if (shippingRemarkAacInput.getLeadTcn() != null && shippingRemarkAacInput.getLeadTcn().length() != 17) {
      response.addWarning("Invalid Lead TCN " + shippingRemarkAacInput.getLeadTcn());
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }
    String remarkWarehouseId = service.validateRemark(shippingRemarkAacInput.getLeadTcn());
    if (Util.isEmpty(remarkWarehouseId)) {
      response.addWarning("Invalid Lead TCN " + shippingRemarkAacInput.getLeadTcn());
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }
    else if (!remarkWarehouseId.equals(warehouseId.toString())) {
      response.addWarning("Unable to remark- Lead TCN " + shippingRemarkAacInput.getLeadTcn() + " in another warehouse");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }
    return true;
  }
}

