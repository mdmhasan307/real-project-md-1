package mil.stratis.view.print;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;

public class XssConstants {
    public static final String XSS_PAYLOAD_HEX = "<IMG SRC=&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29>";
    public static final String XSS_PAYLOAD_JAVASCRIPT = "<IMG SRC=\"javascript:alert('XSS');\">";
    public static final String XSS_PAYLOAD_BASIC = "<IMG SRC=\"javascript:alert('XSS');\">";
    public static final Matcher<String> XSS_MALICIOUS_CHAR_MATCHER = not(anyOf(
            containsString("&"),
            containsString(">"),
            containsString("<")
    ));
}
