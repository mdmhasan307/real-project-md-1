package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.UserEntity;

import java.util.Optional;

public interface UserEntityRepositoryCustom {

  Optional<UserEntity> findByEdipi(String edipi);
}
