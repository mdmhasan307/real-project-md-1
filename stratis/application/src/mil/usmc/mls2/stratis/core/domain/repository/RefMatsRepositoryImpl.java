package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.RefMats;
import mil.usmc.mls2.stratis.core.domain.model.RefMatsSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.RefMatsMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefMatsEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class RefMatsRepositoryImpl implements RefMatsRepository {

  private static final RefMatsMapper REF_MATS_MAPPER = RefMatsMapper.INSTANCE;
  private final RefMatsEntityRepository entityRepository;

  @Override
  public Set<RefMats> search(RefMatsSearchCriteria criteria) {
    val results = entityRepository.search(criteria);
    return REF_MATS_MAPPER.entitySetToModelSet(results);
  }

  @Override
  public void save(RefMats refMats) {
    val result = entityRepository.saveAndFlush(REF_MATS_MAPPER.modelToEntity(refMats));
    REF_MATS_MAPPER.entityToModel(result, refMats);
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

  public Set<RefMats> findAll() {
    val results = entityRepository.findAll();
    return REF_MATS_MAPPER.entitySetToModelSet(new HashSet<>(results));
  }

  /**
   * Return a list of NIINs from the REF_MATS table that don't exist in NIIN Info
   */
  @Override
  public Set<String> findMissingNiins() {
    return entityRepository.findMissingNiins();
  }
}
