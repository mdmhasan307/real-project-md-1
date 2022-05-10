package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.IssueSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.IssueEntity;

import java.util.List;

public interface IssueEntityRepositoryCustom {

  long count(IssueSearchCriteria criteria);

  List<IssueEntity> search(IssueSearchCriteria criteria);
}
