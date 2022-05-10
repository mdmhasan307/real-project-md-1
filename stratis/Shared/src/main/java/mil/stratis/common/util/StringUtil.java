package mil.stratis.common.util;


public final class StringUtil {

    /**
     * Return true or false if an object is empty (null or an empty string).
     * @param object - Object input value
     * @return true or false
     */
    public static boolean isEmpty (final Object object) {
        return (object == null || object.toString().trim().equals(""));
    }

    /**
     * Return true or false if a string is empty (null or an empty string).
     * @param str - String input value
     * @return true or false
     */
    public static boolean isEmpty (final String str) {
        return (str == null || str.trim().equals(""));
    }

    /**
     * Return true or false if an object is not empty (null or an empty string).
     * @param obj - Object input value
     * @return true or false
     */
    public static boolean isNotEmpty (final Object obj) {
        return (obj != null && !obj.toString().trim().equals(""));
    }

    /**
     * Return true or false if an string is not empty (null or an empty string).
     * @param str - String input value
     * @return true or false
     */
    public static boolean isNotEmpty (final String str) {
        return (str != null && !str.trim().equals(""));
    }

    /**
     * Trims an object value of leading and trailing spaces and returns String.
     * @param value - Object input value
     * @return String
     */
    public static String trimClean (final Object value) {
        String str = "";
        if (value != null) {
            str = value.toString().trim();
        }
        return str;
    }

    /**
     * Trims an object value of leading and trailing spaces and returns String.
     * @param value - String input value
     * @return String
     */
    public static String trimClean (final String value) {
        String str = "";
        if (value != null) {
            str = value.trim();
        }
        return str;
    }

    /**
     * Trims an object value of leading and trailing spaces,
     * converts the object value to an all uppercase String and returns String.
     * @param value - Object input value
     * @return String
     */
    public static String trimUpperCaseClean (final Object value) {
        String str = "";
        if (value != null) {
            str = value.toString().trim().toUpperCase();
        }
        return str;
    }

    /**
     * Trims a String value of leading and trailing spaces,
     * converts the String value to an all uppercase String and returns String.
     * @param value - String input value
     * @return String
     */
    public static String trimUpperCaseClean (final String value) {
        String str = "";
        if (value != null) {
            str = value.trim().toUpperCase();
        }
        return str;
    }

    /**
     * Converts the object value to a String and returns String.
     * @param value - Object input value
     * @return String
     */
    public static String clean (final Object value) {
        String str = "";
        if (value != null) {
            str = value.toString();
        }
        return str;
    }

    /**
     * Converts the object value to an int value and returns int, default 0.
     * @param value - Object input value
     * @return int
     */
    public static int cleanInt (final Object value) {
        int i = 0;
        if (value != null) {
            final String str = value.toString();
            try {
                i = Integer.parseInt(str);
            } catch (Exception e) {
                i = 0;
            }
        }
        return i;
    }

    /**
     * Converts the object value to an double value and returns double, default 0.
     * @param value - Object input value
     * @return double
     */
    public static double cleanDouble (Object value) {
        double i = 0.0;
        if (value != null) {
            String str = value.toString();
            try {
                i = Double.parseDouble(str);
            } catch (Exception e) {
                i = 0.0;
            }
        }
        return i;
    }


    /**
     * Override toString method.
     * @return String
     */
    public String toString () {
        return "All classes may override non-final methods.";
    }
}
