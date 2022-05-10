package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefUi;
import mil.usmc.mls2.stratis.core.domain.model.RefUiSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface RefUiRepository {

  Optional<RefUi> findById(Integer id);

  Long count(RefUiSearchCriteria criteria);

  List<RefUi> search(RefUiSearchCriteria criteria);
}
