package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Spool;
import mil.usmc.mls2.stratis.core.domain.model.SpoolSearchCriteria;

import java.util.Optional;
import java.util.Set;

public interface SpoolService {

  void save(Spool spool);

  Set<Spool> search(SpoolSearchCriteria criteria);

  Optional<Spool> searchSingle(SpoolSearchCriteria criteria);

  Integer getSpoolBatchNumber();
  
  Long count(SpoolSearchCriteria criteria);
}