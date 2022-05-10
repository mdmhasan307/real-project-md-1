package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.service.common.SettingsSystemService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.TimeZone;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class SiteConfigurationIntegrationListener {

  private final TransactionCommands transactionCommands;

  @EventListener
  @SuppressWarnings("unused")
  public void onApplicationReady(ApplicationReadyEvent event) {
    transactionCommands.onApplicationReady();
  }

  @Slf4j
  @Component
  @RequiredArgsConstructor
  @Profile(Profiles.INTEGRATION_ANY)
  static class TransactionCommands {

    private final ConfigurableApplicationContext ctx;
    private final SettingsSystemService settingsSystemService;

    @Value("${stratis.startup.validateSettingsSystem}")
    private boolean validateSettingsSystem;

    public void onApplicationReady() {
      log.info("Site Configuration Listener");

      if (validateSettingsSystem) {
        try {
          log.debug("Checking SETTING_SYSTEM configuration...");
          // A StratisRuntimeException will be thrown if System Settings or Site UUID are in an invalid state.
          val systemUuid = settingsSystemService.getSystemUuidString();
          log.info("This system has a valid SETTINGS_SYSTEM entry and System UUID value: [{}].", systemUuid);
        }
        catch (Exception e) {
          ctx.close();
          log.error("Exception caught during SETTINGS_SYSTEM configuraiton check.", e);
          throw new StratisRuntimeException("SETTINGS_SYSTEM Configuration is not valid.");
        }
      }

      if (!validateSystemTimeZone()) {
        ctx.close();
        throw new StratisRuntimeException("JVM Default Timezone must be set to UTC in Integration Mode.");
      }

      log.info("Site Configuration Listener completed.  Site configured correctly.");
    }

    private boolean validateSystemTimeZone() {
      val tz = TimeZone.getDefault();
      return tz.getRawOffset() == 0;
    }
  }
}