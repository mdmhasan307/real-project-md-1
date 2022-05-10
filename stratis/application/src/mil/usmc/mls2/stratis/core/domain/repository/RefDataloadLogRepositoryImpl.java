package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLog;
import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLogSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.RefDataloadLogMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefDataloadLogEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefDataloadLogEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class RefDataloadLogRepositoryImpl implements RefDataloadLogRepository {

  private static final RefDataloadLogMapper REF_DATALOAD_LOG_MAPPER = RefDataloadLogMapper.INSTANCE;

  private final RefDataloadLogEntityRepository refDataloadLogEntityRepository;

  @Override
  public void save(RefDataloadLog refDataloadLog) {
    RefDataloadLogEntity entity;
    if (refDataloadLog.getRefDataloadLogId() == null) {
      entity = new RefDataloadLogEntity();
    }
    else {
      entity = refDataloadLogEntityRepository.findById(refDataloadLog.getRefDataloadLogId()).orElseGet(RefDataloadLogEntity::new);
    }
    REF_DATALOAD_LOG_MAPPER.modelToEntity(refDataloadLog, entity);
    refDataloadLogEntityRepository.saveAndFlush(entity);
  }

  @Override
  public Set<RefDataloadLog> search(RefDataloadLogSearchCriteria criteria) {
    val results = refDataloadLogEntityRepository.search(criteria);
    return REF_DATALOAD_LOG_MAPPER.entitySetToModelSet(results);
  }

  @Override
  public void delete(RefDataloadLog data) {
    refDataloadLogEntityRepository.delete(REF_DATALOAD_LOG_MAPPER.modelToEntity(data));
  }

  //FIXME performance
  @Override
  public void delete(RefDataloadLogSearchCriteria criteria) {
    val results = refDataloadLogEntityRepository.search(criteria);
    results.forEach(refDataloadLogEntityRepository::delete);
  }
}