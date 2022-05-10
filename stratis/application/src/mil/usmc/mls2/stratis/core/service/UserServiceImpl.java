package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.User;
import mil.usmc.mls2.stratis.core.domain.repository.UserRepository;
import mil.usmc.mls2.stratis.core.infrastructure.dao.UserDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserDao userDao;

  @Override
  @Transactional(readOnly = true)
  public long getGroupPrivsForUser(int userId, int userTypeId) {
    return userDao.getGroupPrivsForUser(userId, userTypeId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(String id) {
    return userRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEdipi(String edipi) {
    return userRepository.findByEdipi(edipi);
  }
}
