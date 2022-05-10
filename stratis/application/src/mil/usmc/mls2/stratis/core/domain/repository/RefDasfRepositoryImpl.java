package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.RefDasf;
import mil.usmc.mls2.stratis.core.domain.model.RefDasfSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.RefDasfMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefDasfEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class RefDasfRepositoryImpl implements RefDasfRepository {

  private static final RefDasfMapper REF_DASF_MAPPER = RefDasfMapper.INSTANCE;
  private final RefDasfEntityRepository entityRepository;

  @Override
  public Set<RefDasf> search(RefDasfSearchCriteria criteria) {
    val results = entityRepository.search(criteria);
    return REF_DASF_MAPPER.entitySetToModelSet(results);
  }

  @Override
  public void save(RefDasf refDasf) {
    entityRepository.save(REF_DASF_MAPPER.modelToEntity(refDasf));
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

  public Set<RefDasf> findAll() {
    val results = entityRepository.findAll();
    return REF_DASF_MAPPER.entitySetToModelSet(new HashSet<>(results));
  }

  /**
   * Return a list of NIINs from the REF_DASF table that don't exist in NIIN Info
   */
  @Override
  public Set<String> findMissingNiins() {
    return entityRepository.findMissingNiins();
  }
}
