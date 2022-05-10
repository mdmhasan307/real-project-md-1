package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Spool;
import mil.usmc.mls2.stratis.core.domain.model.SpoolSearchCriteria;

import java.util.Optional;
import java.util.Set;

public interface SpoolRepository {

  Set<Spool> search(SpoolSearchCriteria criteria);

  Optional<Spool> searchSingle(SpoolSearchCriteria criteria);

  Long count(SpoolSearchCriteria criteria);

  Integer getSpoolBatchNumber();

  void save(Spool spool);
}