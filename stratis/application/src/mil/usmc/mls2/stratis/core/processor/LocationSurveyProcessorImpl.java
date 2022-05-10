package mil.usmc.mls2.stratis.core.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.DefaultTransactionExecutor;
import mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyListManager;
import mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyNiinsListManager;
import mil.usmc.mls2.stratis.core.service.InventoryItemService;
import mil.usmc.mls2.stratis.core.service.NiinInfoService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyListManager.removeLocationSurveyFromUser;
import static mil.usmc.mls2.stratis.core.manager.InventoryItemLocationSurveyNiinsListManager.*;

@Slf4j
@Component
@RequiredArgsConstructor
class LocationSurveyProcessorImpl implements LocationSurveyProcessor {

  private final InventoryItemService inventoryItemService;
  private final NiinInfoService niinInfoService;
  private final DefaultTransactionExecutor transactionExecutor;
  private final GlobalConstants globalConstants;

  /**
   * Handles failing the entire location survey.
   * Sets the status of all remaining {@link InventoryItem} to "LOCSURVEYFAILED"
   * and creates a new inventory record for all failed niin items.
   */
  @Override
  public void failSurvey(Integer locationId, Integer userId) {

    val inventorySetupAmService = SpringAdfBindingUtils.getInventorySetupService();
    inventorySetupAmService.setUserId(userId);

    applyToSurvey(
        locationId,
        Arrays.asList(
            "LOCSURVEYFOUND",
            "LOCSURVEYNOTFOUND",
            "LOCSURVEYPENDING",
            "LOCSURVEYFAILED",
            "LOCSURVEYCOMPLETED"),
        item -> {
          item.surveyFailed(userId);
          executeInTransaction(() -> inventoryItemService.save(item));

          inventorySetupAmService.createNIINInventory(item.getNiinId(), false, true);
          removeLocationSurveyFromUser(userId, item.getInventoryItemId());
        });

    removeAllLocationSurveyNiinIdsFromUser(userId);
  }

  /**
   * Handles bypassing the entire location survey.
   */
  @Override
  public SpaPostResponse bypassSurvey(Integer locationId, Integer userId, HttpServletRequest request) {

    val response = SMVUtility.processPostResponse(request.getSession(), null);

    applyToSurvey(
        locationId,
        Arrays.asList(
            "LOCSURVEYFOUND",
            "LOCSURVEYNOTFOUND",
            "LOCSURVEYPENDING",
            "LOCSURVEYFAILED",
            "LOCSURVEYCOMPLETED"),
        item -> {
          item.resetSurvey(null);
          executeInTransaction(() -> inventoryItemService.save(item));
          removeLocationSurveyFromUser(userId, item.getInventoryItemId());
        });

    removeAllLocationSurveyNiinIdsFromUser(userId);
    redirectUser(userId, response);

    return response;
  }

  /**
   * Handles finalizing the location survey and redirecting the user to the proper screen.
   * During finalization it checks for any failures so that it can fail the whole survey.
   * Also, since one Location survey can finalize another, we need to possibly skip surveys
   * in the queue if they are no longer pending.
   */
  @Override
  public SpaPostResponse processForNext(InventoryItem inventoryItem, Integer niinId, Integer userId, HttpServletRequest request, SpaPostResponse response) {

    // If niinId is null we are done with the survey and need to proceed to the finalization process
    if (null != niinId) removeLocationSurveyNiinFromUser(userId, niinId);
    else removeAllLocationSurveyNiinIdsFromUser(userId);

    // Determine if this is the last niin in the list
    if (InventoryItemLocationSurveyNiinsListManager.hasNext(userId)) {
      response.setRedirectUrl(MappingConstants.LOCATION_SURVEY_DETAIL);

      // Populate LocationSurveyInput for next detail screen
      val nextNiinId = getNextLocationSurveyNiinIdForUser(userId);
      val niin = niinInfoService.findById(nextNiinId)
          .map(NiinInfo::getNiin)
          .orElseThrow(() -> new StratisRuntimeException(String.format("No Niin Information found for NIIN_ID %s", nextNiinId)));

      request.getSession().setAttribute(
          globalConstants.getLocationSurveySessionAttrib(),
          LocationSurveyInput.builder()
              .inventoryItemId(inventoryItem.getInventoryItemId())
              .location(inventoryItem.getLocation().getLocationLabel())
              .niin(niin)
              .build());

      return response;
    }

    // Remove the survey from the list and finalize the location survey
    removeLocationSurveyFromUser(userId, inventoryItem.getInventoryItemId());
    finalizeSurvey(inventoryItem, userId);
    redirectUser(userId, response);

    return response;
  }

