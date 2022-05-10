package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnection;
import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnectionSearchCriteria;

import java.util.Set;

public interface SiteRemoteConnectionService {

  Set<SiteRemoteConnection> search(SiteRemoteConnectionSearchCriteria criteria);

  SiteRemoteConnection getSiteForHost(String hostName);
}