package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.service.SiteInfoService;
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
@Profile(Profiles.LEGACY)
public class SiteConfigurationStandardListener {

  private final TransactionCommands transactionCommands;

  @EventListener
  @SuppressWarnings("unused")
  public void onApplicationReady(ApplicationReadyEvent event) {
    transactionCommands.onApplicationReady();
  }

  @Slf4j
  @Component
  @RequiredArgsConstructor
  @Profile(Profiles.LEGACY)
  static class TransactionCommands {

    private final SiteInfoService siteInfoService;
    private final ConfigurableApplicationContext ctx;

    public void onApplicationReady() {
      log.info("Standard Site Configuration Listener");

      val siteInfo = siteInfoService.getStaticRecord();
      val siteTimeZone = TimeZone.getTimeZone(siteInfo.getSiteTimezone());

      if (!validateSystemTimeZone(siteTimeZone)) {
        ctx.close();
        throw new StratisRuntimeException(String.format("JVM Default Timezone must be set to %s in Standard Mode.", siteInfo.getSiteTimezone()));
      }

      log.info("Standard Site Configuration Listener completed.  Site configured correctly.");
    }

    private boolean validateSystemTimeZone(TimeZone siteTimeZone) {
      val tz = TimeZone.getDefault();
      return tz.getRawOffset() == siteTimeZone.getRawOffset();
    }
  }
}