package gcssmcws.stratisgcssmcwebserviceserver;

import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import com.sun.xml.wss.impl.callback.PrivateKeyCallback;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.crypto.WsUtils;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;

@Slf4j
public class KeystorePasswordCallback implements CallbackHandler {

  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for (Callback callback : callbacks) {
      if (callback instanceof KeyStoreCallback) {
        try (InputStream is = new FileInputStream(WsUtils.getConfiguration(WsUtils.KEYSTORE_PATH_CONFIG))) {
          KeyStore ks = KeyStore.getInstance("JKS");
          String key = WsUtils.getConfiguration(WsUtils.KEYSTORE_PASS_CONFIG);
          ks.load(is, key.toCharArray());
          KeyStoreCallback kscb = ((KeyStoreCallback) callback);
          kscb.setKeystore(ks);
        }
        catch (Exception e) {
          log.error("Error opening ws keystore", e);
          throw new IOException("Error opening ws keystore", e);
        }
      }
      else if (callback instanceof PrivateKeyCallback) {
        try {
          String key = WsUtils.getConfiguration(WsUtils.KEYSTORE_PASS_CONFIG);
          String alias = WsUtils.getConfiguration(WsUtils.KEYSTORE_ALIAS_CONFIG);
          PrivateKeyCallback pkcb = ((PrivateKeyCallback) callback);
          pkcb.setAlias(alias);
          pkcb.setKey((PrivateKey) pkcb.getKeystore().getKey(alias, key.toCharArray()));
        }
        catch (Exception e) {
          log.error("Error getting ws keystore private key", e);
          throw new IOException("Error getting ws keystore private key", e);
        }
      }
      else {
        throw new UnsupportedCallbackException(callback, "unknown callback");
      }
    }
  }
}
