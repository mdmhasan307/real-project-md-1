package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.UserType;
import mil.usmc.mls2.stratis.core.domain.model.UserTypeSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.UserTypeMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.UserTypeEntityRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class UserTypeRepositoryImpl implements UserTypeRepository {

  private final UserTypeEntityRepository entityRepository;
  private final UserTypeMapper mapper;

  @Override
  public Optional<UserType> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public List<UserType> getAll() {
    return entityRepository.findAll(Sort.by(Sort.Direction.ASC, "type")).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public List<UserType> search(UserTypeSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }
}