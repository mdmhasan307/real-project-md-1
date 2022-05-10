package mil.usmc.mls2.stratis.modules.smv.api.receiving;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.model.services.ReceiptAMImpl;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.processor.StowProcessor;
import mil.usmc.mls2.stratis.core.service.ReceiptService;
import mil.usmc.mls2.stratis.core.service.StowService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.RECEIVING_CONTROLLED_NIIN)
@RequiredArgsConstructor
public class ReceivingControlledNiinDetailsController {

  private final GlobalConstants globalConstants;
  private final StowService stowService;
  private final ReceiptService receiptService;
  private final StowProcessor stowProcessor;

  private static final String DEFAULT_PAGE = "mobile/receiving/controlledNiinDetails";

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    val receivingWorkflowContainer = (ReceivingWorkflowContainer) request.getSession().getAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());

    request.setAttribute("receipt", receivingWorkflowContainer.getReceipt());
    request.setAttribute("receivingControlledNiinDetailsInput", new ReceivingControlledNiinDetailsInput(receivingWorkflowContainer));

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Receiving Controlled NIIN Details");
  }

  @PostMapping
  public SpaPostResponse post(ReceivingControlledNiinDetailsInput receivingControlledNiinDetailsInput, HttpServletRequest request) {
    ReceivingWorkflowContainer receivingWorkflowContainer = (ReceivingWorkflowContainer) request.getSession().getAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());
    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());

    receivingWorkflowContainer.setReceivingControlledNiinDetailsInput(receivingControlledNiinDetailsInput);

    val receipt = receiptService.findById(receivingWorkflowContainer.getReceipt().getRcn()).orElseThrow(() -> new StratisRuntimeException(String.format("Receipt %s not found", receivingWorkflowContainer.getReceipt().getRcn())));

    //save the serial numbers....
    val stowCriteria = StowSearchCriteria.builder()
        .receipt(receipt)
        .build();

    val stows = stowService.search(stowCriteria);

    if (CollectionUtils.isNotEmpty(stows)) {
      throw new StratisRuntimeException("Stows Exist for this receipt already.");
    }

    val serials = receivingControlledNiinDetailsInput.getSerials();
    val receivedTotal = serials.size();

    val expDate = receivingWorkflowContainer.getReceivingDetailInput().getExpDate();
    val dom = receivingWorkflowContainer.getReceivingDetailInput().getDom();

    serials.forEach(serialNumber -> stowProcessor.createStowForReceipt(receipt, 1, user.getUserId(), expDate, dom, serialNumber));

    receipt.stowsCreated(receivedTotal, user.getUserId());
    receiptService.save(receipt);
    //update receipt in session
    receivingWorkflowContainer.setReceipt(receipt);

    val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.RECEIVING_LOCATION);

    request.getSession().setAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib(), receivingWorkflowContainer);

    return response;
  }

  @PostMapping("/addSerial")
  public SpaPostResponse addSerial(ReceivingControlledNiinDetailsInput receivingControlledNiinDetailsInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }

      val receivingWorkflowContainer = (ReceivingWorkflowContainer) request.getSession().getAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());

      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), null);

      val serial = receivingControlledNiinDetailsInput.getSerial();

      if (!RegUtils.isAlphaNumericDashes(serial)) {
        spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        spaPostResponse.addWarning(serial + " is invalid.  Must be alphanumeric (Dashes allowed)");
        return spaPostResponse;
      }

      val receipt = receivingWorkflowContainer.getReceipt();
      String errorCode = validateSerial(receipt, serial);

      if (!StringUtils.isEmpty(errorCode)) {
        errorCode = "The Serial Number Entered was found to be a duplicate.";
        spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        spaPostResponse.addWarning(errorCode);
      }

      return spaPostResponse;
    }
    catch (Exception e) {
      log.error("Error occurred processing Inventory add Serial Number", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  private String validateSerial(Receipt receipt, String serial) {
    ReceiptAMImpl receiptAM = SpringAdfBindingUtils.getReceiptAM();
    return receiptAM.checkSerial(Integer.toString(receipt.getRcn()), serial, "", Integer.toString(receipt.getNiinInfo().getNiinId()))
        ;
  }
}