  /**
   * Determines if the survey passed or failed and updates the db accordingly.
   */
  private void finalizeSurvey(InventoryItem inventoryItem, Integer userId) {

    val locationId = inventoryItem.getLocation().getLocationId();

    // Skip any location surveys that have already been processed
    applyToSurvey(
        locationId,
        Arrays.asList("LOCSURVEYNOTFOUND", "LOCSURVEYFOUND"),
        i -> removeLocationSurveyFromUser(userId, i.getInventoryItemId()));

    // Map all niins that were found to completed
    applyToSurvey(
        locationId,
        Collections.singletonList("LOCSURVEYFOUND"),
        item -> {
          item.surveyCompleted(userId);
          executeInTransaction(() -> inventoryItemService.save(item));
        });

    // Map all niins that were found to failed
    applyToSurvey(
        locationId,
        Collections.singletonList("LOCSURVEYNOTFOUND"),
        item -> {
          item.surveyFailed(userId);
          executeInTransaction(() -> inventoryItemService.save(item));
        });

    // Check if the user missed any niins
    // Check for any failures
    // If either fail the survey
    inventoryItemService.search(InventoryItemSearchCriteria.builder()
        .locationId(locationId)
        .statuses(Arrays.asList("LOCSURVEYPENDING", "LOCSURVEYFAILED"))
        .build())
        .stream()
        .findFirst()
        .ifPresent(item -> failSurvey(item.getLocation().getLocationId(), userId));

    val inventorySetupAmService = SpringAdfBindingUtils.getInventorySetupService();
    inventorySetupAmService.setUserId(userId);

    // Remove all surveys in this group
    applyToSurvey(
        locationId,
        Arrays.asList("LOCSURVEYPENDING", "LOCSURVEYFAILED", "LOCSURVEYCOMPLETED"),
        i -> inventorySetupAmService.submitInventoryItemRemoveSurvey(
            i.getNiinLocation().getNiinLocationId(),
            i.getInventoryItemId(),
            i.getInventoryId(),
            userId));
  }

  /**
   * Helper method to apply changes to all niins in a location survey.
   */
  private void applyToSurvey(Integer locationId, List<String> statuses, Consumer<InventoryItem> action) {

    inventoryItemService.search(InventoryItemSearchCriteria.builder()
        .locationId(locationId)
        .statuses(statuses)
        .build())
        .forEach(action);
  }

  /**
   * Route user to appropriate page
   */
  private void redirectUser(Integer userId, SpaPostResponse response) {

    if (!InventoryItemLocationSurveyListManager.hasNext(userId)) {
      response.setRedirectUrl(MappingConstants.INVENTORY_HOME);
      response.addNotification("All Location Surveys have been processed.");
    }
    else response.setRedirectUrl(MappingConstants.LOCATION_SURVEY);
  }

  /**
   * Wraps a database action in a transaction
   */
  private void executeInTransaction(Runnable update) {

    transactionExecutor.execute("txSaveNiinLocation", update, e -> {
      throw new StratisRuntimeException("Error saving Inventory item during Location Survey", e);
    });
  }
}
