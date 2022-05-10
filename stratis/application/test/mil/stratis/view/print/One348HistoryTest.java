package mil.stratis.view.print;

import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.OffsetDateTime;

import static mil.stratis.view.print.XssConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class One348HistoryTest {


    public static final String WINDOW_OPEN_PREFIX = "window.open('Serial1348Hist.jspx?";
    public static final String WINDOW_OPEN_SUFFIX = "');";

    @Test
    @DisplayName("Verify XSS Values not reflected in One348History::draw1348()")
    void testEscapeReflectedXSSValueInDraw1348() {
        DateService dateService = Mockito.mock(DateService.class);
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        One348History one348History = new One348History();

        one348History.documentNumber = XSS_PAYLOAD_BASIC;
        one348History.quantity = XSS_PAYLOAD_BASIC;

        String output = one348History.draw1348("_CONTEXT_PATH_", dateService, offsetDateTime);
        assertThat("Basic XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_BASIC)));

        output = one348History.draw1348Serial(dateService, offsetDateTime);
        assertThat("Basic XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_BASIC)));

        one348History.documentNumber = XSS_PAYLOAD_HEX;
        one348History.quantity = XSS_PAYLOAD_HEX;

        output = one348History.draw1348("_CONTEXT_PATH_", dateService, offsetDateTime);
        assertThat("Hex XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_HEX)));

        output = one348History.draw1348Serial(dateService, offsetDateTime);
        assertThat("Hex XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_HEX)));

        one348History.documentNumber = XSS_PAYLOAD_JAVASCRIPT;
        one348History.quantity = XSS_PAYLOAD_JAVASCRIPT;

        output = one348History.draw1348("_CONTEXT_PATH_", dateService, offsetDateTime);
        assertThat("JavaScript XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_JAVASCRIPT)));

        output = one348History.draw1348Serial(dateService, offsetDateTime);
        assertThat("JavaScript XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_JAVASCRIPT)));
    }

    @Test
    @DisplayName("Verify XSS Values not reflected in One348History::draw1348Serial()")
    void testEscapeReflectedXSSValueInDraw1348Serial() {
        DateService dateService = Mockito.mock(DateService.class);
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        One348History one348History = new One348History();

        one348History.documentNumber = XSS_PAYLOAD_BASIC;
        one348History.quantity = XSS_PAYLOAD_BASIC;

        String output = one348History.draw1348Serial(dateService, offsetDateTime);
        assertThat("Basic XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_BASIC)));

        one348History.documentNumber = XSS_PAYLOAD_HEX;
        one348History.quantity = XSS_PAYLOAD_HEX;

        output = one348History.draw1348Serial(dateService, offsetDateTime);
        assertThat("Hex XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_HEX)));

        one348History.documentNumber = XSS_PAYLOAD_JAVASCRIPT;
        one348History.quantity = XSS_PAYLOAD_JAVASCRIPT;

        output = one348History.draw1348Serial(dateService, offsetDateTime);
        assertThat("JavaScript XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_JAVASCRIPT)));

    }

    @Test
    @DisplayName("Verify XSS Values not reflected in One348History::launchSerial()")
    void testEscapeReflectedXSSValueInLaunchSerial() {

        One348History one348History = new One348History();
        one348History.theScn = XSS_PAYLOAD_HEX;
        one348History.quantity = XSS_PAYLOAD_HEX;
        one348History.serialOverflow = true;

        String output = one348History.launchSerial();
        output = StringUtils.removeStart(output, WINDOW_OPEN_PREFIX);
        output = StringUtils.removeEnd(output, WINDOW_OPEN_SUFFIX);
        String[] paramArray = org.apache.commons.lang.StringUtils.split(output, '&');
        assertThat("Should be two params...", paramArray.length, equalTo(2));
        assertThat("First param starts with quantity", paramArray[0], startsWith("quantity="));
        assertThat("Verify scn has been successfully escaped", paramArray[0], XSS_MALICIOUS_CHAR_MATCHER);
        assertThat("Second param starts with scn", paramArray[1], startsWith("scn="));
        assertThat("Verify quantity has been successfully escaped", paramArray[1], XSS_MALICIOUS_CHAR_MATCHER);

        one348History.theScn = XSS_PAYLOAD_JAVASCRIPT;
        one348History.quantity = XSS_PAYLOAD_JAVASCRIPT;
        one348History.serialOverflow = true;
        output = one348History.launchSerial();
        output = StringUtils.removeStart(output, WINDOW_OPEN_PREFIX);
        output = StringUtils.removeEnd(output, WINDOW_OPEN_SUFFIX);
        paramArray = StringUtils.split(output, '&');
        assertThat("Should be two params...", paramArray.length, equalTo(2));
        assertThat("First param starts with quantity", paramArray[0], startsWith("quantity="));
        assertThat("Verify scn has been successfully escaped", paramArray[0], XSS_MALICIOUS_CHAR_MATCHER);
        assertThat("Second param starts with scn", paramArray[1], startsWith("scn="));
        assertThat("Verify quantity has been successfully escaped", paramArray[1], XSS_MALICIOUS_CHAR_MATCHER);

        one348History.theScn = XSS_PAYLOAD_BASIC;
        one348History.quantity = XSS_PAYLOAD_BASIC;
        one348History.serialOverflow = true;
        output = one348History.launchSerial();
        output = StringUtils.removeStart(output, WINDOW_OPEN_PREFIX);
        output = StringUtils.removeEnd(output, WINDOW_OPEN_SUFFIX);
        paramArray = StringUtils.split(output, '&');
        assertThat("Should be two params...", paramArray.length, equalTo(2));
        assertThat("First param starts with quantity", paramArray[0], startsWith("quantity="));
        assertThat("Verify scn has been successfully escaped", paramArray[0], XSS_MALICIOUS_CHAR_MATCHER);
        assertThat("Second param starts with scn", paramArray[1], startsWith("scn="));
        assertThat("Verify quantity has been successfully escaped", paramArray[1], XSS_MALICIOUS_CHAR_MATCHER);
    }
}


