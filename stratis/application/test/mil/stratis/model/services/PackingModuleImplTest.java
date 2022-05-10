package mil.stratis.model.services;

import lombok.val;
import mil.stratis.view.print.XssConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class PackingModuleImplTest {
    @Test
    @DisplayName("Test packing line null normalization")
    void emptyPlaceholderTest() {
        PackingModuleImpl packingModule = new PackingModuleImpl();

        assertThat("The value null should become empty string.", packingModule.normalizeValue(null), equalTo(""));
        assertThat("The value \"null\" should become empty string.", packingModule.normalizeValue("null"), equalTo(""));
        assertThat("Real string is unchanged.", packingModule.normalizeValue("foo"), equalTo("foo"));
        assertThat("Empty string is unchanged.", packingModule.normalizeValue(""), equalTo(""));
    }

    @Test
    @DisplayName("Verify XSS Values not reflected in ReprintBacking::printContainerSummary()")
    void testEscapeReflectedXSSValueInPrintContainerSummary() {

        String normalUrlContextPathImgStringPrefix = "<img src=\"foo/bar/baz/what_ever/SlotImage?type=BARCODE&bc=FOOBARBAZ&bt=BAR39\"/>";
        String normalUrlContextPath = "foo/bar/baz/what_ever";
        String normalBarcode = "FOOBARBAZ";

        PackingModuleImpl reprintBacking = new PackingModuleImpl();
        PackingLabelValues packingLabelValues = new PackingLabelValues();

        String output = reprintBacking.assembleContainerSummaryHtml(normalBarcode, normalUrlContextPath, packingLabelValues, new ArrayList<>());
        assertThat("printContainerSummary() should return *something*", output, not(equalTo(null)));
        assertThat("A normal urlContextPath should be unchanged.", output, containsString(normalUrlContextPathImgStringPrefix));
        assertThat("A normal barcode string should be found unchanged.", output, containsString(normalBarcode));

        // Check for escaped basic XSS payload (from owasp)
        String barcode = XssConstants.XSS_PAYLOAD_BASIC;
        String urlContextPath = XssConstants.XSS_PAYLOAD_BASIC;

        output = reprintBacking.assembleContainerSummaryHtml(barcode, urlContextPath, packingLabelValues, new ArrayList<>());
        assertThat("Malicious basic xss payload in barcode arg was not escaped", output, not(containsString(barcode)));
        assertThat("Malicious basic xss payload in urlContextPath arg was not escaped.", output, not(containsString(urlContextPath)));

        // Check for escaped hex XSS payload (from owasp)
        barcode = XssConstants.XSS_PAYLOAD_HEX;
        urlContextPath = XssConstants.XSS_PAYLOAD_HEX;

        output = reprintBacking.assembleContainerSummaryHtml(barcode, urlContextPath, packingLabelValues, new ArrayList<>());
        assertThat("Malicious hex payload in barcode arg was not escaped", output, not(containsString(barcode)));
        assertThat("Malicious hex payload in urlContextPath arg was not escaped.", output, not(containsString(urlContextPath)));

        // Check for escaped javascript XSS payload (from owasp)
        barcode = XssConstants.XSS_PAYLOAD_JAVASCRIPT;
        urlContextPath = XssConstants.XSS_PAYLOAD_JAVASCRIPT;

        output = reprintBacking.assembleContainerSummaryHtml(barcode, urlContextPath, packingLabelValues, new ArrayList<>());
        assertThat("Malicious javascript in barcode arg was not escaped.", output, not(containsString(barcode)));
        assertThat("Malicious javascript in urlContextPath arg was not escaped.", output, not(containsString(urlContextPath)));
    }

    @Test
    @DisplayName("Verify XSS Values not reflected in ReprintBacking::printContainerSummary()")
    void testAllValuesRenderedPrintContainerSummary() {

        String urlContextPath = "foo/bar/baz/what_ever";
        String barcodeId = "THEBARCODE";

        PackingModuleImpl reprintBacking = new PackingModuleImpl();
        PackingLabelValues packingLabelValues = new PackingLabelValues();

        packingLabelValues.slot = "SLOT 1";
        packingLabelValues.station = "STATION 1";
        packingLabelValues.tcn = "ATCN";
        packingLabelValues.routeTo = "ROUTE_DEST";
        packingLabelValues.escapedShipTo = "SHIP_TO";

        // Generate container packing line entries...
        List<PackingLineItemValues> packingLineItemValuesList = new ArrayList<>();
        Collections.addAll(packingLineItemValuesList,
                PackingLineItemValues.builder()
                        .pin("PIN1")
                        .documentNumber("DOCNUM1")
                        .nsn("NSN1")
                        .ui("UI1")
                        .quantity("QTY1")
                        .suppAdd("SUPADD1").build(),
                PackingLineItemValues.builder()
                        .pin("PIN2")
                        .documentNumber("DOCNUM2")
                        .nsn("NSN2")
                        .ui("UI2")
                        .quantity("QTY2")
                        .suppAdd("SUPADD2").build(),
                PackingLineItemValues.builder()
                        .pin("PIN3")
                        .documentNumber("DOCNUM3")
                        .nsn("NSN3")
                        .ui("UI3")
                        .quantity("QTY3")
                        .suppAdd("SUPADD3").build()
        );

        String outputHtml = reprintBacking.assembleContainerSummaryHtml(barcodeId, urlContextPath, packingLabelValues, packingLineItemValuesList);

        assertThat("Check barcode value is in img tag.", outputHtml, containsString("type=BARCODE&bc="+barcodeId));
        assertThat("Check slot value " + packingLabelValues.slot + " appears", outputHtml, containsString(packingLabelValues.slot));
        assertThat("Check station value " + packingLabelValues.station + " appears", outputHtml, containsString(packingLabelValues.station));
        assertThat("Check tcn value " + packingLabelValues.tcn + " appears", outputHtml, containsString(packingLabelValues.tcn));
        assertThat("Check route to value " + packingLabelValues.routeTo + " appears", outputHtml, containsString(packingLabelValues.routeTo));
        assertThat("Check ship to value " + packingLabelValues.escapedShipTo + " appears", outputHtml, containsString(packingLabelValues.escapedShipTo));

        packingLineItemValuesList.forEach(it -> {
            assertThat("Check each value in picking line [" + it.pin + "] is in output...", outputHtml, containsString(it.pin));
            assertThat("Check each value in picking line [" + it.documentNumber + "] is in output...", outputHtml, containsString(it.documentNumber));
            assertThat("Check each value in picking line [" + it.nsn + "] is in output...", outputHtml, containsString(it.nsn));
            assertThat("Check each value in picking line [" + it.ui + "] is in output...", outputHtml, containsString(it.ui));
            assertThat("Check each value in picking line [" + it.quantity + "] is in output...", outputHtml, containsString(it.quantity));
            assertThat("Check each value in picking line [" + it.suppAdd + "] is in output...", outputHtml, containsString(it.suppAdd));

                }
        );
    }
}
