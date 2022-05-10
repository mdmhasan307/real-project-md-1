package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.RefGabf;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.RefGabfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefGabfServiceImpl implements RefGabfService {

  private final RefGabfRepository refGabfRepository;

  @Override
  public void callPopulateGcssReconHist() {
    refGabfRepository.callPopulateGcssReconHist();
  }

  @Override
  @Transactional
  public void save(RefGabf refGabf) {
    refGabfRepository.save(refGabf);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<RefGabf> search(RefGabfSearchCriteria criteria) {
    return refGabfRepository.search(criteria);
  }

  @Override
  @Transactional
  public void deleteAll() {
    refGabfRepository.deleteAll();
  }
}
