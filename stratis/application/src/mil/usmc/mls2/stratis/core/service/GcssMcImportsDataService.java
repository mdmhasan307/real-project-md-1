package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsData;
import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsDataSearchCriteria;

import java.util.Optional;
import java.util.Set;

public interface GcssMcImportsDataService {

  void save(GcssMcImportsData gcssMcImportsData);

  Set<GcssMcImportsData> search(GcssMcImportsDataSearchCriteria gcssMcImportsDataSearchCriteria);

  void delete(GcssMcImportsData data);

  Optional<GcssMcImportsData> getMostRecentRecordForProcessing(String interfaceName);

  Optional<GcssMcImportsData> getOldestRecordForProcessing(String interfaceName);

  void updateIgnoreAllPreviousDataByInterface(Integer gcssmcImportId, String interfaceName);
}