package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.SiteSecurity;
import mil.usmc.mls2.stratis.core.domain.model.SiteSecuritySearchCriteria;

import java.util.List;

public interface SiteSecurityService {
    List<SiteSecurity> search(SiteSecuritySearchCriteria criteria);

    Long count(SiteSecuritySearchCriteria criteria);
}
