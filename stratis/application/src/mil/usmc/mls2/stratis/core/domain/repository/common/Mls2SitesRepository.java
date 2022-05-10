package mil.usmc.mls2.stratis.core.domain.repository.common;

import mil.usmc.mls2.stratis.core.domain.model.Mls2Sites;
import mil.usmc.mls2.stratis.core.domain.model.Mls2SitesSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface Mls2SitesRepository {

  Long count(Mls2SitesSearchCriteria criteria);

  Long countActive();

  List<Mls2Sites> search(Mls2SitesSearchCriteria criteria);

  Optional<Mls2Sites> getBySiteName(String siteName);

  List<Mls2Sites> getActiveSites();
}
