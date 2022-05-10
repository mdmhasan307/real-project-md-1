package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.NiinLocationSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.NiinLocationEntity;

import java.util.List;

public interface NiinLocationEntityRepositoryCustom {

  long count(NiinLocationSearchCriteria criteria);

  List<NiinLocationEntity> search(NiinLocationSearchCriteria criteria);
}
