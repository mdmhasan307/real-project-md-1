package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.SiteInterface;

import java.util.Optional;

public interface SiteInterfaceRepository {

  Optional<SiteInterface> getByInterfaceName(String interfaceName);

  void save(SiteInterface siteInterface);
}