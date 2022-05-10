package mil.usmc.mls2.stratis.core.infrastructure.util;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Slf4j
@SuppressWarnings("findsecbugs:WEAK_TRUST_MANAGER") // only used in development and debugging situations
public class BlindTrustManager implements X509TrustManager {

  public X509Certificate[] getAcceptedIssuers() {
    return null; // do not return an empty chain as that is interpreted differently from a null.
  }

  public void checkClientTrusted(X509Certificate[] chain, String authType) {
    log.info("client is trusted.");
    // default
  }

  public void checkServerTrusted(X509Certificate[] chain, String authType) {
    log.info("server is trusted.");
    // default
  }
}
