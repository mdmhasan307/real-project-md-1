package mil.usmc.mls2.stratis.core.utility;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * note: certificates will be found iff ClientAuthentication == 'true'
 * This aligns with CertificateUtils from CLC2S and TCPT.  Renamed because its spring managed, and not a static class to support access from ADF.
 */
@Slf4j
@SuppressWarnings("WeakerAccess")
@Component
public class CertificateManager {

  private static final String CN_REGEX = "CN=(.+?),";
  private static final String EDIPI_REGEX = "^\\d{10}$";

  @Value("${app.security.authentication.certificates.pivOnly.enabled:true}")
  private boolean pivOnlyEnabled;

  @Value("${app.security.authentication.certificates.pivOnly.validateByExtendedKeyUsages:false}")
  private boolean validateByExtendedKeyUsages;

  private static final String VALIDATION_PIV_CERTIFICATE_NOT_FOUND = "No PIV Authentication Certificate was found."
      + "  Ensure that your CAC card is inserted into your smart card reader and that you have a valid PIV Authentication Certificate.  These certificates are required for log in as of January 31, 2020."
      + "<br>If it is, you can try the following:"
      + "<br><ol><li>Close the USMC TCPT Application Window.</li>"
      + "<li>In Internet Explorer, go to the Tools - Internet Options - Content tab.</li>"
      + "<li>Click the 'Clear SSL State' button.</li>"
      + "<li>Close the Internet Options popup.</li>"
      + "<li>From the TCPT Notice and Consent page, click 'View Application Window.'</li>"
      + "<li>You should be prompted to select a Certificate - "
      + "<br>On this popup, you need to select the certificate where the Issuer is a \"<b>DOD ID CA</b>\" certificate."
      + "<br>You will not be able to log in to TCPT if you select a certificate issued by a \"<b>DOD EMAIL CA</b>\"</li>"
      + "<li>After you have selected your certificate (issued by a \"DOD ID CA\"), click OK, and you should be able to log in to TCPT.</li>"
      + "<li>If you need further assistance, please visit <a href=\"https://www.mls2support.com/\" target=\"_blank\">https://www.mls2support.com/</a></li><ol>";

  public Optional<CertInformation> getCertInformationFromRequest(HttpServletRequest httpServletRequest) {
    val certificate = CertificateUtils.getInstance().getCertificate(httpServletRequest);
    if (certificate.isPresent()) {
      val cert = certificate.get();
      validCertificate(cert);
      String subject = cert.getSubjectX500Principal().getName();
      Matcher matcher = Pattern.compile(CN_REGEX).matcher(Matcher.quoteReplacement(subject));

      String commonName;
      if (matcher.find()) {
        commonName = matcher.group(1);
      }
      else {
        return Optional.empty();
      }

      int i = commonName.lastIndexOf('.');
      if (i == -1) {
        log.warn("Certificate Common Name malformed.");
        return Optional.empty();
      }
      String edipi = commonName.substring(i + 1);
      if (!edipi.matches(EDIPI_REGEX)) {
        return Optional.empty();
      }
      val certInformation = CertInformation.builder()
          .commonName(commonName)
          .edipi(edipi)
          .build();
      return Optional.of(certInformation);
    }
    return Optional.empty();
  }

  private void validCertificate(X509Certificate certificate) {
    if (pivOnlyEnabled && !CertificateUtils.getInstance().isAuthenticationOnlyCertificateByKeyUsages(certificate, validateByExtendedKeyUsages)) {
      throw new ValidationException(VALIDATION_PIV_CERTIFICATE_NOT_FOUND);
    }
  }

  @lombok.Value
  @Builder
  public static class CertInformation {

    String commonName;
    String edipi;
  }
}