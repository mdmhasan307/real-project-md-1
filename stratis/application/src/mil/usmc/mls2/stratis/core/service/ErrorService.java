package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.ErrorSearchCriteria;

import java.util.List;

public interface ErrorService {
  Error findByCode(String code);

  List<Error> search(ErrorSearchCriteria criteria);

  void save(Error error);
}
