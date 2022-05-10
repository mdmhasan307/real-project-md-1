package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.RefDasf;
import mil.usmc.mls2.stratis.core.domain.model.RefDasfSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.RefDasfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefDasfServiceImpl implements RefDasfService {

  private final RefDasfRepository refDasfRepository;

  @Override
  @Transactional
  public void save(RefDasf refDasf) {
    refDasfRepository.save(refDasf);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<RefDasf> search(RefDasfSearchCriteria criteria) {
    return refDasfRepository.search(criteria);
  }

  @Override
  @Transactional
  public void deleteAll() {
    refDasfRepository.deleteAll();
  }
}
