package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.SiteSecurity;
import mil.usmc.mls2.stratis.core.domain.model.SiteSecuritySearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SiteSecurityMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.SiteSecurityEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SiteSecurityRepositoryImpl implements SiteSecurityRepository {

  private final SiteSecurityEntityRepository entityRepository;
  private final SiteSecurityMapper mapper;

  @Override
  public List<SiteSecurity> search(SiteSecuritySearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Long count(SiteSecuritySearchCriteria criteria) {
    return entityRepository.count(criteria);
  }
}
