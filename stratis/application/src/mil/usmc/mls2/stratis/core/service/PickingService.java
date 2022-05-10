package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Picking;
import mil.usmc.mls2.stratis.core.domain.model.PickingSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.Wac;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PickingService {

  Optional<Picking> findById(Integer id);

  Long count(PickingSearchCriteria criteria);

  List<Picking> search(PickingSearchCriteria criteria);

  List<Picking> getPickingListForProcessing(PickingSearchCriteria criteria, Wac wac);

  void deAssignAllPicksForUser(Integer userId);

  void save(Picking pick);

  void delete(Picking pick);

  void deleteByScn(String scn);

  void deleteHistoryByScnAndDate(String scn, OffsetDateTime createdDate);

  Optional<Long> findCountOfPickingHistory(String scn, String status, OffsetDateTime createdDate);
}
