package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.RefGbof;
import mil.usmc.mls2.stratis.core.domain.model.RefGbofSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.RefGbofMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefGbofEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefGbofRepositoryImpl implements RefGbofRepository {

  private static final RefGbofMapper REF_GBOF_MAPPER = RefGbofMapper.INSTANCE;
  private final RefGbofEntityRepository entityRepository;

  @Override
  public Set<RefGbof> search(RefGbofSearchCriteria criteria) {
    val results = entityRepository.search(criteria);
    return REF_GBOF_MAPPER.entitySetToModelSet(results);
  }

  @Override
  public void save(RefGbof RefGbof) {
    val result = entityRepository.save(REF_GBOF_MAPPER.modelToEntity(RefGbof));
    REF_GBOF_MAPPER.entityToModel(result, RefGbof);
  }

  @Override
  public void deleteAll() {
    entityRepository.deleteAll();
  }
}
