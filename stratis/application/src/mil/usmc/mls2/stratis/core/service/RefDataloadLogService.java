package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLog;
import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLogSearchCriteria;

import java.util.Set;

public interface RefDataloadLogService {

  void save(RefDataloadLog refDataloadLog);

  Set<RefDataloadLog> search(RefDataloadLogSearchCriteria refDataloadLogSearchCriteria);

  void delete(RefDataloadLog data);
}