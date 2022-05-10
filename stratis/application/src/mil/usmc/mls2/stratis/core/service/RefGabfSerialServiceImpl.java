package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerial;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerialSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.RefGabfSerialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefGabfSerialServiceImpl implements RefGabfSerialService {

  private final RefGabfSerialRepository refGabfSerialRepository;

  @Override
  @Transactional
  public void save(RefGabfSerial refGabfSerial) {
    refGabfSerialRepository.save(refGabfSerial);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<RefGabfSerial> search(RefGabfSerialSearchCriteria criteria) {
    return refGabfSerialRepository.search(criteria);
  }

  @Override
  @Transactional
  public void deleteAll() {
    refGabfSerialRepository.deleteAll();
  }
}
