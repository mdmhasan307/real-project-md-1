package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.SettingsSystemEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EntityRepository;

import java.util.UUID;

public interface SettingsSystemEntityRepository  extends EntityRepository<SettingsSystemEntity, UUID>, SettingsSystemEntityRepositoryCustom {}