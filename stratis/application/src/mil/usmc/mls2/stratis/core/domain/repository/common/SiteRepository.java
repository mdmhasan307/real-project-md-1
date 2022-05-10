package mil.usmc.mls2.stratis.core.domain.repository.common;

import mil.usmc.mls2.stratis.core.domain.model.Site;
import mil.usmc.mls2.stratis.core.domain.model.SiteSearchCriteria;

import java.util.List;

public interface SiteRepository {

  List<Site> search(SiteSearchCriteria criteria);

  List<Site> getActiveSites();
}
