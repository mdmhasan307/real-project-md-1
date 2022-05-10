package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Issue;
import mil.usmc.mls2.stratis.core.domain.model.IssueSearchCriteria;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface IssueService {

  Optional<Issue> findByScn(String scn);

  void save(Issue issue);

  Long count(IssueSearchCriteria criteria);

  List<Issue> search(IssueSearchCriteria criteria);

  void delete(Issue issue);

  void deleteByScn(String scn);

  void deleteHistoryByScnAndDate(String scn, OffsetDateTime createdDate);

  void deleteMultiple(IssueSearchCriteria criteria);

  long findCountOfBackOrdersForGbofProcessing(String documentNumber);

  long getHistoryCount(String docNumber, String suffix, String issueType);

  String getNextIssueScn(boolean isWalkThru);
}
