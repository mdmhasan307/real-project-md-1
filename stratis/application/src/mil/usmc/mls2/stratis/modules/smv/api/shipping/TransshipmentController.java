package mil.usmc.mls2.stratis.modules.smv.api.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.stratis.common.util.StringUtil;
import mil.stratis.common.util.Util;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import mil.usmc.mls2.stratis.modules.smv.validation.MobileViewValidator;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants.SHIPPING_HOME;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.SHIPPING_TRANSSHIPMENT)
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class TransshipmentController {

  private static final String DEFAULT_PAGE = MappingConstants.SHIPPING_TRANSSHIPMENT + "/home";
  private static final String BY_TCN_PAGE = MappingConstants.SHIPPING_TRANSSHIPMENT + "/tcn";
  private static final String BY_DOCUMENT_PAGE = MappingConstants.SHIPPING_TRANSSHIPMENT + "/document";
  private static final String BY_CONTRACT_PAGE = MappingConstants.SHIPPING_TRANSSHIPMENT + "/contract";
  private static final String TRANSSHIPMENT_INPUT = "transshipmentInput";

  private final GlobalConstants globalConstants;
  private final EquipmentService equipmentService;

  @GetMapping
  public SpaGetResponse showTransshipment(HttpServletRequest request) {

    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Shipping - Transshipment");
  }

  @GetMapping("/tcn")
  public SpaGetResponse showByTcn(HttpServletRequest request) {

    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    request.setAttribute(TRANSSHIPMENT_INPUT, TransshipmentInput.builder().build());
    return SMVUtility.processGetResponse(request, BY_TCN_PAGE, "Shipping - Transship By TCN");
  }

  @GetMapping("/document")
  public SpaGetResponse showByDocument(HttpServletRequest request) {

    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    request.setAttribute(TRANSSHIPMENT_INPUT, TransshipmentInput.builder().build());
    return SMVUtility.processGetResponse(request, BY_DOCUMENT_PAGE, "Shipping - Transship By Document");
  }

  @GetMapping("/contract")
  public SpaGetResponse showByContract(HttpServletRequest request) {

    val conditionCodes = Arrays.asList(
        new DefaultKeyValue<>("A", "Serviceable"),
        new DefaultKeyValue<>("F", "Unserviceable"));

    request.setAttribute("conditionCodes", conditionCodes);

    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    request.setAttribute(TRANSSHIPMENT_INPUT, TransshipmentInput.builder().build());
    return SMVUtility.processGetResponse(request, BY_CONTRACT_PAGE, "Shipping - Transship By Contract");
  }

  @PostMapping("/tcn")
  public SpaPostResponse submitByTcn(TransshipmentInput transshipmentInput, HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val response = SMVUtility.processPostResponse(request.getSession(), null);
      val success = handleTransshipment(TransshipType.TCN, transshipmentInput, request, response);

      return handleResult(response, success, BY_TCN_PAGE);
    }
    catch (Exception e) {
      log.error("Error occurred processing transhipment by tcn", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping("/document")
  public SpaPostResponse submitByDocument(TransshipmentInput transshipmentInput, HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val response = SMVUtility.processPostResponse(request.getSession(), null);
      val success = handleTransshipment(TransshipType.DOCUMENT, transshipmentInput, request, response);

      return handleResult(response, success, BY_DOCUMENT_PAGE);
    }
    catch (Exception e) {
      log.error("Error occurred processing transhipment by document", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping("/contract")
  public SpaPostResponse submitByContract(TransshipmentInput transshipmentInput, HttpServletRequest request) {

    try {
      if (!SMVUtility.authenticated(request.getSession()))
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);

      val response = SMVUtility.processPostResponse(request.getSession(), null);
      val success = handleTransshipment(TransshipType.CONTRACT, transshipmentInput, request, response);

      return handleResult(response, success, BY_CONTRACT_PAGE);
    }
    catch (Exception e) {
      log.error("Error occurred processing transhipment by contract", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * Helper method to orchestrate transshipment logic
   */
  private boolean handleTransshipment(
      TransshipType type,
      TransshipmentInput transshipmentInput,
      HttpServletRequest request,
      SpaPostResponse response) {

    val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
    val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
    val derivedInput = processInputs(transshipmentInput, response);
    val isFormValid = validate(type, derivedInput, transshipmentInput, response);
    val shippingService = SpringAdfBindingUtils.getShippingService();

    if (!isFormValid) {
      log.warn("Transshipment failed validation.");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      return false;
    }

    log.debug(
        "Calling processTransshipment with derivedInput: [{}], userId: [{}], warehouseId: [{}]",
        derivedInput,
        user.getUserId(),
        workstation.getWarehouseId());

    val success = processTransshipment(derivedInput, user.getUserId(), workstation.getEquipmentNumber(), response);

    if (success && type != TransshipType.CONTRACT)
      shippingService.updateDASF(derivedInput.getDasfId(), Util.cleanInt(derivedInput.getQuantityReceived()));

    return success;
  }

  /**
   * Helper method for managing response back to the client
   */
  private SpaPostResponse handleResult(SpaPostResponse response, boolean success, String errorRedirect) {

    if (!success) {
      log.warn("Transshipment processed unsuccessfully.");
      response.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
      response.setRedirectUrl(errorRedirect);
    }
    else {
      log.info("Transshipment processed successfully.");
      response.setResult(SpaResponse.SpaResponseResult.SUCCESS);
      response.setRedirectUrl(SHIPPING_HOME);
      response.addNotification("Transshipment processed successfully.");
    }

    return response;
  }

  /**
   * Initializes inputs with default values if empty.
   * Also, looks up fields that can be derived from other inputs.
   */
  private TransshipmentInput processInputs(TransshipmentInput input, SpaPostResponse response) {

    val shippingService = SpringAdfBindingUtils.getShippingService();

    var documentNumber = input.getDocumentNumber();
    var aac = input.getAac();
    var ric = "";
    var ui = "EA";
    var quantityReceived = StringUtil.isEmpty(input.getQuantityReceived()) ? "1" : input.getQuantityReceived();
    var cc = "A";
    var pc = "A";
    var up = "";
    var route = "";
    OneDBarcode oneDBarcode = null;

    // Prevent index out of bounds
    // We handle real validation later
    if (input.getTcn().length() >= 14)
      documentNumber = StringUtil.isEmpty(input.getDocumentNumber())
          ? input.getTcn().substring(0, 14)
          : input.getDocumentNumber();

    // Prevent index out of bounds
    // We handle real validation later
    if (documentNumber.length() >= 6)
      aac = StringUtil.isEmpty(input.getAac())
          ? documentNumber.substring(0, 6)
          : input.getAac();

    // Parse "RIC/UI/QTY/CC/UP" combined field
    if (StringUtil.isNotEmpty(input.getBarcodeToParse())) {
      oneDBarcode = new OneDBarcode(input.getBarcodeToParse(), response, true);

      ric = StringUtil.trimUpperCaseClean(oneDBarcode.getRic());
      ui = StringUtil.isEmpty(oneDBarcode.getUi()) ? "EA" : StringUtil.trimUpperCaseClean(oneDBarcode.getUi());
      quantityReceived = StringUtil.isEmpty(oneDBarcode.getQty()) ? "1" : StringUtil.trimUpperCaseClean(oneDBarcode.getQty());
      cc = StringUtil.isEmpty(oneDBarcode.getCc()) ? "A" : StringUtil.trimUpperCaseClean(oneDBarcode.getCc());
      up = StringUtil.trimUpperCaseClean(oneDBarcode.getUp());
      route = StringUtil.trimUpperCaseClean(oneDBarcode.getRoute());
    }

    // Lookup derived fields
    val customerId = shippingService.getCustomerId(aac);
    val niin = shippingService.findNiinId(input.getNsn());
    val dasfId = shippingService.isDASF(documentNumber, "", Util.cleanInt(quantityReceived));

    return input.toBuilder()
        .aac(aac)
        .documentNumber(documentNumber)
        .ric(ric)
        .ui(ui)
        .quantityReceived(quantityReceived)
        .pc(pc)
        .cc(cc)
        .up(up)
        .customerId(customerId)
        .route(route)
        .niin(niin)
        .dasfId(dasfId)
        .oneDBarcode(oneDBarcode)
        .build();
  }

  /**
   * Main validation method.
   * Responsible for setting up and running validators.
   */
  private boolean validate(
      TransshipType type,
      TransshipmentInput derivedInput,
      TransshipmentInput originalInput,
      SpaPostResponse response) {

    List<MobileViewValidator> typeSpecificValidator;

    val commonValidators = createCommonValidators(derivedInput, response);

    switch (type) {
      case TCN:
        typeSpecificValidator = createTcnValidators(derivedInput, originalInput, response);
        break;
      case DOCUMENT:
        typeSpecificValidator = createDocumentValidators(derivedInput, originalInput, response);
        break;
      case CONTRACT:
        typeSpecificValidator = createContractValidators(derivedInput, response);
        break;
      default:
        throw new IllegalArgumentException(String.format("%s not supported.", type));
    }

    // Combine validators, call validate, and check if any validators return invalid
    return Stream.of(commonValidators, typeSpecificValidator)
        .flatMap(Collection::stream)
        .map(MobileViewValidator::validate)
        .reduce(true, (accumulator, isValid) -> !isValid ? isValid : accumulator);
  }

  /**
   * Generate validators common to all 3 transshipment types.
   * Also, run validation rules too specific to house in {@link MobileViewValidator}
   */
  private List<MobileViewValidator> createCommonValidators(TransshipmentInput input, SpaPostResponse response) {

    val nsn = MobileViewValidator.builder()
        .field(input.getNsn())
        .title("NSN")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(13)
        .maxLength(13)
        .isRequired(true)
        .specialRules(v -> {
          if (StringUtil.isEmpty(input.getNiin())) v.validationError("NSN not found in system.");
        })
        .build();

    val smic = MobileViewValidator.builder()
        .field(input.getSmic())
        .title("SMIC")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(2)
        .maxLength(2)
        .isRequired(true)
        .build();

    val barcode = MobileViewValidator.builder()
        .field(input.getBarcode())
        .title("Barcode")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(5)
        .maxLength(10)
        .isRequired(true)
        .build();

    val trackingNumber = MobileViewValidator.builder()
        .field(input.getTrackingNumber())
        .title("Tracking Number")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(10)
        .maxLength(18)
        .isRequired(true)
        .build();

    val suffix = MobileViewValidator.builder()
        .field(input.getSuffix())
        .title("Suffix")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(1)
        .maxLength(1)
        .build();

    return Arrays.asList(nsn, smic, barcode, trackingNumber, suffix);
  }

  /**
   * Generate validators for trnasshipment by tcn.
   * Also, run validation rules too specific to house in {@link MobileViewValidator}
   */
  private List<MobileViewValidator> createTcnValidators(
      TransshipmentInput derivedInput,
      TransshipmentInput originalInput,
      SpaPostResponse response) {

    val shippingService = SpringAdfBindingUtils.getShippingService();

    val tcn = MobileViewValidator.builder()
        .field(derivedInput.getTcn())
        .title("TCN")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(17)
        .maxLength(17)
        .isRequired(true)
        .cannotStartWithY(true)
        .specialRules(v -> {
          if (shippingService.existTCN(v.getField())) v.validationError("TCN already exists.");
          else if (StringUtil.isEmpty(derivedInput.getDasfId()))
            v.validationError("Item not on any DASF, Take gear to Receiving.");
        })
        .build();

    val documentNumber = MobileViewValidator.builder()
        .field(derivedInput.getDocumentNumber())
        .title("Document Number")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(14)
        .maxLength(14)
        .cannotStartWithY(true)
        .build();

    val aac = MobileViewValidator.builder()
        .field(derivedInput.getAac())
        .title("Consignee")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(6)
        .maxLength(6)
        .cannotStartWithY(true)
        .specialRules(v -> {
          if (StringUtil.isEmpty(derivedInput.getCustomerId())) {
            if (StringUtil.isEmpty(originalInput.getAac()))
              v.validationError(String.format("1st 6 characters of TCN (%s) not a valid customer. Either enter a valid customer in the TCN or as the Consignee.", v.getField()));
            else v.validationError(String.format("AAC (%s) is not a valid customer.", v.getField()));
          }
          else if (shippingService.isCustomerRestricted(Integer.parseInt(derivedInput.getCustomerId())))
            v.validationError(MobileViewValidator.getAACValidationErrorMessage(v.getField()));
        })
        .build();

    final List<MobileViewValidator> barcode = null != derivedInput.getOneDBarcode()
        ? derivedInput.getOneDBarcode().getValidators()
        : Collections.emptyList();

    return Stream.of(Arrays.asList(tcn, documentNumber, aac), barcode)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  /**
   * Generate validators for transshipment by document.
   * Also, run validation rules too specific to house in {@link MobileViewValidator}
   */
  private List<MobileViewValidator> createDocumentValidators(
      TransshipmentInput derivedInput,
      TransshipmentInput originalInput,
      SpaPostResponse response) {

    val shippingService = SpringAdfBindingUtils.getShippingService();
    val documentNumber = MobileViewValidator.builder()
        .field(derivedInput.getDocumentNumber())
        .title("Document Number")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(14)
        .maxLength(14)
        .isRequired(true)
        .cannotStartWithY(true)
        .specialRules(v -> {
          if (StringUtil.isEmpty(derivedInput.getDasfId()))
            v.validationError("Item not on any DASF, Take gear to Receiving.");
        })
        .build();

    val aac = MobileViewValidator.builder()
        .field(derivedInput.getAac())
        .title("AAC")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(6)
        .maxLength(6)
        .cannotStartWithY(true)
        .specialRules(v -> {
          if (StringUtil.isEmpty(derivedInput.getCustomerId())) {
            if (StringUtil.isEmpty(originalInput.getAac()))
              v.validationError(String.format("1st 6 characters of Document Number [%s] not a valid customer. Either enter a valid customer in the Document Number or as the AAC.", v.getField()));
            else
              v.validationError(String.format("AAC (%s) not a valid customer.", v.getField()));
          }
          else if (shippingService.isCustomerRestricted(Integer.parseInt(derivedInput.getCustomerId())))
            v.validationError(MobileViewValidator.getAACValidationErrorMessage(v.getField()));
        })
        .build();

    final List<MobileViewValidator> barcode = null != derivedInput.getOneDBarcode()
        ? derivedInput.getOneDBarcode().getValidators()
        : Collections.emptyList();

    return Stream.of(Arrays.asList(documentNumber, aac), barcode)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  /**
   * Generate validators for transshipment by contract.
   * Also, run validation rules too specific to house in {@link MobileViewValidator}
   */
  private List<MobileViewValidator> createContractValidators(TransshipmentInput input, SpaPostResponse response) {

    val shippingService = SpringAdfBindingUtils.getShippingService();

    val contractNumber = MobileViewValidator.builder()
        .field(input.getContractNumber())
        .title("Contract Number")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(1)
        .maxLength(13)
        .isRequired(true)
        .build();

    val qtyInvoiced = MobileViewValidator.builder()
        .field(input.getQuantityInvoiced())
        .title("Quantity Invoiced")
        .type(MobileViewValidator.CharacterType.NUMERIC)
        .response(response)
        .minLength(1)
        .maxLength(5)
        .isRequired(true)
        .build();

    val qtyReceived = MobileViewValidator.builder()
        .field(input.getQuantityReceived())
        .title("Quantity Received")
        .type(MobileViewValidator.CharacterType.NUMERIC)
        .response(response)
        .minLength(1)
        .maxLength(5)
        .isRequired(true)
        .build();

    val shipmentNumber = MobileViewValidator.builder()
        .field(input.getShipmentNumber())
        .title("Shipment Number")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(7)
        .maxLength(7)
        .isRequired(true)
        .build();

    val billAmount = MobileViewValidator.builder()
        .field(input.getBillAmount())
        .title("Bill Amount")
        .type(MobileViewValidator.CharacterType.DECIMAL)
        .response(response)
        .build();

    val documentNumber = MobileViewValidator.builder()
        .field(input.getDocumentNumber())
        .title("Document Number")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(14)
        .maxLength(14)
        .cannotStartWithY(true)
        .build();

    val aac = MobileViewValidator.builder()
        .field(input.getAac())
        .title("AAC")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(6)
        .maxLength(6)
        .isRequired(true)
        .cannotStartWithY(true)
        .specialRules(v -> {
          if (StringUtil.isEmpty(input.getCustomerId()))
            v.validationError(String.format("AAC (%s) not a valid customer.", v.getField()));
          else if (shippingService.isCustomerRestricted(Integer.parseInt(input.getCustomerId())))
            v.validationError(MobileViewValidator.getAACValidationErrorMessage(v.getField()));
        })
        .build();

    val pc = MobileViewValidator.builder()
        .field(input.getPc())
        .title("PC")
        .type(MobileViewValidator.CharacterType.ALPHA)
        .response(response)
        .minLength(1)
        .maxLength(1)
        .build();

    val callNumber = MobileViewValidator.builder()
        .field(input.getCallNumber())
        .title("Call Number")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(5)
        .maxLength(5)
        .build();

    val lineNumber = MobileViewValidator.builder()
        .field(input.getLineNumber())
        .title("Line Number")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(5)
        .maxLength(5)
        .build();

    return Arrays.asList(
        contractNumber,
        qtyInvoiced,
        qtyReceived,
        shipmentNumber,
        billAmount,
        documentNumber,
        aac,
        pc,
        callNumber,
        lineNumber);
  }

  /**
   * Wrapper method for {@link mil.stratis.model.services.ShippingServiceImpl#transship(HashMap, int, int)}.
   */
  private boolean processTransshipment(
      TransshipmentInput input,
      Integer userId,
      Integer workstationId,
      SpaPostResponse response) {

    val shippingService = SpringAdfBindingUtils.getShippingService();

    // Pipe inputs into a hashmap to facilitate calling legacy transship method
    val map = new HashMap<String, String>();
    map.put("contract_number", input.getContractNumber());
    map.put("shipment_number", input.getShipmentNumber());
    map.put("tcn", input.getTcn());
    map.put("nsn", input.getNsn());
    map.put("niin_id", input.getNiin());
    map.put("smic", input.getSmic());
    map.put("barcode", input.getBarcode());
    map.put("tracking_number", input.getTrackingNumber());
    map.put("ricFrom", input.getRic());
    map.put("route_id", input.getRoute());
    map.put("ui", input.getUi());
    map.put("quantity", input.getQuantityReceived());
    map.put("quantity_invoiced", input.getQuantityInvoiced());
    map.put("pc", input.getPc());
    map.put("cc", input.getCc());
    map.put("price", input.getUp());
    map.put("aac", input.getAac());
    map.put("customer_id", input.getCustomerId());
    map.put("document_number", input.getDocumentNumber());
    map.put("suffix", input.getSuffix());
    map.put("call_number", input.getCallNumber());
    map.put("line_number", input.getLineNumber());
    map.put("billed_amount", input.getBillAmount());
    map.put("tailgate_date", input.getTailDate());
    map.put("user_id", userId.toString());
    map.put("workstation_id", workstationId.toString());

    val message = shippingService.transship(map, userId, workstationId);

    // Check for any errors from legacy transship method
    if (message.length() > 0) {
      log.error(message.toString());
      response.addWarning(message.toString());
      return false;
    }

    return true;
  }

  enum TransshipType {
    TCN,
    DOCUMENT,
    CONTRACT
  }
}
