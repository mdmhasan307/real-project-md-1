package mil.usmc.mls2.stratis.modules.smv.api.inventory.locationsurvey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyNiinsListManager;
import mil.usmc.mls2.stratis.core.processor.LocationSurveyProcessor;
import mil.usmc.mls2.stratis.core.service.InventoryItemService;
import mil.usmc.mls2.stratis.core.service.NiinInfoService;
import mil.usmc.mls2.stratis.core.service.NiinLocationService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.stream.Collectors;

import static mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyListManager.getCurrentLocationSurveyForUser;
import static mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyListManager.getNextLocationSurveyForUser;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.LOCATION_SURVEY)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class LocationSurveyController {

  private static final String DEFAULT_PAGE = "mobile/inventory/locationSurvey/locationSurvey";

  private final InventoryItemService inventoryItemService;
  private final NiinLocationService niinLocationService;
  private final NiinInfoService niinInfoService;
  private final LocationSurveyProcessor locationSurveyProcessor;
  private final GlobalConstants globalConstants;

  /**
   * Default handler. This is where user lands when Location Survey is selected from the Inventory menu.
   */
  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val inventoryItemId = getNextLocationSurveyForUser(user.getUserId());
      val inventoryItem = inventoryItemService.findById(inventoryItemId).orElse(null);
      val response = SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Location Survey");

      if (inventoryItem == null) {
        //this means we have processed every location survey (due to processing multiple niins on the same survey)
        response.addWarning("All Location Surveys processed.");
        response.setResponseBody(null);
        response.setResult(SpaResponse.SpaResponseResult.REDIRECT_HOME);
        return response;
      }

      val locationSurveyInput = LocationSurveyInput.builder()
          .inventoryItemId(inventoryItemId)
          .build();

      //load the niins for the location needing surveyed.
      val niinLocationCriteria = NiinLocationSearchCriteria.builder()
          .locationId(inventoryItem.getLocation().getLocationId())
          .build();

      val allNiinLocations = niinLocationService.search(niinLocationCriteria);

      request.setAttribute(globalConstants.getLocationSurveySessionAttrib(), locationSurveyInput);
      request.setAttribute("currentInventoryItem", inventoryItem);
      request.setAttribute("allNiinLocations", allNiinLocations);

      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Location Survey");
    }
    catch (Exception e) {
      log.error("Error occurred retrieving the Location Survey", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, e.getMessage());
    }
  }

  @PostMapping("/addNiin")
  public SpaPostResponse addNiin(LocationSurveyInput locationSurveyInput, HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val inventoryItemId = getCurrentLocationSurveyForUser(userId, locationSurveyInput.getInventoryItemId());
      val inventoryItem = inventoryItemService.findById(inventoryItemId)
          .orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));
      val response = SMVUtility.processPostResponse(request.getSession(), null);

      if (validate(locationSurveyInput, inventoryItem, response)) {
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }

      response.addFlag(locationSurveyInput.getNiin());

      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Location Survey submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  /**
   * Submit the survey results for the given niin set.
   */
  @PostMapping
  public SpaPostResponse submit(LocationSurveyInput locationSurveyInput, HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val inventoryItemId = getCurrentLocationSurveyForUser(userId, locationSurveyInput.getInventoryItemId());
      val inventoryItem = inventoryItemService.findById(inventoryItemId)
          .orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));
      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.LOCATION_SURVEY_DETAIL);

      if (CollectionUtils.isEmpty(locationSurveyInput.getScannedNiins())) {
        locationSurveyProcessor.failSurvey(inventoryItem.getLocation().getLocationId(), userId);
        return locationSurveyProcessor.processForNext(inventoryItem, null, userId, request, response);
      }

      val niinIds = locationSurveyInput.getScannedNiins()
          .stream()
          .map(niinInfoService::findByNiin)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(NiinInfo::getNiinId)
          .collect(Collectors.toList());

      InventoryItemLocationSurveyNiinsListManager.assignLocationSurveyNiinIdsToUser(userId, niinIds);
      request.getSession().setAttribute(globalConstants.getLocationSurveySessionAttrib(), locationSurveyInput);

      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Location Survey submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  /**
   * Bypass all niins in the location survey
   */
  @PostMapping(value = "/bypass")
  public SpaPostResponse postBypass(LocationSurveyInput locationSurveyInput, HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val inventoryItemId = getCurrentLocationSurveyForUser(userId, locationSurveyInput.getInventoryItemId());
      val inventoryItem = inventoryItemService.findById(inventoryItemId)
          .orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));
      val response = locationSurveyProcessor.bypassSurvey(inventoryItem.getLocation().getLocationId(), userId, request);

      response.addNotification(String.format("Location Survey for (%s) Bypassed", inventoryItem.getLocation().getLocationLabel()));

      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Location Survey bypass post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  private boolean validate(LocationSurveyInput input, InventoryItem inventoryItem, SpaPostResponse spaPostResponse) {

    boolean validationWarningsFound = false;

    if (input.getLocation().length() <= 8) {
      spaPostResponse.addWarning("Invalid Location Label.");
      validationWarningsFound = true;
    }
    else if (!input.getLocation().equals(inventoryItem.getLocation().getLocationLabel())) {
      spaPostResponse.addWarning("Location entry mismatch, Re-enter location or bypass pick.");
      validationWarningsFound = true;
    }

    val scannedNiins = input.getScannedNiins();
    if (CollectionUtils.isNotEmpty(scannedNiins) && scannedNiins.contains(input.getNiin())) {
      spaPostResponse.addWarning("NIIN previously scanned in.");
      validationWarningsFound = true;
    }
    else if (input.getNiin().length() != 9) {
      spaPostResponse.addWarning("Invalid NIIN, must be length 9.");
      validationWarningsFound = true;
    }
    else {
      val niinInfoSearchCriteria = NiinInfoSearchCriteria.builder()
          .niin(input.getNiin())
          .build();

      val niinInfos = niinInfoService.search(niinInfoSearchCriteria);
      if (CollectionUtils.isEmpty(niinInfos)) {
        spaPostResponse.addWarning("NIIN non-existent within STRATIS. See Supervisor.");
        validationWarningsFound = true;
      }
    }
    return validationWarningsFound;
  }
}

