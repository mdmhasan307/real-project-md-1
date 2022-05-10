package mil.stratis.common.util;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;

import java.util.Calendar;

@Slf4j
public class GCSSMCUtil {
    private GCSSMCUtil() {
    }

    public static boolean isEmpty(Object object) {
        if (object != null) {
            return object.toString().trim().equals("");
        }
        return true;
    }

    public static boolean isEmpty(String str) {
        if (str != null) {
            return str.trim().equals("");
        }
        return true;
    }
    public static String getCurrentJulian(int numChars) {
        String retVal = "";
        try {

            Calendar cal=Calendar.getInstance();
            SimpleDateFormat simpleDF=new SimpleDateFormat("yyyyDDD");
            String formattedDate=simpleDF.format(cal.getTime());

            if (numChars == 5) retVal += formattedDate.charAt(2);
            if (numChars == 4 || numChars == 5) retVal += formattedDate.charAt(3);

            if (numChars >= 3 && numChars <= 5) {
                retVal += formattedDate.charAt(4);
                retVal += formattedDate.charAt(5);
                retVal += formattedDate.charAt(6);
                return retVal;
            }
            if (numChars < 3 || numChars > 5) return "   ";
        }
        catch (Exception e) {
            log.error("Error in {}", e.getStackTrace()[0].getMethodName(), e);
        }
        retVal = "   ";
        return retVal;
    }
}
