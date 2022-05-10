package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.usmc.mls2.stratis.core.domain.model.Issue;
import mil.usmc.mls2.stratis.core.domain.model.IssueSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.IssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class IssueServiceImpl implements IssueService {

  private final IssueRepository issueRepository;

  @Override
  @Transactional
  public void save(Issue issue) {
    issueRepository.save(issue);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Issue> findByScn(String scn) {
    return issueRepository.findById(scn);
  }

  @Override
  @Transactional(readOnly = true)
  public Long count(IssueSearchCriteria criteria) {
    return issueRepository.count(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Issue> search(IssueSearchCriteria criteria) {
    return issueRepository.search(criteria);
  }

  @Override
  @Transactional
  public void deleteMultiple(IssueSearchCriteria criteria) {
    search(criteria).forEach(this::delete);
  }

  @Override
  @Transactional
  public void delete(Issue issue) {
    issueRepository.delete(issue);
  }

  @Override
  @Transactional
  public void deleteHistoryByScnAndDate(String scn, OffsetDateTime createdDate) {
    issueRepository.deleteHistoryByScnAndDate(scn, createdDate);
  }

  @Override
  @Transactional
  public void deleteByScn(String scn) {
    issueRepository.deleteByScn(scn);
  }

  @Override
  @Transactional(readOnly = true)
  public long findCountOfBackOrdersForGbofProcessing(String documentNumber) {
    return issueRepository.findCountOfBackOrdersForGbofProcessing(documentNumber);
  }

  @Override
  @Transactional(readOnly = true)
  public long getHistoryCount(String docNumber, String suffix, String issueType) {
    return issueRepository.getHistoryCount(docNumber, suffix, issueType);
  }

  @Override
  @Transactional
  public String getNextIssueScn(boolean isWalkThru) {
    val sequenceNumber = issueRepository.getNextIssueSequence();

    String scn = "";
    int r = sequenceNumber;
    int sqM;
    int i = 1;
    while (i++ <= 4) {
      sqM = r % 36;
      if (sqM <= 9)
        scn = (char) (sqM + 48) + scn;
      else
        scn = (char) (sqM + 55) + scn;
      r = Math.abs(r / 36);
    }

    String prefix = "P";
    if (isWalkThru)
      prefix = "W";
    scn = prefix + Util.getCurrentJulian(4) + scn;
    return scn;
  }
}
