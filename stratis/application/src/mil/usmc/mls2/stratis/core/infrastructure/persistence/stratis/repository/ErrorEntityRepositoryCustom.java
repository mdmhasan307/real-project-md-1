package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.ErrorSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorEntity;

import java.util.List;

public interface ErrorEntityRepositoryCustom {

  List<ErrorEntity> search(ErrorSearchCriteria criteria);
}
