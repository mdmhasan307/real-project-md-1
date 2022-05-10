package mil.usmc.mls2.stratis.core.service.common;

import mil.usmc.mls2.stratis.core.domain.model.Site;
import mil.usmc.mls2.stratis.core.domain.model.SiteSearchCriteria;

import java.util.List;

/**
 * Note that no context-specific properties are supplied to the services
 * These services are client-agnostic, with no knowledge or access to HttpServletRequest, Response, JSFContext, etc.)
 */
public interface SiteService {

  List<Site> search(SiteSearchCriteria criteria);

  List<Site> findAll();
}
