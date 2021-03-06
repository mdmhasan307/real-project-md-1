package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Value;
import lombok.val;
import mil.stratis.common.util.StringUtil;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.validation.MobileViewValidator;

import java.util.Arrays;
import java.util.List;

/**
 * Capture the parsing and validation of the 1DBarcode (RIC/UI/QTY/CC/UP)
 * in a central class so that it can be used in various places throughout
 * the project.
 */
@Value
public class OneDBarcode {

  String barcode;
  String firstPart;
  String ric;
  String ui;
  String qty;
  String cc;
  String up;
  String route;
  List<MobileViewValidator> validators;

  /**
   * Constructor handles parsing the 1DBarcode and sets up the validators.
   * Also, by declaring a constructor it disables the all args constructor
   * generated by Lombok making this the only way to create this class.
   */
  public OneDBarcode(String barcodeToParse, SpaPostResponse response, boolean validateRic) {

    val shippingService = SpringAdfBindingUtils.getShippingService();
    val fields = barcodeToParse.split(" {2}");

    if (fields.length != 2)
      throw new StratisRuntimeException("1DBarcode formatted improperly. Please use 2 spaces to separate RIC/UI/QTY/CC and UP.");

    barcode = barcodeToParse;
    firstPart = fields[0];
    ric = fields[0].substring(0, 3);
    ui = fields[0].substring(3, 5);
    qty = fields[0].substring(5, 10);
    cc = fields[0].substring(10, 11);
    up = fields[1];
    route = shippingService.getRoutingId(ric);

    validators = createValidators(response, validateRic);
  }

  /**
   * Build all validators and return them in a list.
   * This gives the calling class a little control over
   * how to handle these validators. Generally, the caller will
   * iterate over the list and call the validator's {@link MobileViewValidator#validate()}
   * method.
   */
  private List<MobileViewValidator> createValidators(SpaPostResponse response, boolean validateRic) {

    val barcodeValidator = MobileViewValidator.builder()
        .field(barcode)
        .title("1DBarcode")
        .response(response)
        .minLength(20)
        .maxLength(24)
        .isRequired(true)
        .build();

    val firstPartValidator = MobileViewValidator.builder()
        .field(firstPart)
        .title("RIC/UI/QTY/CC")
        .response(response)
        .minLength(11)
        .maxLength(11)
        .isRequired(true)
        .build();

    val ricValidator = MobileViewValidator.builder()
        .field(ric)
        .title("RIC")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(3)
        .maxLength(3)
        .isRequired(true)
        .specialRules(validator -> {
          if (validateRic && StringUtil.isNotEmpty(ric) && StringUtil.isEmpty(route))
            validator.validationError("RIC not a valid route");
        })
        .build();

    val uiValidator = MobileViewValidator.builder()
        .field(ui)
        .title("UI")
        .type(MobileViewValidator.CharacterType.ALPHANUMERIC)
        .response(response)
        .minLength(2)
        .maxLength(2)
        .isRequired(true)
        .build();

    val qtyValidator = MobileViewValidator.builder()
        .field(qty)
        .title("Quantity Received")
        .type(MobileViewValidator.CharacterType.NUMERIC)
        .response(response)
        .minLength(5)
        .maxLength(5)
        .isRequired(true)
        .build();

    val ccValidator = MobileViewValidator.builder()
        .field(cc)
        .title("CC")
        .type(MobileViewValidator.CharacterType.ALPHA)
        .response(response)
        .minLength(1)
        .maxLength(1)
        .isRequired(true)
        .specialRules(v -> {
          if (!cc.matches("[aAfF]"))
            v.validationError("CC should be either A or F.");
        })
        .build();

    val upValidator = MobileViewValidator.builder()
        .field(up)
        .title("UP")
        .type(MobileViewValidator.CharacterType.NUMERIC)
        .response(response)
        .minLength(7)
        .maxLength(11)
        .isRequired(true)
        .build();

    return Arrays.asList(barcodeValidator, firstPartValidator, ricValidator, uiValidator, qtyValidator, ccValidator, upValidator);
  }
}
