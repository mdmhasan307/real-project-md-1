package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.SiteInfo;
import mil.usmc.mls2.stratis.core.domain.model.StaticSiteInfo;

import java.util.Optional;

public interface SiteInfoRepository {

  Optional<SiteInfo> getRecord();

  Optional<StaticSiteInfo> getStaticRecord();

  void save(SiteInfo siteInfo);
}