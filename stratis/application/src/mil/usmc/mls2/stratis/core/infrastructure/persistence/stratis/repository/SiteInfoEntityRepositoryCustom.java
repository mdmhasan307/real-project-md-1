package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteInfoEntity;

import java.util.Optional;

public interface SiteInfoEntityRepositoryCustom {

  Optional<SiteInfoEntity> getRecord();
}
