package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Stow;
import mil.usmc.mls2.stratis.core.domain.model.StowSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface StowRepository {

  Optional<Stow> findById(Integer id);

  Long count(StowSearchCriteria criteria);

  List<Stow> search(StowSearchCriteria criteria);

  void save(Stow stow);

  void delete(Stow stow);
}
