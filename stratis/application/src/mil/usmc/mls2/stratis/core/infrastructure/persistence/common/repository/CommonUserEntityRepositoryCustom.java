package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import mil.usmc.mls2.stratis.core.domain.model.CommonUserSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.CommonUserEntity;

import java.util.List;

public interface CommonUserEntityRepositoryCustom {

  List<CommonUserEntity> search(CommonUserSearchCriteria criteria);
}
