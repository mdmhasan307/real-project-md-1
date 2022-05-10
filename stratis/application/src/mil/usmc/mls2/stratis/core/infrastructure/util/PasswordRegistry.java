package mil.usmc.mls2.stratis.core.infrastructure.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PasswordRegistry implements CallbackHandler {

  private static final Map<String, String> aliasPwd = new HashMap<>();

  public void put(String alias, String pwd) {
    aliasPwd.put(alias, pwd);
  }

  public String get(String alias) {
    return aliasPwd.get(alias);
  }

  public void remove(String alias) {
    aliasPwd.remove(alias);
  }

  public void handle(Callback[] callbacks) {
    for (val callback : callbacks) {
      val pc = (WSPasswordCallback) callback;
      val pass = aliasPwd.get(pc.getIdentifier());

      if (pass != null) {
        log.trace("found pass for pc identifier '{}'", pc.getIdentifier());
        pc.setPassword(pass);
      }
      else {
        log.warn("failed to find entry for p-callback [identifier: {}].", pc.getIdentifier());
      }

      log.trace("callback getIdentifier: {} -> getUsage: {}", pc.getIdentifier(), pc.getUsage());
    }
  }
}