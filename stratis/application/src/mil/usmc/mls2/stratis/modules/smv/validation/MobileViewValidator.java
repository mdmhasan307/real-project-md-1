package mil.usmc.mls2.stratis.modules.smv.validation;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.StringUtil;
import mil.usmc.mls2.stratis.core.domain.model.SpaPostResponse;

import java.util.function.Consumer;

/**
 * Class to encapsulate all transshipment validation for all 3 types (tcn, document, and contact).
 * Handles validation checks as well as returning validation warnings to the user.
 */
@Value
@Builder
@Slf4j
public class MobileViewValidator {

  String field;
  String title;
  SpaPostResponse response;

  CharacterType type;
  Integer minLength;
  Integer maxLength;
  boolean isRequired;
  boolean cannotStartWithY;
  Consumer<MobileViewValidator> specialRules;
  @NonFinal
  @Builder.Default
  boolean valid = true;

  public static String getAACValidationErrorMessage(String aac) {
    return String.format("Restricted AAC (%s)", aac);
  }

  /**
   * Main method that orchestrates all validation checks.
   * Could be called automatically after validator creation,
   * but is still currently public to allow calling class a little more control.
   */
  public boolean validate() {

    log.debug("Mobile view validator settings: {}", this);

    if (!isRequired && StringUtil.isEmpty(field)) return isValid();

    checkCorrectCharacterType();
    checkCorrectLength();
    checkRequired();
    checkStartsWithY();

    if (null != specialRules) specialRules.accept(this);

    return isValid();
  }

  /**
   * Method to invalidate the field and provide a warning message to the user.
   */
  public void validationError(String message) {

    log.warn(message);
    response.addWarning(message);
    valid = false;
  }

  private void checkRequired() {

    if (isRequired && StringUtil.isEmpty(field))
      validationError(String.format("%s is a required.", title));
  }

  private void checkCorrectLength() {

    if (null != minLength && field.length() < minLength)
      validationError(String.format("%s must be more than %d characters long.", title, minLength));
    if (null != maxLength && field.length() > maxLength)
      validationError(String.format("%s must be less than %d characters long.", title, maxLength));
  }

  private void checkCorrectCharacterType() {

    if (null == type) return;

    switch (type) {
      case ALPHA:
        if (RegUtils.isNotAlpha(field))
          validationError(String.format("%s must only contain alpha characters.", title));
        break;

      case ALPHANUMERIC:
        if (RegUtils.isNotAlphaNumeric(field))
          validationError(String.format("%s must only contain alphanumeric characters.", title));
        break;

      case NUMERIC:
        if (RegUtils.isNotNumeric(field))
          validationError(String.format("%s must be a positive integer value.", title));
        break;
      case DECIMAL:
        if (RegUtils.isNotDollar(field))
          validationError(String.format("%s must be a decimal value.", title));
        break;

      default:
        throw new IllegalArgumentException(String.format("%s not supported.", type));
    }
  }

  private void checkStartsWithY() {

    if (cannotStartWithY && field.startsWith("Y"))
      validationError(String.format("%s must not begin with 'Y'", title));
  }

  /**
   * Defines the character set that are allowed to be used in the field.
   */
  public enum CharacterType {
    ALPHA,
    ALPHANUMERIC,
    NUMERIC,
    DECIMAL
  }
}
