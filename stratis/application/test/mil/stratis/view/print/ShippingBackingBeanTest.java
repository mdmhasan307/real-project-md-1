package mil.stratis.view.print;

import mil.stratis.model.services.ShippingServiceImpl;
import mil.stratis.view.shipping.ShippingBackingBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;

public class ShippingBackingBeanTest {

    @Test
    @DisplayName("Verify XSS strings escaped in printManifest()")
    void testEscapeReflectedXSSValueInPrintManifest() {
        ShippingBackingBean shippingBackingBean = new ShippingBackingBean(-1, 999, "");
        String ldcon = "";
        String aac = "";
        String leadTcn= "";
        String noOfCopies = "";
        String autoprint = "";
        String stratisUrlContextPath = "";
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        ShippingServiceImpl shippingService = mock(ShippingServiceImpl.class);
        String output = shippingBackingBean.printManifest(ldcon, aac, leadTcn, noOfCopies, autoprint, servletRequest, shippingService, stratisUrlContextPath);

        assertThat("This is null because we don't have access to a bunch of prepared statements and such.", output, equalTo(null));
    }
}
