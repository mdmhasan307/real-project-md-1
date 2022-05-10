package mil.usmc.mls2.stratis.common.share.hmac;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * HMAC: Hashed Message Authentication Code utilities used to verify data integrity and authenticity
 */
@Component
@RequiredArgsConstructor
public class HmacUtility {

  private static final Pattern AUTHORIZATION_HEADER_PATTERN = Pattern.compile("^(\\w+) (\\S+):(\\S+):([\\S]+)$");
  private static final String USER_AGENT = "MIPS RestAPI Client v.1.0";

  private final Properties props;

  public boolean isEnforceHmac() {
    return props.enforceHmac();
  }

  public void configureHeaders(String baseUrl, String resource, HttpHeaders headers, HttpMethod method) {
    val now = OffsetDateTime.now();
    val nonce = UUID.randomUUID().toString();
    val signatureBuilder = createSignatureBuilder(host(baseUrl), port(baseUrl), resource, nonce, method, now);
    val signature = signatureBuilder.buildAsBase64String();
    val algorithm = signatureBuilder.algorithm();
    val authHeader = format("%s %s:%s:%s", algorithm, props.apiKey(), nonce, signature);

    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setDate(now.toInstant().toEpochMilli());
    headers.add(HttpHeaders.AUTHORIZATION, authHeader);
    headers.add(HttpHeaders.USER_AGENT, USER_AGENT);
  }

  public HmacSignatureBuilder createSignatureBuilder(HttpServletRequest request) {
    val authHeader = getAuthHeader(request).orElseThrow(() -> new IllegalStateException("invalid signature"));
    return new HmacSignatureBuilder()
        .algorithm(authHeader.algorithm())
        .scheme(request.getScheme())
        .host(request.getServerName() + ":" + request.getServerPort())
        .method(request.getMethod())
        .resource(request.getRequestURI())
        .contentType(request.getContentType())
        .date(request.getHeader(HttpHeaders.DATE))
        .nonce(authHeader.nonce())
        .apiKey(authHeader.apiKey())
        .apiSecret(props.apiSecret());
  }

  public Optional<AuthHeader> getAuthHeader(HttpServletRequest request) {
    val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null) return Optional.empty();

    val authHeaderMatcher = AUTHORIZATION_HEADER_PATTERN.matcher(authHeader);
    if (!authHeaderMatcher.matches()) return Optional.empty();

    val algorithm = authHeaderMatcher.group(1);
    val apiKey = authHeaderMatcher.group(2);
    val nonce = authHeaderMatcher.group(3);
    val receivedDigest = authHeaderMatcher.group(4);

    val header = new AuthHeader(algorithm, apiKey, nonce, DatatypeConverter.parseBase64Binary(receivedDigest));
    return Optional.of(header);
  }

  private HmacSignatureBuilder createSignatureBuilder(String host, int port, String resource, String nonce, HttpMethod method, OffsetDateTime timestamp) {
    val headers = new HttpHeaders();
    headers.setDate(timestamp.toInstant().toEpochMilli());

    val dateString = headers.getFirst(HttpHeaders.DATE);
    val scheme = host.startsWith("https") ? "https" : "http";

    return new HmacSignatureBuilder()
        .algorithm(HmacSignatureBuilder.HMAC_SHA_512)
        .scheme(scheme)
        .host(format("%s:%d", host, port))
        .method(method.name())
        .resource(resource)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .date(dateString)
        .nonce(nonce)
        .apiKey(props.apiKey())
        .apiSecret(props.apiSecret());
    // .payload(valueAsString.getBytes(StandardCharsets.UTF_8));
  }

  private String host(String url) {
    try {
      return new URI(url).getHost();
    }
    catch (Exception e) {
      return "";
    }
  }

  private int port(String url) {
    try {
      return new URI(url).getPort();
    }
    catch (Exception e) {
      return 0;
    }
  }

  @Value
  @Accessors(fluent = true)
  @ConstructorBinding
  @ConfigurationProperties(prefix = "stratis.integration.mls2.rest.hmac")
  static class Properties {

    String apiKey;
    String apiSecret;
    boolean enforceHmac;

    Properties(
        String key,
        String secret,
        boolean enforce
    ) {
      this.apiKey = key;
      this.apiSecret = secret;
      this.enforceHmac = enforce;
    }
  }
}