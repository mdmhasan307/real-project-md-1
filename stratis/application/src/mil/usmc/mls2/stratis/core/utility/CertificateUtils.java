package mil.usmc.mls2.stratis.core.utility;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * note: certificates will be found iff ClientAuthentication == 'true'
 */
@Slf4j
@SuppressWarnings("WeakerAccess")
@Component
public class CertificateUtils {

  private static CertificateUtils instance = null;

  private final String certificateKey;
  private final Set<String> authenticationOnlyExtendedKeyUsages;

  public CertificateUtils(@Value("${stratis.http.request.attribute.certificate}") String certificateKey) {
    // case intentionally minuscule
    this.authenticationOnlyExtendedKeyUsages = new HashSet<>();
    this.authenticationOnlyExtendedKeyUsages.add("client authentication");
    this.authenticationOnlyExtendedKeyUsages.add("smartcard logon");
    this.certificateKey = certificateKey;
  }

  @PostConstruct
  private void postConstruct() {
    instance = this;
  }

  public static CertificateUtils getInstance() {
    return instance;
  }

  public boolean isAuthenticationOnlyCertificateByKeyUsages(X509Certificate certificate, boolean validateByExtendedKeyUsages) {
    if (validateByExtendedKeyUsages) return isAuthenticationOnlyCertificateByExtendedKeyUsages(certificate);
    return isAuthenticationOnlyCertificateByKeyUsages(certificate);
  }

  public Optional<X509Certificate> getCertificate(HttpServletRequest httpServletRequest) {
    return getCertificateChain(httpServletRequest).map(x -> x[0]);
  }

  public Optional<X509Certificate[]> getCertificateChain(HttpServletRequest httpServletRequest) {
    return Optional.ofNullable((X509Certificate[]) httpServletRequest.getAttribute(certificateKey));
  }

  @SuppressWarnings("unchecked")
  public X509Certificate[] readCertificateChainFromPem(String rawPem) {
    try {
      val pem = rawPem != null && rawPem.contains("%") ? URLDecoder.decode(rawPem, "UTF-8") : rawPem;
      val cfy = CertificateFactory.getInstance("X.509");
      if (pem == null) {
        log.error("encountered null pem attempting to read certificate from pem: {}", rawPem);
        return new X509Certificate[0];
      }
      val certificates = (Collection<X509Certificate>) cfy.generateCertificates(new ByteArrayInputStream(pem.getBytes()));
      return !certificates.isEmpty() ? certificates.toArray(new X509Certificate[0]) : new X509Certificate[0];
    }
    catch (CertificateException e) {
      log.error("encountered CertificateException attempting to read certificate from pem: {}", rawPem);
      return new X509Certificate[0];
    }
    catch (UnsupportedEncodingException e) {
      log.error("encountered UnsupportedEncodingException attempting to read certificate from pem: {}", rawPem);
      return new X509Certificate[0];
    }
  }

  /**
   * KeyUsage ::= BIT STRING {
   * * digitalSignature (0),
   * * nonRepudiation (1),
   * * keyEncipherment (2),
   * * dataEncipherment (3),
   * * keyAgreement (4),
   * * keyCertSign (5),
   * * cRLSign (6),
   * * encipherOnly (7),
   * * decipherOnly (8) }
   *
   * @return true iff the digitalSignature usage field is true
   */
  private boolean isAuthenticationOnlyCertificateByKeyUsages(X509Certificate certificate) {
    if (certificate == null) return false;
    val keyUsages = certificate.getKeyUsage();
    if (!keyUsages[0]) return false;
    for (int i = 1; i < keyUsages.length; i++) {
      if (keyUsages[i]) return false;
    }
    return true;
  }

  /**
   * Analyzes the Extended KeyUsages of the X509 certificate to determine if the certificate is authentication-only (PIV Authentication certificate)
   *
   * @return true iff 'Client Authentication' and 'Smartcard Logon' are the defined extended key usages
   */
  private boolean isAuthenticationOnlyCertificateByExtendedKeyUsages(X509Certificate certificate) {
    if (certificate == null) return false;

    try {
      if (log.isTraceEnabled()) {
        log.trace("Certificate's raw extendedKeyUsage values:");
        certificate.getExtendedKeyUsage().forEach(eku -> log.trace("  - [{}]", eku));
      }

      val certificateExtendedKeyUsages = certificate.getExtendedKeyUsage()
          .stream()
          .map(String::toLowerCase)
          .collect(Collectors.toCollection(HashSet::new));

      // must contain the authenticationOnlyExtendedKeyUsages items
      if (!certificateExtendedKeyUsages.containsAll(authenticationOnlyExtendedKeyUsages)) return false;

      // must ONLY contain the authenticationOnlyExtendedKeyUsages items
      certificateExtendedKeyUsages.removeAll(authenticationOnlyExtendedKeyUsages);
      return certificateExtendedKeyUsages.isEmpty();
    }
    catch (CertificateParsingException e) {
      log.warn("failed to determine if certificate is an authentication-only certificate due to a certificate parsing exception.  Resolving by key usages instead.");
      return isAuthenticationOnlyCertificateByKeyUsages(certificate);
    }
  }
}