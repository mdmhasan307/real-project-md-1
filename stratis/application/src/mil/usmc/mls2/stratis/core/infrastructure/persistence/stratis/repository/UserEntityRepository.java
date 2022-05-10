package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.UserEntity;

public interface UserEntityRepository extends EntityRepository<UserEntity, String>, UserEntityRepositoryCustom {

}
