package mil.usmc.mls2.stratis.core.domain.repository.common;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.CommonUser;
import mil.usmc.mls2.stratis.core.domain.model.CommonUserSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.CommonUserMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository.CommonUserEntityRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class CommonUserRepositoryImpl implements CommonUserRepository {

  private final CommonUserEntityRepository entityRepository;
  private final CommonUserMapper mapper;

  @Override
  public List<CommonUser> search(CommonUserSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }
}
