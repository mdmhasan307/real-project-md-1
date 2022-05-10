package mil.usmc.mls2.stratis.core.domain.repository.common;

import mil.usmc.mls2.stratis.core.domain.model.SettingsSystem;

public interface SettingsSystemRepository {

  SettingsSystem get();

  void save(SettingsSystem settingsSystem);
}