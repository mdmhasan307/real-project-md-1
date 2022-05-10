package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.SiteSecurity;
import mil.usmc.mls2.stratis.core.domain.model.SiteSecuritySearchCriteria;

import java.util.List;

public interface SiteSecurityRepository {
    List<SiteSecurity> search(SiteSecuritySearchCriteria criteria);

    Long count(SiteSecuritySearchCriteria criteria);
}