package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrack;
import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrackSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.SerialLotNumTrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SerialLotNumTrackServiceImpl implements SerialLotNumTrackService {

  private final SerialLotNumTrackRepository serialLotNumTrackRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<SerialLotNumTrack> findById(Integer id) {
    return serialLotNumTrackRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SerialLotNumTrack> search(SerialLotNumTrackSearchCriteria criteria) {
    return serialLotNumTrackRepository.search(criteria);
  }

  @Override
  @Transactional
  public void update(SerialLotNumTrack serial) {
    serialLotNumTrackRepository.save(serial);
  }

  @Override
  @Transactional
  public void delete(SerialLotNumTrack serial) {
    serialLotNumTrackRepository.delete(serial);
  }
}
