package mil.usmc.mls2.stratis.core.domain.repository.common;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.Mls2Sites;
import mil.usmc.mls2.stratis.core.domain.model.Mls2SitesSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.SiteStatusEnum;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.Mls2SitesMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository.Mls2SitesEntityRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("mls2SitesRepository")
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class Mls2SitesRepositoryImpl implements Mls2SitesRepository {

  private final Mls2SitesEntityRepository entityRepository;
  private final Mls2SitesMapper mapper;

  @Override
  public Long count(Mls2SitesSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public Long countActive() {
    val criteria = Mls2SitesSearchCriteria.builder()
        .statusId(SiteStatusEnum.ACTIVE.getId())
        .build();
    return entityRepository.count(criteria);
  }

  @Override
  public List<Mls2Sites> search(Mls2SitesSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Optional<Mls2Sites> getBySiteName(String siteName) {
    return entityRepository.getBySiteName(siteName).map(mapper::map);
  }

  @Override
  public List<Mls2Sites> getActiveSites() {
    val criteria = Mls2SitesSearchCriteria.builder()
        .statusId(SiteStatusEnum.ACTIVE.getId())
        .build();
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }
}
