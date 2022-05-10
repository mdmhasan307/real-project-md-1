package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.User;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.UserMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.UserEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class UserRepositoryImpl implements UserRepository {

  private final UserEntityRepository entityRepository;
  private final UserMapper mapper;

  @Override
  public Optional<User> findById(String id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  public Optional<User> findByEdipi(String edipi) {
    return entityRepository.findByEdipi(edipi).map(mapper::map);
  }
}