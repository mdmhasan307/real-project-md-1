package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.core.utility.CertificateUtils;
import org.apache.commons.lang.text.StrBuilder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.lang.String.format;

/**
 * Scans the request header for the existence of a valid certificate chain for a given header key.  If found the certificate chain is
 * added to the request as a request attribute.
 * <p>
 * Supports handling of user certificates across various web server configurations such as NGINX, Tomcat, et al.
 */
@Slf4j
@Value
@Builder
public class CertificateFilter implements Filter {

  static final String BEFORE = "BEFORE";
  static final String AFTER = "AFTER";
  String headerKey;
  String attributeKey;
  CertificateUtils certificateUtils;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    val httpServletRequest = (HttpServletRequest) request;
    debugRequest(BEFORE, httpServletRequest);
    configRequestAttributes(httpServletRequest);
    debugRequest(AFTER, httpServletRequest);
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // nothing
  }

  @Override
  public void init(FilterConfig filterConfig) {
    // nothing
  }

  private void debugRequest(String context, HttpServletRequest httpServletRequest) {
    StrBuilder sb = new StrBuilder();
    sb.appendNewLine();
    sb.appendln("***********************************************************************************************");
    sb.appendln(format("REQUEST: %s", httpServletRequest.getRequestURI()));
    sb.appendln(format("CONTEXT: %s", context));
    sb.appendln("***********************************************************************************************");
    sb.appendln("REQUEST HEADERS: ");
    sb.appendln(format("* javax.servlet.request.X509Certificate: %s", httpServletRequest.getHeader("javax.servlet.request.X509Certificate")));
    sb.appendln(format("* X-SSL-Certificate: %s", httpServletRequest.getHeader("X-SSL-Certificate")));
    sb.appendln(format("* X-SSL-Client: %s", httpServletRequest.getHeader("X-SSL-Client")));
    sb.appendln("REQUEST ATTRIBUTES: ");
    sb.appendln(format("* javax.servlet.request.X509Certificate: %s", httpServletRequest.getAttribute("javax.servlet.request.X509Certificate")));
    sb.appendln("***********************************************************************************************");
    log.trace(sb.toString());
  }

  private void configRequestAttributes(HttpServletRequest httpServletRequest) {
    val rawPem = httpServletRequest.getHeader(headerKey);
    log.trace("raw pem at request header '{}': {}", headerKey, rawPem);
    if (rawPem == null) return;

    val certificateChain = certificateUtils.readCertificateChainFromPem(rawPem);
    if (certificateChain.length > 0) {
      log.trace("found X590Certificate[] in header '{}', assigning certificate to the request attribute '{}' as an X509Certificate[]", headerKey, attributeKey);
      httpServletRequest.setAttribute(attributeKey, certificateChain);
    }
    else {
      log.trace("did not find any valid certificates in header '{}'", headerKey);
    }
  }
}
