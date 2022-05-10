package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.CustomerEntity;

import java.util.Optional;

public interface CustomerEntityRepository extends EntityRepository<CustomerEntity, Integer>, CustomerEntityRepositoryCustom {

  Optional<CustomerEntity> getByAac(String aac);
}
