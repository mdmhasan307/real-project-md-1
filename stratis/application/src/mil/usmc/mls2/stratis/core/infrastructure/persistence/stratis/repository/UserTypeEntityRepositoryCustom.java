package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.UserTypeSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.UserTypeEntity;

import java.util.List;

public interface UserTypeEntityRepositoryCustom {

  List<UserTypeEntity> search(UserTypeSearchCriteria criteria);
}
