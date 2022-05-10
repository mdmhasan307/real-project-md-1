package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.SiteInfo;
import mil.usmc.mls2.stratis.core.domain.model.StaticSiteInfo;

public interface SiteInfoService {

  SiteInfo getRecord();

  StaticSiteInfo getStaticRecord();

  void save(SiteInfo siteInfo);
}