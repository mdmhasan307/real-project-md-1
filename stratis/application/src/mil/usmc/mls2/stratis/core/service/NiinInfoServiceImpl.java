package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfo;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfoSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.NiinInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class NiinInfoServiceImpl implements NiinInfoService {

  private final NiinInfoRepository niinInfoRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<NiinInfo> findById(Integer id) {
    return niinInfoRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<NiinInfo> findByNiin(String niin) {
    return niinInfoRepository.findByNiin(niin);
  }

  @Override
  @Transactional
  public void save(NiinInfo location) { niinInfoRepository.save(location); }

  @Override
  @Transactional(readOnly = true)
  public Set<NiinInfo> search(NiinInfoSearchCriteria criteria) {
    return niinInfoRepository.search(criteria);
  }
}
