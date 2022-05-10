package mil.usmc.mls2.stratis.core.domain.repository.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.SettingsSystem;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SettingsSystemMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.SettingsSystemEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository.SettingsSystemEntityRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class SettingsSystemRepositoryImpl implements SettingsSystemRepository {

  private static final SettingsSystemMapper MAPPER = SettingsSystemMapper.INSTANCE;

  private final SettingsSystemEntityRepository entityRepository;

  public SettingsSystem get() {

    val settingsSystemList = findAll();

    // There should only be a single SETTINGS_SYSTEM record in the DB.  If there are more or less than 1 - throw an exception.
    if (CollectionUtils.isEmpty(settingsSystemList)) {
      throw new StratisRuntimeException("No SettingsSystem entities found in common database!");
    }
    if (settingsSystemList.size() != 1) {
      throw new StratisRuntimeException(String.format("Found an illegal number [%s] of SettingsSystem entities in common database!  Table should only have a single record.", settingsSystemList.size()));
    }

    return settingsSystemList.get(0);
  }

  private List<SettingsSystem> findAll() {
    return MAPPER.entityListToModelList(entityRepository.findAll());
  }

  @Override
  public void save(SettingsSystem settingsSystem) {
    // There should be exactly one SettingsSystem entity in the database.  If not available an exception should be thrown.
    val entity = entityRepository.findById(settingsSystem.getId()).orElseThrow(() -> new StratisRuntimeException("Could not find SETTINGS_SYSTEM entity."));
    apply(entity, settingsSystem);
    entityRepository.saveAndFlush(entity);
  }

  private void apply(SettingsSystemEntity entity, SettingsSystem settingsSystem) {
    // Do not overwrite entity id field.
    entity.setSystemUuid(settingsSystem.getSystemUuid());
  }
}