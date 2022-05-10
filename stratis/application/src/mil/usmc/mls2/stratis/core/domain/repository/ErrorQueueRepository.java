package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.ErrorQueue;
import mil.usmc.mls2.stratis.core.domain.model.ErrorQueueCriteria;

import java.util.List;

public interface ErrorQueueRepository {

  List<ErrorQueue> search(ErrorQueueCriteria criteria);

  Long count(ErrorQueueCriteria criteria);

  void save(ErrorQueue error);

  void delete(ErrorQueue error);

  void delete(ErrorQueueCriteria criteria);
}
