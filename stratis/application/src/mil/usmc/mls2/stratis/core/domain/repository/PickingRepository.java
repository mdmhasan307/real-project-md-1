package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Picking;
import mil.usmc.mls2.stratis.core.domain.model.PickingSearchCriteria;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PickingRepository {

  void save(Picking picking);

  Optional<Picking> findById(Integer id);

  Long count(PickingSearchCriteria criteria);

  List<Picking> search(PickingSearchCriteria criteria);

  List<Picking> getPicksForProcessing(PickingSearchCriteria criteria);

  List<Picking> findByScn(String scn);

  void delete(Picking pick);

  void deleteByScn(String scn);

  void deleteHistoryByScnAndDate(String scn, OffsetDateTime createdDate);

  Optional<Long> findCountOfPickingHistory(String scn, String status, OffsetDateTime createdDate);
}
