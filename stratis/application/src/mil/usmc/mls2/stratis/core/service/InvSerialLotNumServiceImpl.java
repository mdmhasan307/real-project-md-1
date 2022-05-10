package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.InvSerialLotNum;
import mil.usmc.mls2.stratis.core.domain.model.InvSerialLotNumSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.InvSerialLotNumRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvSerialLotNumServiceImpl implements InvSerialLotNumService {

  private final InvSerialLotNumRepository invSerialLotNumRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<InvSerialLotNum> findById(Integer id) {
    return invSerialLotNumRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Long count(InvSerialLotNumSearchCriteria criteria) {
    return invSerialLotNumRepository.count(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InvSerialLotNum> search(InvSerialLotNumSearchCriteria criteria) {
    return invSerialLotNumRepository.search(criteria);
  }

  @Override
  @Transactional
  public void save(InvSerialLotNum serial) {
    invSerialLotNumRepository.save(serial);
  }

  @Override
  @Transactional
  public void delete(InvSerialLotNum serial) {
    invSerialLotNumRepository.delete(serial);
  }
}
