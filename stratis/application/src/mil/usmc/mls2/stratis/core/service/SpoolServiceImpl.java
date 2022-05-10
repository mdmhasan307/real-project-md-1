package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.Spool;
import mil.usmc.mls2.stratis.core.domain.model.SpoolSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.SpoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpoolServiceImpl implements SpoolService {

  private final SpoolRepository spoolRepository;

  @Override
  public void save(Spool spool) {
    spoolRepository.save(spool);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Spool> search(SpoolSearchCriteria criteria) {
    return spoolRepository.search(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Spool> searchSingle(SpoolSearchCriteria criteria) {
    return spoolRepository.searchSingle(criteria);
  }

  @Override
  @Transactional()
  public Integer getSpoolBatchNumber() {
    return spoolRepository.getSpoolBatchNumber();
  }

  @Override
  @Transactional(readOnly = true)
  public Long count(SpoolSearchCriteria criteria) {
    return spoolRepository.count(criteria);
  }
}