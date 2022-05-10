package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.SiteSecuritySearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteSecurityEntity;

import java.util.List;

public interface SiteSecurityEntityRepositoryCustom {

  List<SiteSecurityEntity> search(SiteSecuritySearchCriteria criteria);

  Long count(SiteSecuritySearchCriteria criteria);
}
