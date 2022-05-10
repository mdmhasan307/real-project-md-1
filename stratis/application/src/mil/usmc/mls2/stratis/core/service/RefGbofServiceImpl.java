package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.RefGbof;
import mil.usmc.mls2.stratis.core.domain.model.RefGbofSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.RefGbofRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefGbofServiceImpl implements RefGbofService {

  private final RefGbofRepository refGbofRepository;

  @Override
  @Transactional
  public void save(RefGbof refGbof) {
    refGbofRepository.save(refGbof);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<RefGbof> search(RefGbofSearchCriteria criteria) {
    return refGbofRepository.search(criteria);
  }

  @Override
  @Transactional
  public void deleteAll() {
    refGbofRepository.deleteAll();
  }
}
