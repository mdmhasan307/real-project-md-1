package mil.usmc.mls2.stratis.modules.smv.api.inventory.shelflife;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.manager.ShelfLifeListManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.ErrorQueueService;
import mil.usmc.mls2.stratis.core.service.ErrorService;
import mil.usmc.mls2.stratis.core.service.NiinLocationService;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.SHELF_LIFE_DETAIL)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class ShelfLifeDetailController {

  private static final String DEFAULT_PAGE = "mobile/inventory/shelfLife/detail";
  private static final String ERROR_KEY = "NIIN_LOC_ID";
  private final NiinLocationService niinLocationService;
  private final ErrorQueueService errorQueueService;
  private final ErrorService errorService;
  private final EquipmentService equipmentService;
  private final DateService dateService;

  /**
   * Handle initial GET request by displaying the next shelf life item.
   */
  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }

      val user = (UserInfo) request.getSession().getAttribute("userbean");
      val niinLocId = ShelfLifeListManager.getNextNiinLocationForUser(user.getUserId());
      val niinLocationResult = niinLocationService.findById(niinLocId);
      if (niinLocationResult.isPresent()) {
        val niinLocation = niinLocationResult.get();
        val formatter = DateTimeFormatter.ofPattern(DateConstants.SITE_DATE_FORMATTER_PATTERN);
        val newDate = calculateNewDate(niinLocation.getExpirationDate(), niinLocation.getNiinInfo().getShelfLifeCode());
        request.setAttribute("fsc", niinLocation.getNiinInfo().getFsc());
        request.setAttribute("location", niinLocation.getLocation().getLocationLabel());
        request.setAttribute("niin", niinLocation.getNiinInfo().getNiin());
        request.setAttribute("currentExpirationDate", niinLocation.getExpirationDate().format(formatter));
        request.setAttribute("newExpirationDate", newDate.format(formatter));
      }
      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shelf Life Detail");
    }
    catch (Exception e) {
      log.error("Error occurred retrieving the Shelf Life Details", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * Process the submitted shelf life entry according to processing type (confirm | noextend | skip)
   */
  @PostMapping(value = "/detail/{type}")
  public SpaPostResponse post(@PathVariable String type, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val user = (UserInfo) request.getSession().getAttribute("userbean");
      if (ShelfLifeListManager.hasNext(user.getUserId())) {
        log.warn("**Here we process shelf life information as type : {}**", type);
        val niinLocId = ShelfLifeListManager.getNextNiinLocationForUser(user.getUserId());
        switch (type) {
          case "confirm":
            handleConfirm(niinLocId, user.getUserId());
            break;
          case "noextend":
            handleNoExtend(niinLocId, user.getUserId());
            break;
          case "skip":
            //just skip this inventory
            break;
          default:
            throw new StratisRuntimeException("Unknown Action");
        }
        ShelfLifeListManager.removeNiinLocationFromUser(user.getUserId(), niinLocId);
      }

      if (ShelfLifeListManager.hasNext(user.getUserId())) {
        return SMVUtility.processPostResponse(request.getSession(), MappingConstants.SHELF_LIFE_DETAIL);
      }
      else {
        val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
        val wacId = workstation.getWac().getWacId();
        val shelflifeCriteria = NiinLocationSearchCriteria.builder()
            .wacId(wacId)
            .expRemark("Y")
            .checkNumExtentsNull(true)
            .expirationDate(dateService.getLastDateOfThisMonth())
            .build();
        val shelfLifeCount = niinLocationService.count(shelflifeCriteria);

        val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), MappingConstants.INVENTORY_HOME);
        spaPostResponse.addNotification("All shelf life inventories has been processed.");
        if (shelfLifeCount <= 0) {
          spaPostResponse.setRedirectUrl(MappingConstants.SMV_HOME);
        }
        return spaPostResponse;
      }
    }
    catch (Exception e) {
      log.error("Error occurred processing Shelf Life submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * Process the submitted shelf life entry if a user wishes to extend its expiration date
   *
   * @param niinLocId Niin location to extend the expiration date of
   * @param userId    user updating the exp date
   */
  private void handleConfirm(Integer niinLocId, Integer userId) {
    //update Expiration
    val niinLocationResult = niinLocationService.findById(niinLocId);
    if (niinLocationResult.isPresent()) {
      var niinLocation = niinLocationResult.get();
      val newExpDate = calculateNewDate(niinLocation.getExpirationDate(), niinLocation.getNiinInfo().getShelfLifeCode());
      niinLocation.updateExpirationDateForShelfLife(newExpDate, userId);
      niinLocationService.save(niinLocation);

      //delete eError queue records
      Error stowError = errorService.findByCode("EXP_REMARK");
      val errorQueueSearch = ErrorQueueCriteria.builder()
          .eid(stowError.getId())
          .keyType(ERROR_KEY)
          .keyNum(niinLocation.getNiinLocationId().toString()).build();
      val errorQueueResults = errorQueueService.search(errorQueueSearch);
      for (ErrorQueue e : errorQueueResults) {
        errorQueueService.save(e);
        errorQueueService.delete(e);
      }
    }
  }

  /**
   * Calculates the new expiration date
   *
   * @param expDate current date
   * @param refSlc  shelf life code reference object from db
   */
  private LocalDate calculateNewDate(LocalDate expDate, RefSlc refSlc) {
    return expDate.plusDays((refSlc != null ? refSlc.getMinVSpan() : 0));
  }

  /**
   * Process the submitted shelf life entry if a user does not wish to extend its expiration date
   *
   * @param niinLocId Niin location to extend the expiration date of
   * @param userId    user updating the exp date
   */
  private void handleNoExtend(Integer niinLocId, Integer userId) {
    val niinLocationResult = niinLocationService.findById(niinLocId);
    if (niinLocationResult.isPresent()) {
      Error stowError = errorService.findByCode("EXP_REMARK");
      var errorQueueSearch = ErrorQueueCriteria.builder()
          .eid(stowError.getId())
          .keyType(ERROR_KEY)
          .keyNum(niinLocId.toString()).build();
      if (errorQueueService.count(errorQueueSearch) <= 0) {
        var newErrorQueue = ErrorQueue.builder()
            .status("Open")
            .createdBy(userId)
            .createdDate(OffsetDateTime.now())
            .eid(stowError.getId())
            .keyType(ERROR_KEY)
            .keyNum(niinLocId.toString()).build();
        errorQueueService.save(newErrorQueue);
      }
    }
  }
}