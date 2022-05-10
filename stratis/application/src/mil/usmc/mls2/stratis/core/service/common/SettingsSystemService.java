package mil.usmc.mls2.stratis.core.service.common;

import mil.usmc.mls2.stratis.core.domain.model.SettingsSystem;

import java.util.UUID;

public interface SettingsSystemService {

  SettingsSystem getSettingsSystem();

  UUID getSystemUuid();

  String getSystemUuidString();

  UUID regenerateSystemUuid();
}