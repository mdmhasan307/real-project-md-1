package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.RefMhif;
import mil.usmc.mls2.stratis.core.domain.model.RefMhifSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.RefMhifRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefMhifServiceImpl implements RefMhifService {

  private final RefMhifRepository refMhifRepository;

  @Override
  public Set<RefMhif> search(RefMhifSearchCriteria criteria) {
    return refMhifRepository.search(criteria);
  }

  @Override
  @Transactional
  public void deleteAll() {
    refMhifRepository.deleteAll();
  }

  @Override
  @Transactional
  public void save(RefMhif refMhif) {
    refMhifRepository.save(refMhif);
  }

  @Override
  @Transactional
  public void callProcessMhif() {
    refMhifRepository.callProcessMhif();
  }
}
