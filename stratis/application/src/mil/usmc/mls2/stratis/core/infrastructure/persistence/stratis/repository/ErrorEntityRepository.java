package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorEntity;

public interface ErrorEntityRepository extends EntityRepository<ErrorEntity, Integer>, ErrorEntityRepositoryCustom {}
