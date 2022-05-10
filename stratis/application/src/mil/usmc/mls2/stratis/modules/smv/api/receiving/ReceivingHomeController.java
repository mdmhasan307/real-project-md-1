package mil.usmc.mls2.stratis.modules.smv.api.receiving;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.StringUtil;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.service.gcss.I136NiinService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import mil.usmc.mls2.stratis.modules.smv.validation.MobileViewValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping(value = "/mobile/receiving")
@RequiredArgsConstructor
public class ReceivingHomeController {

  private final GlobalConstants globalConstants;
  private static final String DEFAULT_PAGE = "mobile/receiving/home";

  private final ReceiptHistoryService receiptHistoryService;
  private final ReceiptService receiptService;
  private final NiinInfoService niinInfoService;
  private final RefMhifService refMhifService;
  private final RefUiService refUiService;
  private final I136NiinService i136NiinService;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession()))
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);

    request.setAttribute("receivingHomeInput", ReceivingHomeInput.builder().build());

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Receiving");
  }

  @PostMapping
  public SpaPostResponse post(ReceivingHomeInput receivingHomeInput, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val response = SMVUtility.processPostResponse(request.getSession(), MappingConstants.RECEIVING_DETAIL);

      val validLength = validateLength(receivingHomeInput, response);
      val validDocumentNumber = validLength && checkDuplicateDocNumber(receivingHomeInput, response);
      var validBarcode = false;
      OneDBarcode barcode = null;

      if (validLength) {
        barcode = new OneDBarcode(receivingHomeInput.getBarcode(), response, false);
        validBarcode = Stream.of(barcode.getValidators())
            .flatMap(Collection::stream)
            .map(MobileViewValidator::validate)
            .reduce(true, (accumulator, isValid) -> !isValid ? isValid : accumulator);
      }

      if (!validLength || !validDocumentNumber || !validBarcode || barcode == null) {
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        response.setRedirectUrl(MappingConstants.RECEIVING_HOME);
        return response;
      }
      val docNumber = receivingHomeInput.getDocNumber().substring(0, 14);
      var uiError = false;
      val niinInfo = retrieveNiinInfo(receivingHomeInput, response);

      if (niinInfo == null) {
        response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        response.setRedirectUrl(MappingConstants.RECEIVING_HOME);
        return response;
      }

      val priceStr = barcode.getUp();
      val bldPrice = priceStr.substring(0, priceStr.length() - 2) + "." + priceStr.substring(priceStr.length() - 2);
      val receipt = Receipt.builder()
          .status("RECEIPT DRAFT")
          .partialShipmentIndicator("on".equalsIgnoreCase(receivingHomeInput.getChkPartialShipment()) ? "Y" : "N").documentID("A5A") //Default to A5A
          .documentNumber(docNumber)
          .fsc(receivingHomeInput.getNsn().substring(0, 4))
          .niinInfo(niinInfo)
          .routingId(barcode.getRic())
          .cc(barcode.getCc())
          .ui(barcode.getUi())
          .quantityInvoiced(Integer.parseInt(barcode.getQty()))
          .price(Double.parseDouble(bldPrice))
          //FUTURE INNOV Backlog: the following are defaulted in the db, but Hibernate doesn't utilize default column values
          .pc("A").cognizanceCode("AA").mechNonMechFlag("Y").ration("N").partialShipment("N")
          .build();

      Boolean conversionResult = false;
      if (!receipt.getUi().equalsIgnoreCase(niinInfo.getUi())) {
        conversionResult = refUiService.convertReceiptUI(niinInfo.getUi(), receipt.getUi(), receipt);
        if (null != conversionResult && conversionResult.equals(true)) {
          response.addNotification("Repack inventory to <" + receipt.getQuantityInvoiced() + " " + niinInfo.getUi() + "> before Stowing.");
        }
      }
      if (conversionResult != null) {
        receipt.addConversionResult(niinInfo);
      }
      else {
        //there is not conversion let the next page know
        uiError = true;
      }

      val receivingWorkflowContainer = ReceivingWorkflowContainer.builder()
          .receivingHomeInput(receivingHomeInput)
          .uiError(uiError)
          .receipt(receipt).build();

      request.getSession().setAttribute(globalConstants.getReceivingWorkflowContainerSessionAttrib(), receivingWorkflowContainer);

      return response;
    }
    catch (Exception e) {
      log.error("Error occurred processing receiving home submit post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, e.getMessage());
    }
  }

  private boolean validateLength(ReceivingHomeInput input, SpaPostResponse spaPostResponse) {
    boolean validationWarningsFound = false;

    if (StringUtil.isEmpty(input.getDocNumber())) {
      validationWarningsFound = true;
      spaPostResponse.addWarning("Document Number is required.");
    }
    if (StringUtil.isEmpty(input.getNsn())) {
      validationWarningsFound = true;
      spaPostResponse.addWarning("NSN is required.");
    }
    if (StringUtil.isEmpty(input.getBarcode())) {
      validationWarningsFound = true;
      spaPostResponse.addWarning("RIC/UI/QTY/CC/UP is required.");
    }

    if (!validationWarningsFound) {
      if (input.getBarcode().split(" {2}").length != 2) {
        validationWarningsFound = true;
        spaPostResponse.addWarning("Barcode formatted improperly. Please use 2 spaces to separate RIC/UI/QTY/CC/UP.");
      }
      if (input.getDocNumber().length() < 14 || RegUtils.isNotAlphaNumeric(input.getDocNumber())) {
        validationWarningsFound = true;
        spaPostResponse.addWarning("Document Number is invalid.");
      }
      if (input.getNsn().length() < 13 || RegUtils.isNotAlphaNumeric(input.getNsn())) {
        validationWarningsFound = true;
        spaPostResponse.addWarning("NSN is invalid.");
      }
      if (input.getBarcode().length() < 11 || RegUtils.isNotAlphaNumericPlusSpace(input.getBarcode())) {
        validationWarningsFound = true;
        spaPostResponse.addWarning("RIC/UI/QTY/CC/UP is invalid.");
      }
    }
    if (validationWarningsFound) spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
    return !validationWarningsFound;
  }

  private boolean checkDuplicateDocNumber(ReceivingHomeInput receivingHomeInput, SpaPostResponse spaPostResponse) {
    val docNumber = receivingHomeInput.getDocNumber().substring(0, 14);

    val receiptCriteria = ReceiptSearchCriteria.builder()
        .documentNumber(docNumber)
        .documentId("YLL").documentIdMatch(false)
        .status("*COMPLETE*")
        .build();
    val receiptHistoryCriteria = ReceiptHistorySearchCriteria.builder()
        .documentNumber(docNumber)
        .documentId("YLL").documentIdMatch(false)
        .status("*COMPLETE*")
        .build();
    if (receivingHomeInput.getDocNumber().length() > 14) {
      receiptCriteria.setSuffix(receivingHomeInput.getDocNumber().substring(14, 15));
      receiptHistoryCriteria.setSuffix(receivingHomeInput.getDocNumber().substring(14, 15));
    }
    else {
      receiptCriteria.setCheckSuffixNull(true);
      receiptHistoryCriteria.setCheckSuffixNull(true);
    }

    val currentCount = receiptService.count(receiptCriteria);
    val historyCount = receiptHistoryService.count(receiptHistoryCriteria);
    if ((currentCount + historyCount) > 0 && !"on".equalsIgnoreCase(receivingHomeInput.getChkPartialShipment())) {
      spaPostResponse.addWarning("Doc Number " + receivingHomeInput.getDocNumber() + " previously processed. Confirm and submit selecting partial shipment option.");
      spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }
    return true;
  }

  private NiinInfo retrieveNiinInfo(ReceivingHomeInput receivingHomeInput, SpaPostResponse spaPostResponse) {

    val niin = receivingHomeInput.getNsn().substring(4, 13);
    NiinInfo niinInfo = null;

    //search in niin_info for the entered niin
    var niinFound = retrieveNiins(niin);
    var sentRequest = false;
    if (niinFound.isPresent()) {
      niinInfo = niinFound.get();
      return niinInfo;
    }
    //no niin found search in mhif ref
    Set<RefMhif> mhifs = retrieveRefMhifs(niin);
    if (CollectionUtils.isEmpty(mhifs)) {
      //call service
      val result = i136NiinService.processI136Niin(niin);
      if (!result.status().isSuccess()) {
        spaPostResponse.addWarning("NIIN " + niin + " not recognized in system. Immediate MHIF request sent to host.");
        sentRequest = true;
      }
    }
    else {
      RefMhif mhif = mhifs.iterator().next();
      //insert niin record and get the id
      NiinInfo newNiin = NiinInfo.builder()
          .niin(niin)
          .fsc(mhif.getRecordFsc())
          .ui(mhif.getUnitOfIssue())
          .price(mhif.getUnitPrice())
          .nomenclature(mhif.getItemNameNomenclature())
          .inventoryThreshold("N").build();
      niinInfoService.save(newNiin);
    }

    niinFound = retrieveNiins(niin);
    if (niinFound.isPresent()) {
      //success check niin info again
      niinInfo = niinFound.get();
    }
    else if (!sentRequest) {
      spaPostResponse.addWarning("NIIN " + niin + " not recognized in system.");
    }
    return niinInfo;
  }

  private Optional<NiinInfo> retrieveNiins(String niin) {
    return niinInfoService.findByNiin(niin);
  }

  private Set<RefMhif> retrieveRefMhifs(String niin) {
    RefMhifSearchCriteria mhifSearch = RefMhifSearchCriteria.builder().recordNiin(niin).build();
    return refMhifService.search(mhifSearch);
  }
}
