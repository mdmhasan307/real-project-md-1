package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorQueueEntity;

public interface ErrorQueueEntityRepository extends EntityRepository<ErrorQueueEntity,Integer>, ErrorQueueEntityRepositoryCustom {}
