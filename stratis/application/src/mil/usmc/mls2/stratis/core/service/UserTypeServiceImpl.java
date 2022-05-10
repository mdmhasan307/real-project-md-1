package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.UserType;
import mil.usmc.mls2.stratis.core.domain.model.UserTypeSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.UserTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class UserTypeServiceImpl implements UserTypeService {

  private final UserTypeRepository userTypeRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<UserType> findById(Integer id) {
    return userTypeRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserType> getAll() {
    return userTypeRepository.getAll();
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserType> search(UserTypeSearchCriteria criteria) {
    return userTypeRepository.search(criteria);
  }
}
