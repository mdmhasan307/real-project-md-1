package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.IssueEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface IssueEntityRepository extends EntityRepository<IssueEntity, String>, IssueEntityRepositoryCustom {

  @Query(value = "select sum(issue_count) from (select count(1) as issue_count from issue_hist where issue_type = 'B' AND status <> 'BACKORDER' and document_number=:docNumber and rownum = 1 union select count(1) as issue_count from issue where issue_type = 'B' and document_number=:docNumber and rownum =1)",
      nativeQuery = true)
  long findCountOfBackOrdersForGbofProcessing(@Param("docNumber") String docNumber);

  @Query(value = "select count(1) from issue_hist where document_number=:docNumber AND (:issueType is NULL OR (issue_type is NULL OR issue_type = :issueType)) and (:suffix is null or suffix=:suffix)",
      nativeQuery = true)
  long getHistoryCount(@Param("docNumber") String docNumber, @Param("suffix") String suffix, @Param("issueType") String issueType);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(value = "delete from issue_hist where scn=:scn AND created_date > :createdDate",
      nativeQuery = true)
  void deleteHistoryByScnAndDate(@Param("scn") String scn, @Param("createdDate") OffsetDateTime createdDate);

  @Query(value = "select ISSUE_SCN_SEQ.nextval from dual", nativeQuery = true)
  Integer getNextIssueSequence();
}
