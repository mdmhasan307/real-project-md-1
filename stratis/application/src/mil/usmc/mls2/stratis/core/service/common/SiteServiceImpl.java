package mil.usmc.mls2.stratis.core.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.core.domain.model.Site;
import mil.usmc.mls2.stratis.core.domain.model.SiteSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.common.SiteRepository;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Note that no context-specific properties are supplied to the services
 * These services are client-agnostic, with no knowledge or access to HttpServletRequest, Response, JSFContext, etc.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
@SuppressWarnings({"Duplicates", "squid:S1192"})
public class SiteServiceImpl implements SiteService {

  private final SiteRepository siteRepository;

  @Override
  @CommonDbTransaction(readOnly = true)
  public List<Site> search(SiteSearchCriteria criteria) {
    return siteRepository.search(criteria);
  }

  @Override
  @CommonDbTransaction(readOnly = true)
  public List<Site> findAll() {
    val criteria = SiteSearchCriteria.builder().build();
    criteria.setSortColumn("siteName");
    return siteRepository.search(criteria);
  }
}