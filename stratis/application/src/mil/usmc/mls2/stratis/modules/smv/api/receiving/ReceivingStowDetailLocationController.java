package mil.usmc.mls2.stratis.modules.smv.api.receiving;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.stratis.common.dm.LocationSelectionOption;
import mil.stratis.common.util.StringUtil;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.processor.ReceiptProcessor;
import mil.usmc.mls2.stratis.core.processor.StowProcessor;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.LabelPrintUtil;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils.getWorkloadManagerService;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.RECEIVING_LOCATION)
@RequiredArgsConstructor
public class ReceivingStowDetailLocationController {

  private static final String DEFAULT_PAGE = "mobile/receiving/stowDetailLocation";

  private final GlobalConstants globalConstants;
  private final LabelPrintUtil labelPrintUtil;
  private final ReceiptService receiptService;
  private final StowService stowService;
  private final LocationService locationService;
  private final NiinLocationService niinLocationService;
  private final StowProcessor stowProcessor;
  private final ErrorQueueService errorQueueService;
  private final ErrorService errorService;
  private final EquipmentService equipmentService;
  private final WacService wacService;
  private final ReceiptProcessor receiptProcessor;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);

      val receivingWorkflowContainer = (ReceivingWorkflowContainer) request.getSession()
          .getAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());
      val receipt = receivingWorkflowContainer.getReceipt();

      request.setAttribute("receipt", receipt);
      request.setAttribute("serialControlFlag", receivingWorkflowContainer.getReceipt().getNiinInfo().getSerialControlFlag());
      request.setAttribute("receivingStowDetailLocationInput", new ReceivingStowDetailLocationInput(receivingWorkflowContainer));

      val wacs = wacService.getAll();
      request.setAttribute("wacs", wacs);

      var errorResult = "";
      try {
        request.setAttribute("locations", getLocationList(receipt, ""));
      }
      catch (StratisRuntimeException validationError) {
        errorResult = validationError.getMessage();
      }
      val response = SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Receiving Stow Detail Location");
      if (!errorResult.isEmpty()) {
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        response.addWarning(errorResult);
      }
      return response;
    }
    catch (Exception e) {
      log.error("Error occurred navigating to stow detail location.", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping
  public SpaPostResponse post(ReceivingStowDetailLocationInput receivingStowDetailLocationInput, HttpServletRequest request) {
    val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.RECEIVING_HOME);

    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
    val locationId = Integer.parseInt(receivingStowDetailLocationInput.getLocation());
    val location = locationService.findById(locationId);

    //note this should never happen as the locations is a drop down selection from a db query.
    if (!location.isPresent()) {
      val e = new StratisRuntimeException("No Location found matching selection");
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
    val receivingWorkflowContainer = (ReceivingWorkflowContainer) request.getSession()
        .getAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());

    //pull the latest version of the receipt just in case.
    val receipt = receiptService.findById(receivingWorkflowContainer.getReceipt().getRcn())
        .orElseThrow(() -> new StratisRuntimeException("Receipt not found"));

    val ninLoc = niinLocationService.search(NiinLocationSearchCriteria.builder()
        .niinId(receipt.getNiinInfo().getNiinId())
        .locationId(location.get().getLocationId()).build())
        .stream().findFirst();
    if (ninLoc.isPresent() && !ninLoc.get().getCc().equalsIgnoreCase(receipt.getCc())) {
      response.addWarning("CC(s) of NIIN(s) at this location do not match for . Please assign different location.");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return response;
    }

    val stowSearchCriteria = StowSearchCriteria.builder()
        .receipt(receipt)
        .build();

    val stows = stowService.search(stowSearchCriteria);

    stows.forEach(stow -> {
      stow.finalizeStowForReceiptCreation(location.get(), user.getUserId());
      stowService.update(stow);
    });

    log.info(String.format("Receipt complete via SMV with RCN [%s]", receipt.getRcn()));
    receipt.completeReceipt(user.getUserId());
    receiptService.save(receipt);

    val workloadService = SpringAdfBindingUtils.getWorkloadManagerService();
    val dasfOverride = "Override".equalsIgnoreCase(receivingWorkflowContainer.getReceivingDetailInput().getDasfQtyOverride());
    Receipt d6aReceipt = null;
    log.info("Sending D6 transaction via SMV.");
    if (dasfOverride) {
      d6aReceipt = receiptProcessor.copyAndUpdateToD6AReceipt(receipt, location.get());
    }
    val d6ReturnCode = workloadService.getGCSSMCTransactionsService().sendD6GCSSMCTransaction(Integer.toString(receipt.getRcn()), dasfOverride);
    if (d6ReturnCode == -2) {
      response.addWarning("Duplicate D6 transaction found.");
      response.setRedirectUrl(MappingConstants.RECEIVING_HOME);
      return response;
    }
    if (dasfOverride) {
      val d6aReturnCode = workloadService.getGCSSMCTransactionsService().sendD6GCSSMCTransaction(Integer.toString(d6aReceipt.getRcn()), dasfOverride);
      if (d6aReturnCode == -2) {
        response.addWarning("Duplicate D6 transaction found.");
        response.setRedirectUrl(MappingConstants.RECEIVING_HOME);
        return response;
      }
    }

    if (receipt.getQuantityInduced() != receipt.getQuantityInvoiced()) {
      val errSearch = ErrorQueueCriteria.builder()
          .keyType("RCN")
          .keyNum(Integer.toString(receipt.getRcn())).build();
      val errCount = errorQueueService.count(errSearch);
      if (errCount == 0) {

        val error = errorService.findByCode("RECEIPT_QTYRECV");
        stowProcessor.saveErrorForReceipt(error, receipt, user);
      }
    }

    val allStows = stowService.search(stowSearchCriteria);
    if (d6aReceipt != null) {
      val newReceiptSearch = StowSearchCriteria.builder().receipt(d6aReceipt).build();

      val newStows = stowService.search(newReceiptSearch);
      allStows.addAll(newStows);
    }

    StringBuilder printString = new StringBuilder();
    allStows.forEach(stow -> printString.append(workloadService.generatePrintSIDLabel(stow.getSid(), 0, 0)));

    if (!StringUtils.isEmpty(printString)) {
      val sidList = allStows.stream().map(s -> s.getSid()).toArray(String[]::new);
      labelPrintUtil.printLabel(user, printString.toString(), sidList, LabelType.STOW_LABEL);
      request.getSession().setAttribute(globalConstants.getUserbeanSession(), user);
    }

    request.getSession().removeAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());
    val suffixStr = receipt.getSuffix() != null ? receipt.getSuffix() : "";
    if (dasfOverride) {
      val d6aSuffixStr = d6aReceipt != null && d6aReceipt.getSuffix() != null ? d6aReceipt.getSuffix() : "";
      val d6aRcn = d6aReceipt != null ? d6aReceipt.getRcn() : "";
      val d6aDocumentNumber = d6aReceipt != null ? d6aReceipt.getDocumentNumber() : "";
      response.addNotification(String.format("Receipts %s (%s%s) for D6T and %s (%s%s) for D6A processed successfully.  SID(s) Generated: %s",
          receipt.getRcn(),
          receipt.getDocumentNumber(),
          suffixStr,
          d6aRcn,
          d6aDocumentNumber,
          d6aSuffixStr,
          allStows.stream().map(Stow::getSid).collect(Collectors.joining(","))));
    }
    else {
      response.addNotification(String.format("Receipt %s (%s%s) processed successfully.  SID(s) Generated: %s",
          receipt.getRcn(),
          receipt.getDocumentNumber(),
          suffixStr,
          allStows.stream().map(Stow::getSid).collect(Collectors.joining(","))));
    }
    return response;
  }

  @PostMapping("/list")
  public LocationListResponse list(ReceivingStowDetailLocationInput receivingStowDetailLocationInput, HttpServletRequest request) {
    val response = new LocationListResponse();

    val receivingWorkflowContainer = (ReceivingWorkflowContainer) request.getSession()
        .getAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());
    val receipt = receivingWorkflowContainer.getReceipt();

    try {
      response.setList(getLocationList(receipt, receivingStowDetailLocationInput.getWac()));
    }
    catch (StratisRuntimeException validationError) {
      response.setError(true);
      response.setErrorMessage(validationError.getMessage());
    }
    return response;
  }

  private List<LocationSelectionOption> getLocationList(Receipt receipt, String wac) {
    val stows = stowService.search(StowSearchCriteria.builder()
        .receipt(receipt)
        .build());

    val locationListError = stows.stream()
        .map(Stow::getStowId)
        .map(Object::toString)
        .limit(1)
        .map(id -> getWorkloadManagerService().buildLocationList(
            wac,
            "",
            id,
            "ALL",
            -1))
        .filter(StringUtil::isNotEmpty)
        .findFirst();

    if (locationListError.isPresent()) {
      throw new StratisRuntimeException(locationListError.get());
    }
    return getWorkloadManagerService().locationListToSelectionOption();
  }
}
