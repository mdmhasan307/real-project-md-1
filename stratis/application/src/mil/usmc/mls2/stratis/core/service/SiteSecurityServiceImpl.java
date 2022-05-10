package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.SiteSecurity;
import mil.usmc.mls2.stratis.core.domain.model.SiteSecuritySearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.SiteSecurityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteSecurityServiceImpl implements SiteSecurityService {
    private final SiteSecurityRepository siteSecurityRepository;

    @Override
    public List<SiteSecurity> search(SiteSecuritySearchCriteria criteria) {
        return siteSecurityRepository.search(criteria);
    }

    @Override
    public Long count(SiteSecuritySearchCriteria criteria) {
        return siteSecurityRepository.count(criteria);
    }
}
