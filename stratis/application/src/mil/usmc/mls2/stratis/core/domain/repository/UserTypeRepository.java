package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.UserType;
import mil.usmc.mls2.stratis.core.domain.model.UserTypeSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface UserTypeRepository {

  Optional<UserType> findById(Integer id);

  List<UserType> getAll();

  List<UserType> search(UserTypeSearchCriteria criteria);
}
