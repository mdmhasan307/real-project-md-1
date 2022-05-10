package mil.usmc.mls2.stratis.core.domain.repository;
import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrack;
import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrackSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface SerialLotNumTrackRepository  {
  Optional<SerialLotNumTrack> findById(Integer id);

  List<SerialLotNumTrack> search(SerialLotNumTrackSearchCriteria criteria);

  void save(SerialLotNumTrack serial);

  void delete(SerialLotNumTrack serial);
}
