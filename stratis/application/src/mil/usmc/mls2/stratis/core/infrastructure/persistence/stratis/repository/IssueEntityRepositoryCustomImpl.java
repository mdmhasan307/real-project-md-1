package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.IssueSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.IssueEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QIssueEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class IssueEntityRepositoryCustomImpl implements IssueEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public long count(IssueSearchCriteria criteria) {
    val qIssue = QIssueEntity.issueEntity;

    val query = selectFrom(qIssue, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qIssue));
    return query.fetchCount();
  }

  @Override
  public List<IssueEntity> search(IssueSearchCriteria criteria) {
    val qIssue = QIssueEntity.issueEntity;

    val query = selectFrom(qIssue, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qIssue));
    return query.fetch();
  }

  private Predicate createSearchPredicate(IssueSearchCriteria criteria, QIssueEntity qIssue) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qIssue.scn, criteria.getScn(), expressions);

    if (!CollectionUtils.isEmpty(criteria.getScns())) {
      val scnExpression = BooleanExpressionBuilder.of(SearchTypeEnum.OR);
      criteria.getScns().forEach(x -> match(qIssue.scn, x, scnExpression));
      expressions.append(scnExpression.getExpression());
    }

    match(qIssue.status, criteria.getStatus(), expressions);
    match(qIssue.suffix, criteria.getSuffix(), expressions);
    match(qIssue.documentNumber, criteria.getDocumentNumber(), expressions);

    val issueTypeExpressions = BooleanExpressionBuilder.of(SearchTypeEnum.OR);
    match(qIssue.issueType, criteria.getIssueType(), issueTypeExpressions);
    if (criteria.isAllowNullIssueType()) {
      issueTypeExpressions.append(qIssue.issueType.isNull());
    }
    expressions.append(issueTypeExpressions.getExpression());

    if (criteria.getCreatedBefore() != null) {
      expressions.append(qIssue.createdDate.lt(criteria.getCreatedBefore()));
    }

    return expressions.getExpression();
  }
}