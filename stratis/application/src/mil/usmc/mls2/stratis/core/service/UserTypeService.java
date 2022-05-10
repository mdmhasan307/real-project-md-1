package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.UserType;
import mil.usmc.mls2.stratis.core.domain.model.UserTypeSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface UserTypeService {

  Optional<UserType> findById(Integer id);

  List<UserType> getAll();

  List<UserType> search(UserTypeSearchCriteria criteria);
}
