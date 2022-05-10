package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.ErrorSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.ErrorMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.ErrorEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ErrorRepositoryImpl implements ErrorRepository {

  private final ErrorEntityRepository entityRepository;
  private final ErrorMapper mapper;

  @Override
  public List<Error> search(ErrorSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public void save(Error error) {
    val entity = entityRepository.findById(error.getId()).orElseGet(ErrorEntity::new);
    apply(entity, error);
    entityRepository.saveAndFlush(entity);
  }

  private void apply(ErrorEntity entity, Error input) {
    entity.setId(input.getId());
    entity.setCode(input.getCode());
    entity.setDescription(input.getDescription());
    entity.setLabel(input.getLabel());
    entity.setSecurityLevel(input.getSecurityLevel());
    entity.setTitle(input.getTitle());
  }
}
