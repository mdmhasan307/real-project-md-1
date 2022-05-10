package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefUiSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefUiEntity;

import java.util.List;

public interface RefUiEntityRepositoryCustom {

  long count(RefUiSearchCriteria criteria);

  List<RefUiEntity> search(RefUiSearchCriteria criteria);
}
