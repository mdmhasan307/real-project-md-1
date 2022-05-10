package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.CommonUserCompositeKey;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.CommonUserEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EntityRepository;

public interface CommonUserEntityRepository extends EntityRepository<CommonUserEntity, CommonUserCompositeKey>, CommonUserEntityRepositoryCustom {}
