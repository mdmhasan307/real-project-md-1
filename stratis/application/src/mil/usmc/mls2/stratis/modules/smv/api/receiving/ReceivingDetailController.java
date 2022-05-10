package mil.usmc.mls2.stratis.modules.smv.api.receiving;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.stratis.common.util.JNDIUtils;
import mil.stratis.common.util.Util;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.processor.ReceiptSuffixProcessor;
import mil.usmc.mls2.stratis.core.processor.StowProcessor;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

@Slf4j
@RestController
@RequestMapping(value = "/mobile/receiving/detail")
@RequiredArgsConstructor
public class ReceivingDetailController {

  private final GlobalConstants globalConstants;
  private static final String DEFAULT_PAGE = "mobile/receiving/detail";

  private final NiinInfoService niinInfoService;
  private final ReceiptService receiptService;
  private final RefDasfService dasfService;
  private final ErrorQueueService errorQueueService;
  private final ErrorService errorService;
  private final StowProcessor stowProcessor;
  private final ReceiptSuffixProcessor receiptSuffixProcessor;
  private final DateService dateService;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {

    if (!SMVUtility.authenticated(request.getSession()))
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);

    val receivingWorkflowContainer = (ReceivingWorkflowContainer) request.getSession().getAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());

    request.setAttribute("receipt", receivingWorkflowContainer.getReceipt());
    request.setAttribute("receivingDetailInput", new ReceivingDetailInput(receivingWorkflowContainer));
    request.setAttribute("domRequired", receivingWorkflowContainer.getReceipt().getNiinInfo().getShelfLifeCode() != null);

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Receiving Detail");
  }

  @PostMapping
  public SpaPostResponse post(ReceivingDetailInput receivingDetailInput, HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());

      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.RECEIVING_DETAIL);
      ReceivingWorkflowContainer receivingWorkflowContainer = (ReceivingWorkflowContainer) request.getSession().getAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib());
      val receipt = receivingWorkflowContainer.getReceipt();
      //generate suffix here
      String suffix = null;
      if ("yes".equalsIgnoreCase(receivingDetailInput.getChkPartialShipment()) && receipt.getSuffix() == null) {
        try {
          suffix = receiptSuffixProcessor.generateSuffix(receivingDetailInput.getDocNumber());
        }
        catch (StratisRuntimeException e) {
          log.error("Error generating suffix", e);
          response.addWarning(e.getMessage());
          return response;
        }
      }
      else if (receipt.getSuffix() != null) {
        suffix = receipt.getSuffix();
      }

      val preSaveValid = preSaveValidation(receivingDetailInput, response, suffix);

      if (!preSaveValid) {
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }

      receipt.setDetails(receivingDetailInput, user.getUserId(), suffix);
      receiptService.save(receipt);

      Receipt newReceipt;
      val receiptSearch = ReceiptSearchCriteria.fromReceipt(receipt);
      val receipts = receiptService.search(receiptSearch);

      if (!receipts.isEmpty()) newReceipt = receipts.get(0);
      else throw new StratisRuntimeException("Failed to find saved receipt");

      receivingWorkflowContainer.setDetailsData(newReceipt, receivingDetailInput);
      request.getSession().setAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib(), receivingWorkflowContainer);

      val postValid = postsaveValidation(receivingDetailInput, receivingWorkflowContainer, newReceipt, response, user);
      if (!postValid) {
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        return response;
      }

      stowProcessor.deleteStowsIfExistsForReceipt(newReceipt);

      if ("Y".equalsIgnoreCase(newReceipt.getNiinInfo().getSerialControlFlag())) {
        //serial
        //stows will be created in controlled niin page
        response.setRedirectUrl(MappingConstants.RECEIVING_CONTROLLED_NIIN);
      }
      else {
        //location
        int inducted = receipt.getQuantityInvoiced();
        int released = receipt.getQuantityReleased();
        int stowed = receipt.getQuantityStowed();

        var qtyToBeStowed = inducted - released - stowed;
        val expDate = receivingDetailInput.getExpDate();
        val dom = receivingDetailInput.getDom();
        stowProcessor.createStowForReceipt(newReceipt, qtyToBeStowed, user.getUserId(), expDate, dom, null);
        response.setRedirectUrl(MappingConstants.RECEIVING_LOCATION);
      }
      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing Add Container submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  private boolean preSaveValidation(ReceivingDetailInput input, SpaPostResponse response, String suffix) throws SQLException {

    if (input.getRi().isEmpty()) return validationError("RI is required.", response);
    if (input.getRi().length() != 3) return validationError("Please enter valid RI to send charges.", response);
    if (!Arrays.asList("A", "F").contains(input.getCc()))
      return validationError(String.format("The CC %s for this Receipt is not supported by STRATIS.", input.getCc()), response);

    if (null != input.getQtyReceived() && input.getQtyReceived() > 99999)
      return validationError("Qty Received must be 1 to 5 characters numeric", response);

    if (null != input.getQtyInducted() && input.getQtyInducted() > 99999)
      return validationError("Qty Inducted must be 1 to 5 characters numeric", response);

    try {
      val documentNumber = input.getDocNumber().substring(0, 14);
      val niin = input.getNsn().substring(4);
      val quantityReceived = input.getQtyReceived();
      val gcssmcTransactionsService = SpringAdfBindingUtils.getWorkloadManagerService().getGCSSMCTransactionsService();
      val onDASF = Util.isNotEmpty(gcssmcTransactionsService.isDASF(documentNumber, niin, quantityReceived).refDasfId);

      if (onDASF && gcssmcTransactionsService.checkDASFDuplicate(documentNumber, suffix) ||
          !onDASF && gcssmcTransactionsService.checkNonDASFDuplicate(documentNumber, suffix))
        return validationError("Duplicate D6 transaction found.", response);

      if (onDASF) {
        val quantityDue = dasfService.search(RefDasfSearchCriteria.builder().documentNumber(input.getDocNumber()).build())
            .stream()
            .map(RefDasf::getQuantityDue)
            .findFirst();

        if (quantityDue.isPresent() && quantityDue.get() < input.getQtyReceived() && !"Override".equalsIgnoreCase(input.getDasfQtyOverride())) {
          val lookup = JNDIUtils.getInstance();
          val stringSetting = lookup.getProperty("stratis.dasf.overage.split");
          if ("false".equalsIgnoreCase(stringSetting)) {
            return validationError("The quantity being received is greater than the quantity on the DASF - please confirm the correct quantity.", response);
          }
          else {
            response.addFlag("dasfQty");
            return false;
          }
        }
      }
      val sdf = new SimpleDateFormat(DateConstants.DEFAULT_INPUT_DATE_FORMAT);
      val manuDate = input.getDom();
      val expDate = input.getExpDate();
      if (!expDate.isEmpty() && !dateService.isValidDate(expDate)) return validationError("Expiration Date is invalid.", response);
      if (!manuDate.isEmpty() && !dateService.isValidDate(manuDate)) return validationError("Manufacture Date is invalid.", response);
      if (!manuDate.isEmpty()) {
        //check if Manufacture date is after current date
        val manCal = Calendar.getInstance();
        val curCal = Calendar.getInstance();

        manCal.setTime(sdf.parse(manuDate));
        if (curCal.before(manCal)) return validationError("Manufacture Date is after today.", response);
      }

      val nsn = input.getNsn().substring(4);
      val niinInfo = niinInfoService.search(
          NiinInfoSearchCriteria.builder()
              .niin(nsn)
              .build())
          .stream()
          .findFirst();

      val refSlc = niinInfo.map(NiinInfo::getShelfLifeCode);
      val spanDays = refSlc.map(RefSlc::getSpanDays);
      val shelfLifeCode = refSlc.map(RefSlc::getShelfLifeCode);
      // NIINs with a nonzero shelf life code requires either a manufacture or expiration date
      if (shelfLifeCode.isPresent()
          && !shelfLifeCode.get().equalsIgnoreCase("0")
          && manuDate.isEmpty())
        return validationError("Manufacture date entry required.", response);

      // Manufacture date overwrites expiration date if set
      if (!manuDate.isEmpty() && spanDays.isPresent()) {
        val manCal = Calendar.getInstance();
        manCal.setTime(sdf.parse(manuDate));
        manCal.add(Calendar.DATE, spanDays.get());
        input.setExpDate(sdf.format(manCal.getTime()));
      }
      else input.setExpDate("99990101");

      return true;
    }
    catch (ParseException e) {
      return validationError("Manufacture Date is invalid.", response);
    }
  }

  private boolean postsaveValidation(ReceivingDetailInput receivingDetailInput, ReceivingWorkflowContainer receivingWorkflowContainer, Receipt receipt, SpaResponse response, UserInfo user) {
    //check for ui error
    if (receivingWorkflowContainer.isUiError()) {
      //add error record
      Error uiError = errorService.findByCode("RECEIPT_UI_NCON");
      if (uiError != null) {
        val result = processPostValidation(uiError,
            receipt,
            response,
            user,
            receivingWorkflowContainer.isUiError(),
            "Unable to convert unit of issue. See Supervisor.",
            false);
        if (!result)
          return result;
      }
    }

    Error receiptFscError = errorService.findByCode("RECEIPT_FSC");
    if (receiptFscError != null) {
      val result = processPostValidation(receiptFscError,
          receipt,
          response,
          user,
          !receipt.getFsc().equalsIgnoreCase(receipt.getNiinInfo().getFsc()),
          "Change FSC " + receipt.getFsc() + " to " + receipt.getNiinInfo().getFsc() + ".",
          true);
      if (!result)
        return result;
    }

    //Following is the Quantity Receipt validation
    // first check if there is an error row
    Error qtyRecivedError = errorService.findByCode("RECEIPT_QTYRECV");
    if (qtyRecivedError != null) {
      val result = processPostValidation(qtyRecivedError,
          receipt,
          response,
          user,
          receivingDetailInput.getQtyReceived() <= 0 || receipt.getQuantityInvoiced() != receivingDetailInput.getQtyReceived(),
          "Received quantity discrepancy. Correct quantity or click Submit to continue.",
          true);
      if (!result)
        return result;
    }

    //Following is the Quantity Inducted validation
    // first check if there is an error row
    Error qtyInducedError = errorService.findByCode("RECEIPT_QTYINDUC");
    if (qtyInducedError != null) {
      val result = processPostValidation(qtyInducedError,
          receipt,
          response,
          user,
          receivingDetailInput.getQtyInducted() <= 0 || !receivingDetailInput.getQtyInducted().equals(receivingDetailInput.getQtyReceived()),
          "Quantity Received must match Quantity Inducted.",
          false);
      if (!result)
        return result;
    }

    return true;
  }

  private boolean processPostValidation(Error error, Receipt receipt, SpaResponse response, UserInfo user, boolean validationResult, String message, boolean returnFalseOnlyOnErrCount0) {
    //check if the error record already exists
    val errSearch = ErrorQueueCriteria.builder()
        .eid(error.getId())
        .keyType("RCN")
        .keyNum(Integer.toString(receipt.getRcn())).build();
    val errCount = errorQueueService.count(errSearch);

    if (validationResult) {
      //add error record if the error doesn't already exists
      if (errCount == 0) {
        //If not add it
        stowProcessor.saveErrorForReceipt(error, receipt, user);
      }
      if (!returnFalseOnlyOnErrCount0 || errCount == 0) {
        response.addWarning(message);
        return false;
      }
    }
    else if (errCount > 0) {
      stowProcessor.deleteErrorRecordsForReceipt(errSearch);
    }
    return true;
  }

  private boolean validationError(String message, SpaResponse response) {
    log.warn(message);
    response.addWarning(message);
    return false;
  }
}
