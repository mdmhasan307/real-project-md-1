package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;


import mil.usmc.mls2.stratis.core.domain.model.ErrorQueueCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorQueueEntity;

import java.util.List;


public interface ErrorQueueEntityRepositoryCustom {
  List<ErrorQueueEntity> search(ErrorQueueCriteria criteria);

  Long count(ErrorQueueCriteria criteria);
}
