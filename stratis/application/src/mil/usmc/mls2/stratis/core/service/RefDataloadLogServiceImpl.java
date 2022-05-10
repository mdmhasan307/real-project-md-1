package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLog;
import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLogSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.RefDataloadLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefDataloadLogServiceImpl implements RefDataloadLogService {

  private final RefDataloadLogRepository refDataloadLogRepository;

  @Override
  @Transactional
  public void save(RefDataloadLog refDataloadLog) {
    refDataloadLogRepository.save(refDataloadLog);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<RefDataloadLog> search(RefDataloadLogSearchCriteria refDataloadLogSearchCriteria) {
    return refDataloadLogRepository.search(refDataloadLogSearchCriteria);
  }

  @Override
  @Transactional
  public void delete(RefDataloadLog data) {
    refDataloadLogRepository.delete(data);
  }
}