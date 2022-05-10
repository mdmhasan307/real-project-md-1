package mil.usmc.mls2.stratis.core.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.core.domain.model.SettingsSystem;
import mil.usmc.mls2.stratis.core.domain.repository.common.SettingsSystemRepository;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class SettingsSystemServiceImpl implements SettingsSystemService {

  private final SettingsSystemRepository settingsSystemRepository;

  private final Object lock = new Object();

  private static volatile UUID systemUuid;

  @Override
  @CommonDbTransaction(readOnly = true)
  public SettingsSystem getSettingsSystem() {
    return settingsSystemRepository.get();
  }

  @Override
  public UUID getSystemUuid() {
    synchronized (lock) {
      if (systemUuid == null) {
        systemUuid = getSettingsSystem().getSystemUuid();
      }
      return systemUuid;
    }
  }

  @Override
  public String getSystemUuidString() {
    return getSystemUuid().toString();
  }

  public UUID regenerateSystemUuid() {
    // Should this check actually be performed here?  (probably not, but... ADF)
    if (!SMVUtility.isIntegrationActive()) {
      log.info("The regenerateSystemUuid method was called, however the system is not in INTEGRATION mode.  No action required.");
      return null;
    }

    val settingsSystem = getSettingsSystem();

    val previousSystemUuid = settingsSystem.getSystemUuid();
    val newSystemUuid = UUID.randomUUID();

    settingsSystem.setSystemUuid(newSystemUuid);
    settingsSystemRepository.save(settingsSystem);

    // Reassign the cached internal systemUuid to the newly stored database value.
    systemUuid = newSystemUuid;

    log.info("Regenerated SETTINGS_SYSTEM.SYSTEM_UUID value.  Previous value: [{}], new value: [{}].", previousSystemUuid, newSystemUuid);
    return newSystemUuid;
  }
}
