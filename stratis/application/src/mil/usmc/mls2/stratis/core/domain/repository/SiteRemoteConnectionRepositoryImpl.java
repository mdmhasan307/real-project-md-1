package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnection;
import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnectionSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SiteRemoteConnectionMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.SiteRemoteConnectionEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class SiteRemoteConnectionRepositoryImpl implements SiteRemoteConnectionRepository {

  private static final SiteRemoteConnectionMapper SITE_REMOTE_CONNECTION_MAPPER = SiteRemoteConnectionMapper.INSTANCE;

  private final SiteRemoteConnectionEntityRepository siteRemoteConnectionEntityRepository;

  @Override
  public Set<SiteRemoteConnection> search(SiteRemoteConnectionSearchCriteria criteria) {
    return SITE_REMOTE_CONNECTION_MAPPER.entitySetToModelSet(siteRemoteConnectionEntityRepository.search(criteria));
  }
}