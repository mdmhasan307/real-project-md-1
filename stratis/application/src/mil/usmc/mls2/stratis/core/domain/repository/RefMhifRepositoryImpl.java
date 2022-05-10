package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.RefMhif;
import mil.usmc.mls2.stratis.core.domain.model.RefMhifSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.RefMhifMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefMhifEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefMhifRepositoryImpl implements RefMhifRepository {

  private static final RefMhifMapper REF_MHIF_MAPPER = RefMhifMapper.INSTANCE;

  private final RefMhifEntityRepository entityRepository;

  @Override
  public Set<RefMhif> search(RefMhifSearchCriteria criteria) {
    val results = entityRepository.search(criteria);
    return REF_MHIF_MAPPER.entitySetToModelSet(results);
  }

  @Override
  public void save(RefMhif refMhif) {
    val result = entityRepository.save(REF_MHIF_MAPPER.modelToEntity(refMhif));
    REF_MHIF_MAPPER.entityToModel(result, refMhif);
  }

  @Override
  public void deleteAll() {
    entityRepository.deleteAll();
  }

  @Override
  public void callProcessMhif() {
    entityRepository.callProcessMhif();
  }
}
