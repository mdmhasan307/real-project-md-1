package mil.stratis.common.util;

/**
 * This is utility class used for padding values.
 */
public final class PadUtil {

  /**
   * Private Constructor.
   */
  private PadUtil() {
  }

  /**
   * Utility method to pad a string with leading spaces.
   *
   * @param str        - String value to pad
   * @param pad_length - length to pad the String value to
   * @return padded String
   */
  public static String padIt(final String str, final int pad_length) {
    StringBuilder pad = new StringBuilder();

    if (str == null) {
      for (int i = 0; i < pad_length; i++) {
        pad.append(' ');
      }
    }
    else {
      final int cur_length = str.length();
      if (cur_length < pad_length) {
        // pad it
        final int length = pad_length - cur_length;
        for (int i = 0; i < length; i++) {
          pad.append(' ');
        }
      }
      pad.append(str);
    }
    return pad.toString();
  }

  /**
   * Utility method to pad a string with trailing spaces.
   *
   * @param str        - String value to pad
   * @param pad_length - length to pad the String value to
   * @return padded String
   */
  public static String padItTrailingSpaces(final String str, final int pad_length) {
    StringBuilder pad = new StringBuilder();

    if (str == null) {
      for (int i = 0; i < pad_length; i++) {
        pad.append(' ');
      }
    }
    else {
      final int cur_length = str.length();
      if (cur_length < pad_length) {
        pad.append(str);
        // pad it
        final int length = pad_length - cur_length;
        for (int i = 0; i < length; i++) {
          pad.append(' ');
        }
      }
      else {
        // no changes
        pad.append(str);
      }
    }
    return pad.toString();
  }

  /**
   * Utility method to pad a string with leading zeros.
   *
   * @param str       - String value to pad
   * @param padLength - length to pad the String value to
   * @return padded String
   */
  public static String padItZeros(final String str, final int padLength) {
    StringBuilder pad;

    if (str == null) {
      pad = new StringBuilder(padLength);
      for (int i = 0; i < padLength; i++) {
        pad.append('0');
      }
    }
    else {
      final int currentLength = str.length();
      if (currentLength < padLength) {
        pad = new StringBuilder(padLength);
        // pad it with zeros
        final int length = padLength - currentLength;
        for (int i = 0; i < length; i++) {
          pad.append('0');
        }
      }
      else {
        pad = new StringBuilder(currentLength);
        // no changes
      }
      pad.append(str);
    }
    return pad.toString();
  }

  /**
   * Utility method to pad a string with trailing zeros.
   *
   * @param str        - String value to pad
   * @param pad_length - length to pad the String value to
   * @return padded String
   */
  public static String padItTrailingZeros(final String str,
                                          final int pad_length) {
    StringBuilder pad = new StringBuilder();

    if (str == null) {
      for (int i = 0; i < pad_length; i++) {
        pad.append('0');
      }
    }
    else {
      final int cur_length = str.length();
      if (cur_length < pad_length) {
        pad.append(str);

        // pad it with zeros
        final int length = pad_length - cur_length;
        for (int i = 0; i < length; i++) {
          pad.append('0');
        }
      }
      else {
        // no changes
        pad.append(str);
      }
    }
    return pad.toString();
  }

  /**
   * Utility method to pad a string with leading zeros
   */
  public static String padItLeadingZeros(String str, int padLength) {
    StringBuilder pad = new StringBuilder();

    if (str == null) {
      for (int i = 0; i < padLength; i++) {
        pad.append('0');
      }
    }
    else {
      int currentLength = str.length();
      if (currentLength < padLength) {
        // pad it with zeros
        int length = padLength - currentLength;
        for (int i = 0; i < length; i++) {
          pad.append('0');
        }
        pad.append(str);
      }
      else {
        // no changes
        pad = new StringBuilder(str);
      }
    }
    return pad.toString();
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
