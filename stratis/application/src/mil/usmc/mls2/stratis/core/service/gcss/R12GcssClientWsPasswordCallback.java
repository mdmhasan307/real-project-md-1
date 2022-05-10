package mil.usmc.mls2.stratis.core.service.gcss;

import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.util.PasswordRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

/**
 * Client Web Services Password Callback
 */
@Slf4j
@Component
@Profile(Profiles.LEGACY)
public class R12GcssClientWsPasswordCallback implements CallbackHandler {

  private final PasswordRegistry passwordRegistry = new PasswordRegistry();

  public R12GcssClientWsPasswordCallback(R12GcssFeedConstants gcssFeedConstants) {
    // User in WSS4JOutInterceptor props should match a keystore alias
    passwordRegistry.put(gcssFeedConstants.getSignatureAlias(), gcssFeedConstants.getKeyStorePass());
    passwordRegistry.put(gcssFeedConstants.getEncryptionAlias(), gcssFeedConstants.getTrustStorePass());
  }

  @Override
  public void handle(Callback[] callbacks) {
    passwordRegistry.handle(callbacks);
  }
}

