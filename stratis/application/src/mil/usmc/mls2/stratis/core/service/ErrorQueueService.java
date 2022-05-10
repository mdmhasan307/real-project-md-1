package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.ErrorQueue;
import mil.usmc.mls2.stratis.core.domain.model.ErrorQueueCriteria;

import java.util.List;

public interface ErrorQueueService {
  List<ErrorQueue> search(ErrorQueueCriteria criteria);

  Long count(ErrorQueueCriteria criteria);

  void save(ErrorQueue error);

  void delete(ErrorQueue error);
}
