package mil.stratis.view.print;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class LabelBackingTest {
    @Test
    @DisplayName("Verify XSS Values not reflected in LabelBacking::draw1348()")
    void testEscapeReflectedXSSValuePrintFloorLocationLabelHTML() {

        String floor = XssConstants.XSS_PAYLOAD_BASIC;
        String aac = XssConstants.XSS_PAYLOAD_BASIC;
        String area = XssConstants.XSS_PAYLOAD_BASIC;

        LabelBacking labelBacking = new LabelBacking();
        String output = labelBacking.printFloorLocationLabelHTML(floor, aac, area, false, "");
        assertThat("Floor param has been escaped", output, not(containsString(floor)));
        assertThat("Aac param has been escaped", output, not(containsString(aac)));
        assertThat("Area param has been escaped", output, not(containsString(area)));

        floor = XssConstants.XSS_PAYLOAD_HEX;
        aac = XssConstants.XSS_PAYLOAD_HEX;
        area = XssConstants.XSS_PAYLOAD_HEX;

        output = labelBacking.printFloorLocationLabelHTML(floor, aac, area, false, "");
        assertThat("Floor param has been escaped", output, not(containsString(floor)));
        assertThat("Aac param has been escaped", output, not(containsString(aac)));
        assertThat("Area param has been escaped", output, not(containsString(area)));

        floor = XssConstants.XSS_PAYLOAD_JAVASCRIPT;
        aac = XssConstants.XSS_PAYLOAD_JAVASCRIPT;
        area = XssConstants.XSS_PAYLOAD_JAVASCRIPT;

        output = labelBacking.printFloorLocationLabelHTML(floor, aac, area, false, "");
        assertThat("Floor param has been escaped", output, not(containsString(floor)));
        assertThat("Aac param has been escaped", output, not(containsString(aac)));
        assertThat("Area param has been escaped", output, not(containsString(area)));

        output = labelBacking.printFloorLocationLabelHTML(floor, aac, area, true, "");
        assertThat("Floor param has been escaped", output, not(containsString(floor)));
        assertThat("Aac param has been escaped", output, not(containsString(aac)));
        assertThat("Area param has been escaped", output, not(containsString(area)));
    }
}
