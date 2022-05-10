package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsDataSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.GcssMcImportsDataEntity;

import java.util.Optional;
import java.util.Set;

public interface GcssMcImportsDataEntityRepositoryCustom {

  Set<GcssMcImportsDataEntity> search(GcssMcImportsDataSearchCriteria gcssMcImportsDataSearchCriteria);

  Optional<GcssMcImportsDataEntity> getMostRecentRecordForProcessing(String interfaceName);

  Optional<GcssMcImportsDataEntity> getOldestRecordForProcessing(String interfaceName);
}
