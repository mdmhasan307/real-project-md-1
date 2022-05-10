package mil.usmc.mls2.stratis.core.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.ErrorQueueService;
import mil.usmc.mls2.stratis.core.service.LocationService;
import mil.usmc.mls2.stratis.core.service.StowService;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
class StowProcessorImpl implements StowProcessor {

  private final StowService stowService;
  private final ErrorQueueService errorQueueService;
  private final LocationService locationService;
  private final DateService dateService;

  public void createStowForReceipt(Receipt receipt, int qtyToStow, int userId, String expirationDate, String manDate, String serialNumber) {
    val sid = SpringAdfBindingUtils.getWorkloadManagerService().assignSID();

    val shelfLifeCode = receipt.getShelfLifeCode();
    LocalDate expDate = null;
    if (shelfLifeCode == null || (!shelfLifeCode.isEmpty() && shelfLifeCode.equalsIgnoreCase("0"))) {
      expDate = DateConstants.maxLocalDate;
    }

    if (!expirationDate.isEmpty() && dateService.isValidDate(expirationDate)) {
      expDate = dateService.convertLocalDateStringToLocalDate(expirationDate, DateConstants.DEFAULT_INPUT_DATE_FORMAT);
    }
    LocalDate manufactureDate = null;
    if (!manDate.isEmpty() && dateService.isValidDate(manDate)) {
      manufactureDate = dateService.convertLocalDateStringToLocalDate(manDate, DateConstants.DEFAULT_INPUT_DATE_FORMAT);
    }

    val location = locationService.findById(1).orElseThrow(() -> new StratisRuntimeException("Failed to find default location"));

    Stow stow = Stow.builder()
        .receipt(receipt) //all stows link to the same receipt.
        .sid(sid)
        .qtyToBeStowed(qtyToStow)
        .serialNumber(serialNumber)
        .createdBy(userId)
        .createdDate(OffsetDateTime.now())
        .status("STOW")
        .scanInd("N")
        .securityMarkClass("FOUO")
        .location(location)
        .expirationDate(expDate)
        .dateOfManufacture(manufactureDate)
        .caseWeightQty(0).stowQty(0).bypassCount(0).build();
    stowService.update(stow);
  }

  public void deleteErrorRecordsForReceipt(ErrorQueueCriteria errSearch) {
    val errors = errorQueueService.search(errSearch);
    errors.forEach(errorQueueService::delete);
  }

  public void deleteStowsIfExistsForReceipt(Receipt receipt) {
    val currentStows = StowSearchCriteria.builder()
        .receipt(receipt).build();
    val stows = stowService.search(currentStows);

    if (CollectionUtils.isNotEmpty(stows)) {
      stows.forEach(stowService::delete);
    }
  }

  public void saveErrorForReceipt(Error error, Receipt receipt, UserInfo user) {
    //If not add it
    ErrorQueue errorQueue = ErrorQueue.builder()
        .status("Open")
        .createdBy(user.getUserId())
        .createdDate(OffsetDateTime.now())
        .eid(error.getId())
        .keyType("RCN")
        .keyNum(Integer.toString(receipt.getRcn())).build();
    errorQueueService.save(errorQueue);
  }
}
