package mil.usmc.mls2.stratis.common.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.camel.ExchangePattern;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a Camel Message (Object) that should be published as an event for indirect processing
 * <p>
 * meaningfulIdentifier         a meaningful identifier for the message to be published. Used for logging.
 * uri                          uri
 * exchangePattern              exchange pattern
 * body                         message
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CamelMessage implements Message {

  private final String meaningfulIdentifier;
  private final String uri;
  private final ExchangePattern exchangePattern;
  private final Object body;
  private final Map<String, Object> headers;

  public static CamelMessage of(String meaningfulIdentifier, String uri, ExchangePattern exchangePattern, Object body) {
    return new CamelMessage(meaningfulIdentifier, uri, exchangePattern, body, Collections.emptyMap());
  }

  public static CamelMessage of(String meaningfulIdentifier, String uri, ExchangePattern exchangePattern, Object body, Map<String, Object> headers) {
    return new CamelMessage(meaningfulIdentifier, uri, exchangePattern, body, headers);
  }
}