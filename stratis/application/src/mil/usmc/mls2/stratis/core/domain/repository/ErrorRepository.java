package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.ErrorSearchCriteria;

import java.util.List;

public interface ErrorRepository {

  List<Error> search(ErrorSearchCriteria criteria);

  void save(Error error);
}
