package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.RefMats;
import mil.usmc.mls2.stratis.core.domain.model.RefMatsSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.RefMatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefMatsServiceImpl implements RefMatsService {

  private final RefMatsRepository refMatsRepository;

  @Override
  @Transactional
  public void save(RefMats refMats) {
    refMatsRepository.save(refMats);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<RefMats> search(RefMatsSearchCriteria criteria) {
    return refMatsRepository.search(criteria);
  }

  @Override
  @Transactional
  public void deleteAll() {
    refMatsRepository.deleteAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Set<RefMats> findAll() {
    return refMatsRepository.findAll();
  }
}
