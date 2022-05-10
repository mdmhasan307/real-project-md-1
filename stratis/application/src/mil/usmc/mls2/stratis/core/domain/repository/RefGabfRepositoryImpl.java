package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.RefGabf;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.RefGabfMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefGabfEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefGabfRepositoryImpl implements RefGabfRepository {

  private static final RefGabfMapper REF_GABF_MAPPER = RefGabfMapper.INSTANCE;
  private final RefGabfEntityRepository entityRepository;

  @Override
  public void callPopulateGcssReconHist() {
    entityRepository.callPopulateGcssReconHist();
  }

  @Override
  public Set<RefGabf> search(RefGabfSearchCriteria criteria) {
    val results = entityRepository.search(criteria);
    return REF_GABF_MAPPER.entitySetToModelSet(results);
  }

  @Override
  public void save(RefGabf refGabf) {
    val result = entityRepository.save(REF_GABF_MAPPER.modelToEntity(refGabf));
    REF_GABF_MAPPER.entityToModel(result, refGabf);
  }

  @Override
  public void deleteAll() {
    entityRepository.deleteAll();
  }

  @Override
  public void truncate() {
    //FIXME refactor to use truncate
    entityRepository.deleteAll();
  }
}
