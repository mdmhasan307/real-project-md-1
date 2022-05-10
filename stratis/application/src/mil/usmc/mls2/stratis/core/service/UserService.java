package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.User;

import java.util.Optional;

public interface UserService {

  Optional<User> findById(String id);

  Optional<User> findByEdipi(String edipi);

  long getGroupPrivsForUser(int userId, int userTypeId);
}
