package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.PickingSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PickingEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QIssueEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QPickingEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class PickingEntityRepositoryCustomImpl implements PickingEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Long count(PickingSearchCriteria criteria) {
    val qPicking = QPickingEntity.pickingEntity;
    val query = selectFrom(qPicking, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qPicking));
    return query.fetchCount();
  }

  @Override
  public List<PickingEntity> search(PickingSearchCriteria criteria) {
    val qPicking = QPickingEntity.pickingEntity;
    val query = selectFrom(qPicking, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qPicking));
    configureOrderBy(query, createSortPairs(criteria, qPicking));
    configureLimits(query, criteria);
    return query.fetch();
  }

  public List<PickingEntity> getPicksForProcessing(PickingSearchCriteria criteria) {
    val qPicking = QPickingEntity.pickingEntity;
    val qIssue = QIssueEntity.issueEntity;
    val query = selectFrom(qPicking, entityManager).leftJoin(qIssue).on(qPicking.scn.eq(qIssue.scn));

    configurePredicate(query, createSearchPredicate(criteria, qPicking));

    val picksForProcessingExpressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);

    if (!CollectionUtils.isEmpty(criteria.getSides())) {
      picksForProcessingExpressions.append(qPicking.niinLocation().location().side.in(criteria.getSides()));
    }

    match(qPicking.scn, criteria.getScn(), picksForProcessingExpressions);
    match(qIssue.customer().aac, criteria.getAac(), picksForProcessingExpressions);
    match(qIssue.issuePriorityGroup, criteria.getPriority(), picksForProcessingExpressions);
    match(qIssue.customer().shippingRoute().routeId, criteria.getRouteId(), picksForProcessingExpressions);
    match(qPicking.niinLocation().niinLocationId, criteria.getNiinLocationId(), picksForProcessingExpressions);

    configurePredicate(query, picksForProcessingExpressions.getExpression());
    configureOrderBy(query, createSortPairs(criteria, qPicking));
    configureLimits(query, criteria);

    return query.fetch();
  }

  private Predicate createSearchPredicate(PickingSearchCriteria criteria, QPickingEntity qPicking) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qPicking.niinLocation().location().wac().wacId, criteria.getWacId(), expressions);
    match(qPicking.niinLocation().niinLocationId, criteria.getNiinLocationId(), expressions);
    match(qPicking.pid, criteria.getPid(), expressions);
    match(qPicking.pin, criteria.getPin(), expressions);
    match(qPicking.scn, criteria.getScn(), expressions);
    match(qPicking.packingConsolidationId, criteria.getPackingConsolidationId(), expressions);

    //if assignedToUser is specified look for any records for the user, or unassigned records.
    if (criteria.getAssignedUserId() != null) {
      val assignUserExpression = BooleanExpressionBuilder.of(SearchTypeEnum.OR);
      match(qPicking.assignToUser, criteria.getAssignedUserId(), assignUserExpression);
      if (criteria.isAllowNullAssignedUserIds()) {
        assignUserExpression.append(qPicking.assignToUser.isNull());
      }
      expressions.append(assignUserExpression.getExpression());
    }

    if (!CollectionUtils.isEmpty(criteria.getStatuses())) {
      expressions.append(qPicking.status.in(criteria.getStatuses()));
    }

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(PickingSearchCriteria criteria, QPickingEntity qPicking) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qPicking);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();
      sort.setPath(qPicking);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}
