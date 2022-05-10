package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnection;
import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnectionSearchCriteria;

import java.util.Set;

public interface SiteRemoteConnectionRepository {

  Set<SiteRemoteConnection> search(SiteRemoteConnectionSearchCriteria criteria);
}