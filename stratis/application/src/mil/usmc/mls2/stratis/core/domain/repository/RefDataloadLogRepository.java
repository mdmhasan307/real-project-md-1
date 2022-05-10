package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLog;
import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLogSearchCriteria;

import java.util.Set;

public interface RefDataloadLogRepository {

  void save(RefDataloadLog refDataloadLog);

  Set<RefDataloadLog> search(RefDataloadLogSearchCriteria criteria);

  void delete(RefDataloadLog log);

  void delete(RefDataloadLogSearchCriteria criteria);
}