package mil.usmc.mls2.stratis.modules.smv.api.inventory.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.StringUtil;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.InventoryItemsListManager;
import mil.usmc.mls2.stratis.core.processor.InventoryItemProcessor;
import mil.usmc.mls2.stratis.core.service.InvSerialLotNumService;
import mil.usmc.mls2.stratis.core.service.InventoryItemService;
import mil.usmc.mls2.stratis.core.service.SerialLotNumTrackService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import mil.usmc.mls2.stratis.modules.smv.utility.ValidationUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.INVENTORY_DETAIL)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class InventoryDetailController {

  private final InventoryItemService inventoryItemService;
  private final InventoryItemProcessor inventoryItemProcessor;
  private final InvSerialLotNumService invSerialLotNumService;
  private final SerialLotNumTrackService serialLotNumTrackService;
  private final GlobalConstants globalConstants;

  private static final String DEFAULT_PAGE = "mobile/inventory/inventory/detail";

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val inventoryItemId = InventoryItemsListManager.getNextInventoryItemForUser(user.getUserId());

      if (inventoryItemId == null) {
        //Assumption is you should not get here without having picks to work.
        throw new StratisRuntimeException("No Inventory Item Available to process");
      }
      val inventoryItem = inventoryItemService.findById(inventoryItemId).orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));

      val inventoryItemInput = InventoryItemInput.builder().inventoryItemId(inventoryItem.getInventoryItemId()).build();

      request.setAttribute(globalConstants.getInventoryItemSessionAttrib(), inventoryItemInput);
      request.setAttribute("inventoryItem", inventoryItem);
      request.setAttribute("serialControlled", "Y".equals(inventoryItem.getNiinLocation().getNiinInfo().getSerialControlFlag()));
      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Inventory Detail");
    }
    catch (Exception e) {
      log.error("Error occurred retrieving the Inventory Details", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping("/addSerial")
  public SpaPostResponse addSerial(InventoryItemInput inventoryItemInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();

      val inventoryItemId = InventoryItemsListManager.getCurrentInventoryItemForUser(userId, inventoryItemInput.getInventoryItemId());
      if (inventoryItemId == null) {
        throw new StratisRuntimeException(("No Inventory Item Id found for processing"));
      }

      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), null);

      val inventoryItem = inventoryItemService.findById(inventoryItemId).orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));

      validateSerialNumber(inventoryItem, inventoryItemInput.getSerial(), spaPostResponse);

      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Inventory add Serial Number", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping
  public SpaPostResponse postSubmit(InventoryItemInput inventoryItemInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();

      val inventoryItemId = InventoryItemsListManager.getCurrentInventoryItemForUser(userId, inventoryItemInput.getInventoryItemId());
      if (inventoryItemId == null) {
        throw new StratisRuntimeException(("No Inventory Item Id found for processing"));
      }

      val inventoryItem = inventoryItemService.findById(inventoryItemId).orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));

      val serialControlled = "Y".equals(inventoryItem.getNiinLocation().getNiinInfo().getSerialControlFlag());

      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), "mobile/inventory/inventory/");

      validate(inventoryItemInput, inventoryItem, spaPostResponse);
      if (serialControlled && ValidationUtility.validateSerialNumberInput(inventoryItemInput.getSerials(), inventoryItemInput.getLocationQty(), spaPostResponse)) {
        spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      }

      //if any validation warnings were found, return.
      if (spaPostResponse.getResult().equals(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS)) {
        return spaPostResponse;
      }

      inventoryItemProcessor.processInventory(inventoryItem, inventoryItemInput, userId);
      spaPostResponse.addNotification("Inventory Item Processed Successfully");
      //If there is a next return to detail to process, otherwise go to the picking home page.
      inventoryItemProcessor.processForNext(spaPostResponse, user);

      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Inventory submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping(value = "/bypass")
  public SpaPostResponse postBypass(InventoryItemInput inventoryItemInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      val userId = user.getUserId();

      val inventoryItemId = InventoryItemsListManager.getCurrentInventoryItemForUser(userId, inventoryItemInput.getInventoryItemId());
      if (inventoryItemId == null) {
        throw new StratisRuntimeException(("No Inventory Item Id found for processing"));
      }

      val inventoryItem = inventoryItemService.findById(inventoryItemId).orElseThrow(() -> new StratisRuntimeException(String.format("No Inventory Item found for ID %s", inventoryItemId)));

      inventoryItem.assignItem(null);
      inventoryItemService.save(inventoryItem);
      InventoryItemsListManager.removeInventoryItemFromUser(userId, inventoryItem.getInventoryItemId());

      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), null);

      spaPostResponse.addNotification("Inventory Item Bypassed");
      inventoryItemProcessor.processForNext(spaPostResponse, user);
      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Inventory bypass post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  private void validate(InventoryItemInput input, InventoryItem inventoryItem, SpaPostResponse spaPostResponse) {
    boolean validationWarningsFound = false;
    if ("Y".equals(inventoryItem.getNiinLocation().getLocked())) {
      spaPostResponse.addWarning("Location Locked - ByPass.");
      validationWarningsFound = true;
    }

    if (input.getLocation().length() <= 8) {
      spaPostResponse.addWarning("Invalid Location Label.");
      validationWarningsFound = true;
    }
    else if (!input.getLocation().equalsIgnoreCase(inventoryItem.getNiinLocation().getLocation().getLocationLabel())) {
      spaPostResponse.addWarning("Location entry mismatch, Re-enter location or bypass pick.");
      validationWarningsFound = true;
    }

    val niin = input.getNiin();
    val recordNiin = inventoryItem.getNiinLocation().getNiinInfo().getNiin();
    if (!StringUtil.isEmpty(niin)) {
      if (niin.length() != 2) {
        spaPostResponse.addWarning("Niin Length must be 2.");
        validationWarningsFound = true;
      }
      else if (!niin.equals(recordNiin.substring(recordNiin.length() - 2))) {
        spaPostResponse.addWarning("Invalid last two digits of NIIN.");
        validationWarningsFound = true;
      }
    }

    if (input.getLocationQty() < 0) {
      spaPostResponse.addWarning("Quantity can not be negative.");
      validationWarningsFound = true;
    }
    else if (!input.isReconfirmQtyRequired() && !input.getLocationQty().equals(inventoryItem.getNiinLocation().getQty())) {
      validationWarningsFound = true;
      spaPostResponse.addWarning("Reconfirm Quantity.  Please re-enter.");
      spaPostResponse.addFlag("quantityMisMatch");
    }
    else if (input.isReconfirmQtyRequired() && !input.getLocationQty().equals(input.getReconfirmLocationQty())) {
      validationWarningsFound = true;
      spaPostResponse.addWarning("Reconfirm Quantity Does not match.");
    }

    if (validationWarningsFound) spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
  }

  private void validateSerialNumber(InventoryItem inventoryItem, String serialNumber, SpaPostResponse spaPostResponse) {
    AtomicBoolean errorsFound = new AtomicBoolean(false);
    if (serialNumber == null) {
      spaPostResponse.addWarning("Serial Number is required.");
      errorsFound.set(true);
    }
    else {
      val invSerialLotCriteria = InvSerialLotNumSearchCriteria.builder()
          .niinId(inventoryItem.getNiinLocation().getNiinInfo().getNiinId())
          .serialNumber(serialNumber)
          .build();

      val invSerialLotNumMatches = invSerialLotNumService.search(invSerialLotCriteria);

      if (CollectionUtils.isEmpty(invSerialLotNumMatches)) {
        //no match for serial numebr in inv_serial_lot_num, now check serial_lot_num
        val serialLotNumTrackSearchCriteria = SerialLotNumTrackSearchCriteria.builder()
            .niinId(inventoryItem.getNiinLocation().getNiinInfo().getNiinId())
            .serialNumber(serialNumber)
            .build();

        val serialLotNumTrackMatches = serialLotNumTrackService.search(serialLotNumTrackSearchCriteria);

        if (CollectionUtils.isEmpty(serialLotNumTrackMatches)) {
          spaPostResponse.addNotification(String.format("Serial Number (%s) does not match location.", serialNumber));
        }
        else {
          //found a matching serial number, is it at a different location?
          AtomicBoolean otherLocationMatch = new AtomicBoolean(false);

          serialLotNumTrackMatches.forEach(x -> {
            if (!x.getLocationId().equals(inventoryItem.getNiinLocation().getLocation().getLocationId())) {
              otherLocationMatch.set(true);
            }
          });

          if (otherLocationMatch.get()) {
            spaPostResponse.addWarning(String.format("Serial Number (%s) entered in other location.", serialNumber));
            errorsFound.set(true);
          }
        }
      }
      else {
        AtomicBoolean otherLocationMatch = new AtomicBoolean(false);

        invSerialLotNumMatches.forEach(x -> {
          if (!x.getLocationId().equals(inventoryItem.getNiinLocation().getLocation().getLocationId())) {
            otherLocationMatch.set(true);
          }
        });

        if (otherLocationMatch.get()) {
          spaPostResponse.addWarning(String.format("Serial Number (%s) entered in other location.", serialNumber));
          errorsFound.set(true);
        }
      }
    }
    if (errorsFound.get()) spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
  }
}
