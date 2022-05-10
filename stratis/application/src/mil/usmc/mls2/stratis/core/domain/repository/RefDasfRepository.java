package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefDasf;
import mil.usmc.mls2.stratis.core.domain.model.RefDasfSearchCriteria;

import java.util.Set;

public interface RefDasfRepository {

  Set<RefDasf> search(RefDasfSearchCriteria criteria);

  void save(RefDasf refDasf);

  void deleteAll();

  void truncate();

  Set<RefDasf> findAll();

  Set<String> findMissingNiins();
}
