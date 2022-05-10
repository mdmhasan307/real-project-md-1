package mil.usmc.mls2.stratis.core.domain.repository.common;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.Site;
import mil.usmc.mls2.stratis.core.domain.model.SiteSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.SiteStatusEnum;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SiteMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository.SiteEntityRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class SiteRepositoryImpl implements SiteRepository {

  private final SiteEntityRepository entityRepository;
  private final SiteMapper mapper;

  @Override
  public List<Site> search(SiteSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public List<Site> getActiveSites() {
    val criteria = SiteSearchCriteria.builder()
        .statusId(SiteStatusEnum.ACTIVE.getId())
        .build();
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }
}
