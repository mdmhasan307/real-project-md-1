package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.ErrorQueue;
import mil.usmc.mls2.stratis.core.domain.model.ErrorQueueCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.ErrorQueueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorQueueServiceImpl implements ErrorQueueService {

  private final ErrorQueueRepository errorQueueRepository;

  @Override
  @Transactional(readOnly = true)
  public List<ErrorQueue> search(ErrorQueueCriteria criteria) { return errorQueueRepository.search(criteria); }

  @Override
  @Transactional(readOnly = true)
  public Long count(ErrorQueueCriteria criteria) {
    return errorQueueRepository.count(criteria);
  }

  @Override
  @Transactional
  public void save(ErrorQueue error) { errorQueueRepository.save(error); }

  @Override
  @Transactional
  public void delete(ErrorQueue error) { errorQueueRepository.delete(error); }
}
