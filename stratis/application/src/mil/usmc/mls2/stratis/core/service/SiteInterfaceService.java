package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.SiteInterface;

import java.util.Optional;

public interface SiteInterfaceService {

  Optional<SiteInterface> getByInterfaceName(String interfaceName);

  void save(SiteInterface siteInterface);
}