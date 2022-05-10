package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNum;
import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNumSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.PickSerialLotNumRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickSerialLotNumServiceImpl implements PickSerialLotNumService {

  private final PickSerialLotNumRepository pickSerialLotNumRepository;

  @Override
  public List<PickSerialLotNum> search(PickSerialLotNumSearchCriteria criteria) {
    return pickSerialLotNumRepository.search(criteria);
  }

  @Override
  @Transactional
  public void delete(PickSerialLotNum serial) {
    pickSerialLotNumRepository.delete(serial);
  }

  @Override
  @Transactional
  public void save(PickSerialLotNum serial) {
    pickSerialLotNumRepository.save(serial);
  }
}
