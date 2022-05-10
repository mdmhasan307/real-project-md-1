package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnection;
import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnectionSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.SiteRemoteConnectionRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteRemoteConnectionServiceImpl implements SiteRemoteConnectionService {

  private final SiteRemoteConnectionRepository siteRemoteConnectionRepository;

  @Override
  @Transactional(readOnly = true)
  public Set<SiteRemoteConnection> search(SiteRemoteConnectionSearchCriteria criteria) {
    return siteRemoteConnectionRepository.search(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public SiteRemoteConnection getSiteForHost(String hostName) {
    val criteria = SiteRemoteConnectionSearchCriteria.builder()
        .hostName(hostName)
        .build();
    val results = search(criteria);
    if (CollectionUtils.isNotEmpty(results)) {
      if (results.size() > 1) {
        throw new StratisRuntimeException(String.format("More then one Site Remote Connection found for %s", hostName));
      }
      return results.iterator().next();
    }
    throw new StratisRuntimeException(String.format("Not Site Remote Connection found for %s", hostName));
  }
}