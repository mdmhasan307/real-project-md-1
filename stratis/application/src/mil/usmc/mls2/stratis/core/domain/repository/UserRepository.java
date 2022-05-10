package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.User;

import java.util.Optional;

public interface UserRepository {

  Optional<User> findById(String id);

  Optional<User> findByEdipi(String edipi);
}
