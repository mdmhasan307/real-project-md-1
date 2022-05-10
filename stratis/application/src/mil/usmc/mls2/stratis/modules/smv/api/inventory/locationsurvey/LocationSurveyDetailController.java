package mil.usmc.mls2.stratis.modules.smv.api.inventory.locationsurvey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.DefaultTransactionExecutor;
import mil.usmc.mls2.stratis.core.processor.LocationSurveyProcessor;
import mil.usmc.mls2.stratis.core.service.InventoryItemService;
import mil.usmc.mls2.stratis.core.service.NiinInfoService;
import mil.usmc.mls2.stratis.core.service.NiinLocationService;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;

import static mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyListManager.*;
import static mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyNiinsListManager.getCurrentLocationSurveyNiinsForUser;
import static mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyNiinsListManager.getNextLocationSurveyNiinIdForUser;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.LOCATION_SURVEY_DETAIL)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class LocationSurveyDetailController {

  private static final String DEFAULT_PAGE = "mobile/inventory/locationSurvey/detailSurvey";

  private final InventoryItemService inventoryItemService;
  private final NiinLocationService niinLocationService;
  private final LocationSurveyProcessor locationSurveyProcessor;
  private final NiinInfoService niinInfoService;
  private final DefaultTransactionExecutor transactionExecutor;
  private final GlobalConstants globalConstants;
  private final DateService dateService;

  /**
   * Default handler. This is where user lands when processing the niins input on the main location survey screen.
   */
  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);

      val locationSurveyInput = (LocationSurveyInput) request.getSession().getAttribute(globalConstants.getLocationSurveySessionAttrib());
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();

      val inventoryItemId = getCurrentLocationSurveyForUser(userId, locationSurveyInput.getInventoryItemId());
      val inventoryItem = inventoryItemService.findById(inventoryItemId)
          .orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));
      val niinId = getNextLocationSurveyNiinIdForUser(userId);

      //load the niinInfo for the niin being processed.
      val niinInfo = niinInfoService.findById(niinId)
          .orElseThrow(() -> new StratisRuntimeException(String.format("No Niin Information found for NIIN_ID %s", niinId)));

      //check if there is currently a niinLocation record for the niin at this location.
      val niinLocationCriteria = NiinLocationSearchCriteria.builder()
          .locationId(inventoryItem.getLocation().getLocationId())
          .niinId(niinId)
          .build();

      val niinLocations = niinLocationService.search(niinLocationCriteria);
      val niinLocationInput = new NiinLocationInput();
      niinLocationInput.setInventoryItemId(inventoryItemId);
      niinLocationInput.setNiinId(niinId);

      if (CollectionUtils.isNotEmpty(niinLocations)) {
        val currentNiinLocation = niinLocations.get(0);
        niinLocationInput.setNiinLocationId(currentNiinLocation.getNiinLocationId());
        niinLocationInput.setCc(currentNiinLocation.getCc());
        niinLocationInput.setExpirationDate(currentNiinLocation.getExpirationDate());
      }

      val conditionCodes = Arrays.asList(
          new DefaultKeyValue<>("A", "Serviceable"),
          new DefaultKeyValue<>("F", "Unserviceable"));

      request.setAttribute("conditionCodes", conditionCodes);
      request.setAttribute("niinLocationInput", niinLocationInput);
      request.setAttribute("location", inventoryItem.getLocation());
      request.setAttribute("niinInfo", niinInfo);

      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Location Survey Details");
    }
    catch (Exception e) {
      log.error("Error occurred retrieving the Location Survey Details", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping
  public SpaPostResponse submit(NiinLocationInput niinLocationInput, HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val inventoryItemId = niinLocationInput.getInventoryItemId();
      val inventoryItem = inventoryItemService.findById(inventoryItemId)
          .orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));
      val niinId = getCurrentLocationSurveyNiinsForUser(userId, niinLocationInput.getNiinId());
      val hasNiinLocation = null != niinLocationInput.getNiinLocationId();
      val response = SMVUtility.processPostResponse(request.getSession(), null);

      // Determine if we have a new niin to add or an existing niin to complete
      if (!hasNiinLocation) handleAdd(inventoryItem, niinId, niinLocationInput, userId);
      else handleComplete(inventoryItem, niinId, userId);

      return locationSurveyProcessor.processForNext(inventoryItem, niinId, userId, request, response);
    }
    catch (Exception e) {
      log.error("Error occurred processing Shelf Life submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping(value = "/remove")
  public SpaPostResponse remove(NiinLocationInput niinLocationInput, HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val inventoryItemId = niinLocationInput.getInventoryItemId();
      val inventoryItem = inventoryItemService.findById(inventoryItemId)
          .orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));
      val niinId = getCurrentLocationSurveyNiinsForUser(userId, niinLocationInput.getNiinId());
      val niinLocationId = niinLocationInput.getNiinLocationId();
      val response = SMVUtility.processPostResponse(request.getSession(), null);

      val inventoryItemToUpdate = inventoryItemService.search(InventoryItemSearchCriteria.builder()
          .locationId(inventoryItem.getLocation().getLocationId())
          .niinLocationId(niinLocationId)
          .niinId(niinId)
          .statuses(Collections.singletonList("LOCSURVEYPENDING"))
          .build())
          .stream()
          .findFirst()
          .orElseThrow(() -> new StratisRuntimeException(String.format("Unable to retrieve inventory item record for niinId %d", niinId.intValue())));

      inventoryItemToUpdate.niinNotFound(userId);
      executeInTransaction("Inventory item", () -> inventoryItemService.save(inventoryItemToUpdate));

      return locationSurveyProcessor.processForNext(inventoryItem, niinLocationInput.getNiinId(), userId, request, response);
    }
    catch (Exception e) {
      log.error("Error occurred processing Shelf Life submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * Bypass all niins in the location survey
   */
  @PostMapping(value = "/bypass")
  public SpaPostResponse postBypass(HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val inventoryItemId = getNextLocationSurveyForUser(userId);
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

  /**
   * Fail the current survey and unassign all location surveys from the user
   */
  @PostMapping(value = "/exit")
  public SpaPostResponse postExit(HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();
      val inventoryItemId = getNextLocationSurveyForUser(userId);
      val inventoryItem = inventoryItemService.findById(inventoryItemId)
          .orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));

      locationSurveyProcessor.failSurvey(inventoryItem.getLocation().getLocationId(), userId);
      inventoryItemService.deAssignAllInventoryItemsForUser(userId);
      removeAllLocationSurveyFromUser(userId);

      return SMVUtility.processPostResponse(request.getSession(), MappingConstants.SMV_HOME);
    }
    catch (Exception e) {
      log.error("Error occurred processing Location Survey bypass post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  private void handleComplete(InventoryItem inventoryItem, Integer niinId, Integer userId) {

    val criteria = NiinLocationSearchCriteria.builder()
        .locationId(inventoryItem.getLocation().getLocationId())
        .niinId(niinId)
        .build();

    val niinLocation = niinLocationService.search(criteria)
        .stream()
        .findFirst()
        .orElseThrow(() -> new StratisRuntimeException("Unable to find location data for NIIN."));

    niinLocation.surveyUpdateNiinLocation(userId);
    executeInTransaction("Niin location", () -> niinLocationService.save(niinLocation));

    val inventoryItemToUpdate = inventoryItemService.search(InventoryItemSearchCriteria.builder()
        .locationId(inventoryItem.getLocation().getLocationId())
        .niinLocationId(niinLocation.getNiinLocationId())
        .niinId(niinId)
        .statuses(Collections.singletonList("LOCSURVEYPENDING"))
        .build())
        .stream()
        .findFirst()
        .orElseThrow(() -> new StratisRuntimeException(String.format("Unable to retrieve inventory item record for niinId %d", niinId)));

    inventoryItemToUpdate.niinFound(userId);
    executeInTransaction("Inventory item", () -> inventoryItemService.save(inventoryItemToUpdate));
  }

  private void handleAdd(InventoryItem inventoryItem, Integer niinId, NiinLocationInput niinLocationInput, Integer userId) {

    val addedNiinLocation = new NiinLocation();
    val niinInfo = niinInfoService.findById(niinId).orElseThrow(() -> new StratisRuntimeException("Unable to find niin information."));

    addedNiinLocation.surveyAddNiinLocation(niinInfo, inventoryItem.getLocation(), niinLocationInput.getCc(), niinLocationInput.getExpirationDate(), userId);
    executeInTransaction("Niin location", () -> niinLocationService.save(addedNiinLocation));

    val insertedNiinLocation = niinLocationService.search(NiinLocationSearchCriteria.builder()
        .niinId(addedNiinLocation.getNiinInfo().getNiinId())
        .locationId(addedNiinLocation.getLocation().getLocationId())
        .build())
        .stream()
        .findFirst()
        .orElseThrow(() -> new StratisRuntimeException("Unable to find added NIIN location information."));

    val inventorySetupAmService = SpringAdfBindingUtils.getInventorySetupService();
    inventorySetupAmService.setUserId(userId);
    inventorySetupAmService.createNIINInventory(niinLocationInput.getNiinId(), false, true);

    val workloadManagerService = SpringAdfBindingUtils.getWorkloadManagerService();
    workloadManagerService.createErrorQueueRecord(
        "51",
        "NIIN_LOC_ID",
        String.valueOf(insertedNiinLocation.getNiinLocationId()),
        String.valueOf(userId),
        null);
  }

  /**
   * Wraps a database action in a transaction
   */
  private void executeInTransaction(String title, Runnable update) {

    transactionExecutor.execute("txSaveNiinLocation", update, e -> {
      throw new StratisRuntimeException(String.format("Error saving %s during Location Survey", title), e);
    });
  }
}