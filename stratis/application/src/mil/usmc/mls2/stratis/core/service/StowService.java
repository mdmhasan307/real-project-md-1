package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Stow;
import mil.usmc.mls2.stratis.core.domain.model.StowSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.Wac;

import java.util.List;
import java.util.Optional;

public interface StowService {

  Optional<Stow> findById(Integer id);

  Long count(StowSearchCriteria criteria);

  List<Stow> search(StowSearchCriteria criteria);

  List<Stow> createAssignedListOfStowsForUser(StowSearchCriteria criteria, Integer uid, Wac wac, Long limit);

  void update(Stow stow);

  Integer updateStowScannedFlag(Wac wac, String sid, Integer userId, boolean isMobile);

  void deAssignAllStowsForUser(Integer userId);

  void delete(Stow stow);
}
