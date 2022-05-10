package mil.usmc.mls2.stratis.modules.smv.api.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.datatype.ship.ShippingFloor;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.ShippingService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.SHIPPING_REVIEW_LOCATION_BY_CONTAINER)
@RequiredArgsConstructor
public class ReviewLocationByContainerController {

  private final GlobalConstants globalConstants;
  private static final String DEFAULT_PAGE = "mobile/shipping/reviewlocation/container";

  private final ShippingService shippingService;

  @GetMapping
  public SpaGetResponse showReviewLocation(@RequestParam(required = false) Integer id, @RequestParam(required = false) String floor, HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }
    if (id == null) {
      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Review Location By Container");
    }
    try {
      val criteria = ShippingSearchCriteria.builder().floorLocationId(id).build();
      val results = shippingService.search(criteria);
      List<String> tableResults = new ArrayList<>();

      if (results.size() > 0) {
        for (Shipping s : results) {
          tableResults.add(s.getPackingConsolidation().getConsolidationBarcode());
        }
        request.setAttribute("containers", tableResults);
        request.setAttribute("floor", htmlEscape(floor));
      }
      else {
        val response = SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shipping Manifest");
        response.addWarning("No barcodes to review at " + floor);
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }
      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shipping Manifest");
    }
    catch (Exception e) {
      log.error("Error occurred processing review container table get request", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
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
      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.SHIPPING_REVIEW_LOCATION_BY_CONTAINER);
      val service = SpringAdfBindingUtils.getShippingService();

      if (reviewLocationInput.getLocation().length() > 5) {
        //* display ERROR - Empty Barcode
        response.addWarning("Location to review " + reviewLocationInput.getLocation() + " must be 5 alphanumeric characters or less.");
        return response;
      }

      ShippingFloor shippingFloor = service.buildShippingFloor(reviewLocationInput.getLocation(), user.getWorkstationId(), true);
      if (shippingFloor.getFloorLocationId() < 1) {
        response.addWarning("Location " + reviewLocationInput.getLocation() + " does not exist (in this warehouse).");
        return response;
      }

      val criteria = ShippingSearchCriteria.builder().floorLocationId(shippingFloor.getFloorLocationId()).build();
      val count = shippingService.count(criteria);

      if (count > 0) {
        //get barcodes
        response.setRedirectUrl(MappingConstants.SHIPPING_REVIEW_LOCATION_BY_CONTAINER + "?id=" + shippingFloor.getFloorLocationId() + "&floor=" + reviewLocationInput.getLocation());
      }
      else {
        response.addWarning("No barcodes to review at " + reviewLocationInput.getLocation());
      }

      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing review container submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }
}
