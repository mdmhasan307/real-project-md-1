package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import mil.usmc.mls2.stratis.core.domain.model.Mls2SitesSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.Mls2SitesEntity;

import java.util.List;

public interface Mls2SitesEntityRepositoryCustom {

  Long count(Mls2SitesSearchCriteria criteria);

  List<Mls2SitesEntity> search(Mls2SitesSearchCriteria criteria);
}
