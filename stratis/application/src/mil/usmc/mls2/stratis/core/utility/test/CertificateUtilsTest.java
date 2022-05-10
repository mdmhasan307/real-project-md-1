package mil.usmc.mls2.stratis.core.utility.test;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.core.utility.CertificateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@UnitTest
class CertificateUtilsTest {

  private static final String CERT_KEY = "javax.servlet.request.X509Certificate";
  private static CertificateUtils certificateUtils;

  @BeforeAll
  public static void beforeAll() {
    certificateUtils = new CertificateUtils(CERT_KEY);
  }

  @Test
  public void withEncodedPemAsAttributeShouldSucceed() throws UnsupportedEncodingException {
    // when
    val rawPem = simplePem();
    val request = new MockHttpServletRequest();
    val certificateChain = certificateUtils.readCertificateChainFromPem(URLEncoder.encode(rawPem, "UTF-8"));
    request.setAttribute(CERT_KEY, certificateChain);

    // then
    val optionalX509Certificate = certificateUtils.getCertificate(request);
    assertThat(optionalX509Certificate).isPresent();
  }

  @Test
  public void withInvalidPemAsAttributeShouldFail() throws UnsupportedEncodingException {
    // when
    val rawPem = invalidPem();
    val request = new MockHttpServletRequest();
    val certificateChain = certificateUtils.readCertificateChainFromPem(URLEncoder.encode(rawPem, "UTF-8"));
    if (certificateChain != null && certificateChain.length > 0) {
      request.setAttribute(CERT_KEY, certificateChain);
    }

    // then
    val optionalX509Certificate = certificateUtils.getCertificate(request);
    assertThat(optionalX509Certificate).isEmpty();
  }

  private String simplePem() {
    return "-----BEGIN CERTIFICATE-----\n"
        + "MIIBMjCB3aADAgECAhB6225ckZVssEukPuvk1U1PMA0GCSqGSIb3DQEBBAUAMBox\n"
        + "GDAWBgNVBAMTD1Jvb3RDZXJ0aWZpY2F0ZTAeFw0wMTEwMTkxNjA5NTZaFw0wMjEw\n"
        + "MTkyMjA5NTZaMBsxGTAXBgNVBAMTEFVzZXJDZXJ0aWZpY2F0ZTIwXDANBgkqhkiG\n"
        + "9w0BAQEFAANLADBIAkEAzicGiW9aUlUoQIZnLy1l8MMV5OvA+4VJ4T/xo/PpN8Oq\n"
        + "WgZVGKeEp6JCzMlXEJk3TGLfpXL4Ytw+Ldhv0QPhLwIDAnMpMA0GCSqGSIb3DQEB\n"
        + "BAUAA0EAQmj9SFHEx66JyAps3ew4pcSS3QvfVZ/6qsNUYCG75rFGcTUPHcXKql9y\n"
        + "qBT83iNLJ//krjw5Ju0WRPg/buHSww==\n"
        + "-----END CERTIFICATE-----";
  }

  private String invalidPem() {
    return "-----BEGIN FAILING CERTIFICATE-----\n"
        + "MIIBMjCB3aADAgECAhB6225ckZVssEukPuvk1U1PMA0GCSqGSIb3DQEBBAUAMBox\n"
        + "GDAWBgNVBAMTD1Jvb3RDZXJ0aWZpY2F0ZTAeFw0wMTEwMTkxNjA5NTZaFw0wMjEw\n"
        + "MTkyMjA5NTZaMBsxGTAXBgNVBAMTEFVzZXJDZXJ0aWZpY2F0ZTIwXDANBgkqhkiG\n"
        + "9w0BAQEFAANLADBIAkEAzicGiW9aUlUoQIZnLy1l8MMV5OvA+4VJ4T/xo/PpN8Oq\n"
        + "WgZVGKeEp6JCzMlXEJk3TGLfpXL4Ytw+Ldhv0QPhLwIDAnMpMA0GCSqGSIb3DQEB\n"
        + "BAUAA0EAQmj9SFHEx66JyAps3ew4pcSS3QvfVZ/6qsNUYCG75rFGcTUPHcXKql9y\n"
        + "qBT83iNLJ//krjw5Ju0WRPg/buHSww==\n"
        + "-----END CERTIFICATE-----";
  }
}