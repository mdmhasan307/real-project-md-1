package mil.usmc.mls2.stratis.modules.smv.api.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyListManager;
import mil.usmc.mls2.stratis.core.manager.InventoryItemsListManager;
import mil.usmc.mls2.stratis.core.manager.ShelfLifeListManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.InventoryItemService;
import mil.usmc.mls2.stratis.core.service.NiinLocationService;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.INVENTORY_HOME)
@RequiredArgsConstructor
@SuppressWarnings("squid:S1192")
public class InventoryHomeController {

  private static final String DEFAULT_PAGE = "mobile/inventory/home";

  private final EquipmentService equipmentService;
  private final InventoryItemService inventoryItemService;
  private final NiinLocationService niinLocationService;
  private final GlobalConstants globalConstants;
  private final DateService dateService;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }
    request.setAttribute("inventoryEnabled", globalConstants.isInventoryEnabledBySystem());
    request.setAttribute("locationSurveyEnabled", globalConstants.isLocationSurveyEnabledBySystem());
    request.setAttribute("shelfLifeEnabled", globalConstants.isShelfLifeEnabledBySystem());

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Inventory");
  }

  @PostMapping("/selection/{selectedOption}")
  public SpaPostResponse post(@PathVariable String selectedOption, HttpServletRequest request) {
    val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), null);
    try {
      if ("inventory".equals(selectedOption)) {
        spaPostResponse.setRedirectUrl(MappingConstants.INVENTORY_DETAIL);
        inventory(request, spaPostResponse);
      }
      else if ("shelfLife".equals(selectedOption)) {
        spaPostResponse.setRedirectUrl(MappingConstants.SHELF_LIFE_DETAIL);
        shelfLife(request, spaPostResponse);
      }
      else if ("locationSurvey".equals(selectedOption)) {
        spaPostResponse.setRedirectUrl(MappingConstants.LOCATION_SURVEY);
        locationSurvey(request, spaPostResponse);
      }
      else {
        throw new StratisRuntimeException("Invalid Inventory Menu option selection");
      }
      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Inventory Main menu selection", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  //(470)
  private void inventory(HttpServletRequest request, SpaPostResponse spaPostResponse) {
    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());

    val workstation = equipmentService.findById(user.getWorkstationId()).orElseThrow(() -> new StratisRuntimeException("No Workstation found"));
    val wac = workstation.getWac();

    List<String> pendingInventoryStatuses = new ArrayList<>();
    pendingInventoryStatuses.add("INVENTORYPENDING");

    val inventoryItemSearchCriteria = InventoryItemSearchCriteria.builder()
        .wacId(wac.getWacId())
        .statuses(pendingInventoryStatuses)
        .assignedUserId(user.getUserId())
        .allowNullAssignedUserIds(true)
        .build();

    val results = inventoryItemService.getInventoryListForProcessing(inventoryItemSearchCriteria, wac);

    //if no inventory available to process, just warn them, and don't send the page to another screen.
    if (CollectionUtils.isEmpty(results)) {
      spaPostResponse.addNotification("No inventory available to process at this time.");
      spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      spaPostResponse.setRedirectUrl(null);
    }
    else {
      InventoryItemsListManager.assignInventoryToUser(user.getUserId(), results.stream().map(InventoryItem::getInventoryItemId).collect(Collectors.toList()));
    }
  }

  private void shelfLife(HttpServletRequest request, SpaPostResponse spaPostResponse) {
    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());

    val workstation = equipmentService.findById(user.getWorkstationId()).orElseThrow(() -> new StratisRuntimeException("No Workstation found"));
    val wac = workstation.getWac();

    val niinLocationSearchCriteria = NiinLocationSearchCriteria.builder()
        .wacId(wac.getWacId())
        .expRemark("Y")
        .checkNumExtentsNull(true)
        .expirationDate(dateService.getLastDateOfThisMonth())
        .build();

    val results = niinLocationService.getNiinLocationListForProcessing(niinLocationSearchCriteria, wac);
    //if no inventory available to process, just warn them, and don't send the page to another screen.
    if (CollectionUtils.isEmpty(results)) {
      spaPostResponse.addNotification("No shelf life inventory available to process at this time.");
      spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      spaPostResponse.setRedirectUrl(null);
    }
    else {
      ShelfLifeListManager.assignNiinLocationsToUser(user.getUserId(), results.stream().map(NiinLocation::getNiinLocationId).collect(Collectors.toList()));
    }
  }

  private void locationSurvey(HttpServletRequest request, SpaPostResponse response) {
    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());

    val workstation = equipmentService.findById(user.getWorkstationId()).orElseThrow(() -> new StratisRuntimeException("No Workstation found"));
    val wac = workstation.getWac();

    val inventoryItemSearchCriteria = InventoryItemSearchCriteria.builder()
        .wacId(wac.getWacId())
        .statuses(Collections.singletonList("LOCSURVEYPENDING"))
        .assignedUserId(user.getUserId())
        .allowNullAssignedUserIds(true)
        .build();

    val results = inventoryItemService.getInventoryListForProcessing(inventoryItemSearchCriteria, wac);

    if (CollectionUtils.isEmpty(results)) {
      response.addNotification("No Location Surveys available to process at this time.");
      response.setRedirectUrl(MappingConstants.INVENTORY_HOME);
    }
    else {
      val userId = user.getUserId();
      val criteria = InventoryItemSearchCriteria.builder()
          .wacId(wac.getWacId())
          .statuses(Collections.singletonList("LOCSURVEYPENDING"))
          .build();
      val inventoryItemIds = results.stream()
          .filter(distinctBy(ii -> ii.getLocation().getLocationId()))
          .flatMap(ii -> {
            val assignmentConflict = checkAssignmentConflict(criteria, ii, userId);
            if (!assignmentConflict.isPresent()) return Stream.of(ii);
            else return findNextAvailableSurvey(criteria, userId);
          })
          .flatMap(ii -> inventoryItemService.search(criteria.toBuilder().locationId(ii.getLocation().getLocationId()).build()).stream())
          .filter(distinctBy(InventoryItem::getNiinId))
          .peek(ii -> {
            if (wac.getMechanizedFlag().equalsIgnoreCase("N")) {
              ii.assignItem(userId);
              inventoryItemService.save(ii);
            }
          })
          .map(InventoryItem::getInventoryItemId)
          .collect(Collectors.toList());
      InventoryItemLocationSurveyListManager.assignLocationSurveyToUser(userId, inventoryItemIds);
    }
  }

  /**
   * Stateful predicate to help filter duplicate fields of an {@link InventoryItem}
   */
  private Predicate<InventoryItem> distinctBy(Function<InventoryItem, Integer> function) {

    Map<Object, Boolean> seen = new HashMap<>();
    return i -> seen.putIfAbsent(function.apply(i), Boolean.TRUE) == null;
  }

  private Optional<InventoryItem> checkAssignmentConflict(InventoryItemSearchCriteria criteria, InventoryItem inventoryItem, Integer userId) {

    return inventoryItemService.search(criteria.toBuilder().locationId(inventoryItem.getLocation().getLocationId()).build())
        .stream()
        .filter(i -> null != i.getAssignToUser())
        .filter(i -> !i.getAssignToUser().equals(userId))
        .findAny();
  }

  private Stream<InventoryItem> findNextAvailableSurvey(InventoryItemSearchCriteria criteria, Integer userId) {

    return inventoryItemService.search(criteria.toBuilder().assignedUserId(null).build())
        .stream()
        .filter(distinctBy(i -> i.getLocation().getLocationId()))
        .filter(i -> !checkAssignmentConflict(criteria, i, userId).isPresent())
        .findAny()
        .map(Stream::of)
        .orElseGet(Stream::empty);
  }
}
