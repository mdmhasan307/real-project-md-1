package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrack;
import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrackSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface SerialLotNumTrackService {

  Optional<SerialLotNumTrack> findById(Integer id);

  List<SerialLotNumTrack> search(SerialLotNumTrackSearchCriteria criteria);

  void update(SerialLotNumTrack serial);

  void delete(SerialLotNumTrack serial);
}
