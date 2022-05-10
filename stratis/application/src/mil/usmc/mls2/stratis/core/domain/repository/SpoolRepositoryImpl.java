package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.Spool;
import mil.usmc.mls2.stratis.core.domain.model.SpoolSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SpoolMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SpoolEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.SpoolEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class SpoolRepositoryImpl implements SpoolRepository {

  private static final SpoolMapper SPOOL_MAPPER = SpoolMapper.INSTANCE;

  private final SpoolEntityRepository spoolEntityRepository;

  @Override
  public void save(Spool spool) {
    SpoolEntity entity;
    if (spool.getSpoolId() == null) {
      entity = new SpoolEntity();
    }
    else {
      entity = spoolEntityRepository.findById(spool.getSpoolId()).orElseGet(SpoolEntity::new);
    }
    SPOOL_MAPPER.modelToEntity(spool, entity);
    val result = spoolEntityRepository.saveAndFlush(entity);
    SPOOL_MAPPER.entityToModel(result, spool); //updates the spool object that was passed in with data triggered/updated in database (sequence value on inserts)
  }

  @Override
  public Set<Spool> search(SpoolSearchCriteria criteria) {
    return SPOOL_MAPPER.entitySetToModelSet(spoolEntityRepository.search(criteria));
  }

  @Override
  public Integer getSpoolBatchNumber() {
    return spoolEntityRepository.getSpoolBatchNumber();
  }

  @Override
  public Optional<Spool> searchSingle(SpoolSearchCriteria criteria) {
    val result = spoolEntityRepository.searchSingle(criteria);
    return result.map(SPOOL_MAPPER::entityToModel);
  }

  @Override
  public Long count(SpoolSearchCriteria criteria) {
    return spoolEntityRepository.count(criteria);
  }
}