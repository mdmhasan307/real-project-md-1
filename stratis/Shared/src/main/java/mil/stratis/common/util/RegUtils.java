package mil.stratis.common.util;

import java.util.regex.Pattern;

/**
 * This is a Utility class.
 */
public final class RegUtils {

  /**
   * Private Constructor.
   */
  private RegUtils() {
  }

  /**
   * Returns true if a string contains the first character of 'Y'.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean containsFirstCharY(final String str) {
    return Pattern.matches("^[yY].*$", str);
  }

  /**
   * Returns true if a string contains the first character of special
   * characters !@#$%^&*()_+.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean containsFirstCharSpecial(final String str) {
    return Pattern.matches("^[!@#$%^&*()_+].*$", str);
  }

  /**
   * Returns true if a string contains the first character of digit
   * 0 - 9.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean containsFirstCharDigit(final String str) {
    return Pattern.matches("^[0-9].*$", str);
  }

  /**
   * Returns true if string contains whitespace character.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean containsWhitespace(final String str) {
    return Pattern.matches("^[\\s]+$", str);
  }

  /**
   * Returns true if string contains digits.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean containsDigits(final String str) {
    return Pattern.matches("^[\\d]+$", str);
  }

  /**
   * Returns true if string contains non-word character.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean containsInvalidCharacters(final String str) {
    return Pattern.matches("^[\\W]+$", str);
  }

  /**
   * Returns true if the string contains all except a non-digit number.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isNotAlphaNumeric(final String str) {
    return !Pattern.matches("^[\\w]*$", str);
  }

  /**
   * Returns true if the string contains all except a non-digit number.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isAlphaNumeric(final String str) {
    return Pattern.matches("^[\\w]*$", str);
  }

  /**
   * Returns true if the string contains alphanumerical with dashes and underscores (no spaces allowed).
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isAlphaNumericDashes(final String str) {
    return Pattern.matches("^[a-zA-Z0-9-_]*$", str);
  }

  /**
   * Returns true if the string contains all except a non-digit number.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isNotAlpha(final String str) {
    return !Pattern.matches("^[a-zA-Z ]*$", str);
  }

  /**
   * Returns true if the string contains all except a non-digit number.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isNotAlphaNumericPlusSpace(final String str) {
    return !Pattern.matches("^[a-zA-Z0-9 ]*$", str);
  }

  /**
   * Returns true if string contains all except alphabet.
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isNotNumeric(final String str) {
    return !Pattern.matches("^[\\d]*$", str);
  }

  /**
   * Returns true if string is not in dollar format
   * (#.#).
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isNotDollar(final String str) {
    return !Pattern.matches("^[0-9]*\\.?[0-9]*$", str);
  }

  /**
   * Returns true if string is not a valid email format
   * (some@domain.com).
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isNotValidEmail(final String str) {
    //http://www.devx.com/tips/Tip/28334
    return !Pattern.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]+$", str);
  }

  /**
   * Returns true if string is a decimal number
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isDecimal(final String str) {
    return Pattern.matches("^[0-9]*\\.[0-9]+$", str);
  }
  
  /**
   * Override toString method.
   *
   * @return String
   */
  public String toString() {
    return "All classes may override non-final methods.";
  }
}
