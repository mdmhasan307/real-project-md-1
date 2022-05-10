package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import mil.usmc.mls2.stratis.core.domain.model.SiteSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.SiteEntity;

import java.util.List;

public interface SiteEntityRepositoryCustom {

  List<SiteEntity> search(SiteSearchCriteria criteria);
}
