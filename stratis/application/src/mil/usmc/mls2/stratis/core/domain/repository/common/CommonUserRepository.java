package mil.usmc.mls2.stratis.core.domain.repository.common;

import mil.usmc.mls2.stratis.core.domain.model.CommonUser;
import mil.usmc.mls2.stratis.core.domain.model.CommonUserSearchCriteria;

import java.util.List;

public interface CommonUserRepository {

  List<CommonUser> search(CommonUserSearchCriteria criteria);
}
