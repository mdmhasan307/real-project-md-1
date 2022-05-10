package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.ErrorQueue;
import mil.usmc.mls2.stratis.core.domain.model.ErrorQueueCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.ErrorQueueMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorQueueEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.ErrorQueueEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ErrorQueueRepositoryImpl implements ErrorQueueRepository {

  private final ErrorQueueEntityRepository entityRepository;
  private final ErrorQueueMapper mapper;

  @Override
  public List<ErrorQueue> search(ErrorQueueCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Long count(ErrorQueueCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public void save(ErrorQueue error) {
    val entity = entityRepository.findById(error.getErrorQueueId()).orElseGet(ErrorQueueEntity::new);
    apply(entity, error);
    entityRepository.saveAndFlush(entity);
  }

  @Override
  public void delete(ErrorQueue error) {
    val entity = entityRepository.findById(error.getErrorQueueId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to delete error queue for id %d", error.getErrorQueueId())));
    entityRepository.delete(entity);
  }

  //FIXME performance
  @Override
  public void delete(ErrorQueueCriteria criteria) {
    val results = entityRepository.search(criteria);
    results.forEach(entityRepository::delete);
  }

  private void apply(ErrorQueueEntity entity, ErrorQueue input) {
    entity.setErrorQueueId(input.getErrorQueueId());
    entity.setStatus(input.getStatus());
    entity.setCreatedBy(input.getCreatedBy());
    entity.setCreatedDate(input.getCreatedDate());
    entity.setEid(input.getEid());
    entity.setKeyType(input.getKeyType());
    entity.setKeyNum(input.getKeyNum());
    entity.setModifiedBy(input.getModifiedBy());
    entity.setModifiedDate(input.getModifiedDate());
    entity.setNotes(input.getNotes());
  }
}
