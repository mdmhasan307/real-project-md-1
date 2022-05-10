package mil.stratis.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * This is a Utility class.
 */
@Slf4j
public final class Util {

  /**
   * Private Constructor.
   */
  private Util() {
    //* do nothing
  }

  /**
   * Return true or false if an object is empty (null or an empty string).
   *
   * @param object - Object input value
   * @return true or false
   */
  public static boolean isEmpty(Object object) {
    return (object == null || object.toString().trim().equals(""));
  }

  /**
   * Return true or false if a string is empty (null or an empty string).
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isEmpty(String str) {
    return (str == null || str.trim().equals(""));
  }

  /**
   * Return true or false if an object is not empty (null or an empty string).
   *
   * @param obj - Object input value
   * @return true or false
   */
  public static boolean isNotEmpty(Object obj) {
    return (obj != null && !obj.toString().trim().equals(""));
  }

  /**
   * Return true or false if an string is not empty (null or an empty string).
   *
   * @param str - String input value
   * @return true or false
   */
  public static boolean isNotEmpty(String str) {
    return (str != null && !str.trim().equals(""));
  }

  /**
   * Trims an object value of leading and trailing spaces and returns String.
   *
   * @param value - Object input value
   * @return String
   */
  public static String trimClean(Object value) {
    String str = "";
    if (value != null) {
      str = value.toString().trim();
    }
    return str;
  }

  /**
   * Trims an object value of leading and trailing spaces and returns String.
   *
   * @param value - String input value
   * @return String
   */
  public static String trimClean(String value) {
    String str = "";
    if (value != null) {
      str = value.trim();
    }
    return str;
  }

  /**
   * Trims an object value of leading and trailing spaces,
   * converts the object value to an all uppercase String and returns String.
   *
   * @param value - Object input value
   * @return String
   */
  public static String trimUpperCaseClean(Object value) {
    String str = "";
    if (value != null) {
      str = value.toString().trim().toUpperCase();
    }
    return str;
  }

  //Creates a formatted list.
  public static String addCommasForList(String listStr, String itemStr) {
    if (listStr.isEmpty()) {
      listStr = itemStr;
    }
    else {
      listStr = listStr + ", " + itemStr;
    }
    return listStr;
  }

  /**
   * Trims a String value of leading and trailing spaces,
   * converts the String value to an all uppercase String and returns String.
   *
   * @param value - String input value
   * @return String
   */
  public static String trimUpperCaseClean(String value) {
    String str = "";
    if (value != null) {
      str = value.trim().toUpperCase();
    }
    return str;
  }

  /**
   * Converts the object value to a String and returns String.
   *
   * @param value - Object input value
   * @return String
   */
  public static String cleanString(Object value) {
    String str = "";
    if (value != null) {
      str = value.toString();
    }
    return str;
  }

  /**
   * Converts the object value to an int value and returns int, default 0.
   *
   * @param value - Object input value
   * @return int
   */
  public static int cleanInt(Object value) {
    return cleanInt(value, 0);
  }

  public static int cleanInt(Object value, int defaultValue) {
    int i = defaultValue;
    if (value != null) {
      String str = value.toString().trim();
      try {
        i = Integer.parseInt(str);
      }
      catch (Exception e) {
        //no-op
      }
    }
    return i;
  }

  /**
   * Converts the object value to an double value and returns double, default 0.
   *
   * @param value - Object input value
   * @return double
   */
  public static double cleanDouble(Object value) {
    double i = 0.0;
    if (value != null) {
      String str = value.toString().trim();
      try {
        i = Double.parseDouble(str);
      }
      catch (Exception e) {
        i = 0.0;
      }
    }
    return i;
  }

  /**
   * This function gets the current Julian Date given the number of characters
   * that should make up the Julian Date (e.g., 4-digit Julian date).  Only
   * accepts 3,4, or 5.  Any other numChars will return blanks.
   *
   * @param numChars - the number of digits that make up the Julian date
   * @return String value of Julian data
   */
  public static String getCurrentJulian(final int numChars) {
    StringBuilder retVal = new StringBuilder();
    try {
      if (numChars > 5) return "   ";
      final Calendar cal = Calendar.getInstance();
      SimpleDateFormat simpleDF = new SimpleDateFormat("yyyyDDD", Locale.US);
      final String formattedDate = simpleDF.format(cal.getTime());

      if (numChars == 5)
        retVal.append(formattedDate.charAt(2));
      if (numChars == 4 || numChars == 5)
        retVal.append(formattedDate.charAt(3));

      if (numChars >= 3) {
        retVal.append(formattedDate.charAt(4));
        retVal.append(formattedDate.charAt(5));
        retVal.append(formattedDate.charAt(6));
        return retVal.toString();
      }
    }
    catch (Exception e) {
      log.error("Exception getting Current Julian Date", e);
    }
    retVal.append("   ");
    return retVal.toString();
  }

  /**
   * Utility method to wrap the appropriate encoding and add a mandatory catch block for an exception that *should* be
   * impossible to generate.
   */
  public static String encodeUTF8(String val) {
    if (val == null || val.isEmpty())
      return val;
    try {
      return URLEncoder.encode(val, StandardCharsets.UTF_8.toString());
    }
    catch (UnsupportedEncodingException ex) {
      return val; // No good options at this point; we should never get here since we're using StandardCharsets
    }
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
