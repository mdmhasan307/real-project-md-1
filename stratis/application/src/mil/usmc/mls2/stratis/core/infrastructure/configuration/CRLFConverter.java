package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import org.springframework.beans.factory.annotation.Value;

/**
 * Custom implementation of a CRLFConverter
 * <p>
 * Note: owasp-security-logging-logback is an OWASP-provided library intended to resolve CRLF Log forging issues, however the library
 * is not properly maintained and currently contains security vulnerabilities as of version 1.1.6, last published March 2018.
 * <p>
 * Usage: configured in logback-spring.xml
 * Only required for console-appender as all other appenders are json-based which already handles CRLF
 */
@SuppressWarnings("unused")
public class CRLFConverter extends CompositeConverter<ILoggingEvent> {

  @Value("${stratis.logging.crlfEncoding.enabled}")
  private boolean encodingEnabled;

  @Override
  protected String transform(ILoggingEvent event, String in) {
    return encodingEnabled ? CRLFConverter.replaceCRLFWithUnderscore(in) : in;
  }

  /**
   * Override start method because the superclass ReplacingCompositeConverter
   * requires at least two options and this class has none.
   */
  @Override
  public void start() {
    started = true;
  }

  /**
   * Replace any carriage returns and line feeds with an underscore to prevent log injection attacks.
   *
   * @param value string to convert
   * @return converted string
   */
  public static String replaceCRLFWithUnderscore(String value) {
    // ensure no CRLF injection into logs for forging records
    String clean = value.replace('\n', '_').replace('\r', '_');
    if (!value.equals(clean)) {
      clean += " (Encoded)";
    }

    return clean;
  }
}
