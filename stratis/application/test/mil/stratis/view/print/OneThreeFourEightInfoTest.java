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

public class OneThreeFourEightInfoTest {

    public static final String WINDOW_OPEN_PREFIX = "window.open('Serial1348.jspx?";
    public static final String WINDOW_OPEN_SUFFIX = "');";

    @Test
    @DisplayName("Verify XSS Values not reflected in OneThreeFourEightInfo::draw1348()")
    void testEscapeReflectedXSSValueInDraw1348() {
        DateService dateService = Mockito.mock(DateService.class);
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        OneThreeFourEightInfo oneThreeFourEightInfo = new OneThreeFourEightInfo();

        oneThreeFourEightInfo.documentNumber = XSS_PAYLOAD_BASIC;
        oneThreeFourEightInfo.quantity = XSS_PAYLOAD_BASIC;

        String output = oneThreeFourEightInfo.draw1348("_CONTEXT_PATH_", dateService, offsetDateTime);
        assertThat("Basic XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_BASIC)));

        output = oneThreeFourEightInfo.draw1348Serial(dateService, offsetDateTime);
        assertThat("Basic XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_BASIC)));

        oneThreeFourEightInfo.documentNumber = XSS_PAYLOAD_HEX;
        oneThreeFourEightInfo.quantity = XSS_PAYLOAD_HEX;

        output = oneThreeFourEightInfo.draw1348("_CONTEXT_PATH_", dateService, offsetDateTime);
        assertThat("Hex XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_HEX)));

        output = oneThreeFourEightInfo.draw1348Serial(dateService, offsetDateTime);
        assertThat("Basic XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_HEX)));

        oneThreeFourEightInfo.documentNumber = XSS_PAYLOAD_JAVASCRIPT;
        oneThreeFourEightInfo.quantity = XSS_PAYLOAD_JAVASCRIPT;

        output = oneThreeFourEightInfo.draw1348("_CONTEXT_PATH_", dateService, offsetDateTime);
        assertThat("JavaScript XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_JAVASCRIPT)));

        output = oneThreeFourEightInfo.draw1348Serial(dateService, offsetDateTime);
        assertThat("Basic XSS has been escaped from response", output, not(containsString(XSS_PAYLOAD_JAVASCRIPT)));


    }

    @Test
    @DisplayName("Verify XSS Values not reflected in OneThreeForEightInfo::launchSerial()")
    void testEscapeReflectedXSSValueInLaunchSerial() {

        OneThreeFourEightInfo oneThreeFourEightInfo = new OneThreeFourEightInfo();
        oneThreeFourEightInfo.theScn = XSS_PAYLOAD_HEX;
        oneThreeFourEightInfo.quantity = XSS_PAYLOAD_HEX;
        oneThreeFourEightInfo.serialOverflow = true;

        String output = oneThreeFourEightInfo.launchSerial();
        output = StringUtils.removeStart(output, WINDOW_OPEN_PREFIX);
        output = StringUtils.removeEnd(output, WINDOW_OPEN_SUFFIX);
        String[] paramArray = org.apache.commons.lang.StringUtils.split(output, '&');
        assertThat("Should be two params...", paramArray.length, equalTo(2));
        assertThat("First param starts with quantity", paramArray[0], startsWith("quantity="));
        assertThat("Verify scn has been successfully escaped", paramArray[0], XSS_MALICIOUS_CHAR_MATCHER);
        assertThat("Second param starts with scn", paramArray[1], startsWith("scn="));
        assertThat("Verify quantity has been successfully escaped", paramArray[1], XSS_MALICIOUS_CHAR_MATCHER);

        oneThreeFourEightInfo.theScn = XSS_PAYLOAD_JAVASCRIPT;
        oneThreeFourEightInfo.quantity = XSS_PAYLOAD_JAVASCRIPT;
        oneThreeFourEightInfo.serialOverflow = true;
        output = oneThreeFourEightInfo.launchSerial();
        output = StringUtils.removeStart(output, WINDOW_OPEN_PREFIX);
        output = StringUtils.removeEnd(output, WINDOW_OPEN_SUFFIX);
        paramArray = StringUtils.split(output, '&');
        assertThat("Should be two params...", paramArray.length, equalTo(2));
        assertThat("First param starts with quantity", paramArray[0], startsWith("quantity="));
        assertThat("Verify scn has been successfully escaped", paramArray[0], XSS_MALICIOUS_CHAR_MATCHER);
        assertThat("Second param starts with scn", paramArray[1], startsWith("scn="));
        assertThat("Verify quantity has been successfully escaped", paramArray[1], XSS_MALICIOUS_CHAR_MATCHER);

        oneThreeFourEightInfo.theScn = XSS_PAYLOAD_BASIC;
        oneThreeFourEightInfo.quantity = XSS_PAYLOAD_BASIC;
        oneThreeFourEightInfo.serialOverflow = true;
        output = oneThreeFourEightInfo.launchSerial();
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


