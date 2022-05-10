package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnectionSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteRemoteConnectionEntity;

import java.util.Set;

public interface SiteRemoteConnectionEntityRepositoryCustom {

  Set<SiteRemoteConnectionEntity> search(SiteRemoteConnectionSearchCriteria criteria);
}
