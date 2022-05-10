package gcssmcws.stratisgcssmcwebserviceserver;

import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.crypto.WsUtils;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

@Slf4j
public class TruststorePasswordCallback implements CallbackHandler {

  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for (Callback callback : callbacks) {
      if (callback instanceof KeyStoreCallback) {
        try (InputStream is = new FileInputStream(WsUtils.getConfiguration(WsUtils.TRUSTSTORE_PATH_CONFIG))) {
          KeyStore ks = KeyStore.getInstance("JKS");
          String key = WsUtils.getConfiguration(WsUtils.TRUSTSTORE_PASS_CONFIG);
          ks.load(is, key.toCharArray());
          ((KeyStoreCallback) callback).setKeystore(ks);
        }
        catch (Exception e) {
          log.error("Error opening ws truststore", e);
          throw new IOException("Error opening ws truststore", e);
        }
      }
      else {
        throw new UnsupportedCallbackException(callback, "unknown callback");
      }
    }
  }
}
