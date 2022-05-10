package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.InventoryItemSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.InventoryItemEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QInventoryItemEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class InventoryItemEntityRepositoryCustomImpl implements InventoryItemEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Long count(InventoryItemSearchCriteria criteria) {
    val qInventoryItem = QInventoryItemEntity.inventoryItemEntity;
    val query = selectFrom(qInventoryItem, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qInventoryItem));
    return query.fetchCount();
  }

  @Override
  public List<InventoryItemEntity> search(InventoryItemSearchCriteria criteria) {
    val qInventoryItem = QInventoryItemEntity.inventoryItemEntity;

    val query = selectFrom(qInventoryItem, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qInventoryItem));
    configureOrderBy(query, createSortPairs(criteria, qInventoryItem));
    configureLimits(query, criteria);
    return query.fetch();
  }

  private Predicate createSearchPredicate(InventoryItemSearchCriteria criteria, QInventoryItemEntity qInventoryItem) {

    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qInventoryItem.wac().wacId, criteria.getWacId(), expressions);
    match(qInventoryItem.inventoryId, criteria.getInventoryItemId(), expressions);

    if (!CollectionUtils.isEmpty(criteria.getSides())) {
      expressions.append(qInventoryItem.location().side.in(criteria.getSides()));
    }

    match(qInventoryItem.location().locationId, criteria.getLocationId(), expressions);
    match(qInventoryItem.niinId, criteria.getNiinId(), expressions);
    match(qInventoryItem.niinLocation().niinLocationId, criteria.getNiinLocationId(), expressions);

    if (criteria.getAssignedUserId() != null) {
      val assignUserExpression = BooleanExpressionBuilder.of(SearchTypeEnum.OR);
      match(qInventoryItem.assignToUser, criteria.getAssignedUserId(), assignUserExpression);

      val nullableUserCheckExpression = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
      if (criteria.isAllowNullAssignedUserIds()) {
        nullableUserCheckExpression.append(qInventoryItem.assignToUser.isNull());
        //if we don't allow self recounts, then we limit to any where the numCounts is 0 or any with a numCount > 0 and user wasn't the one that completed.
        if (!criteria.isAllowSelfRecounts()) {
          nullableUserCheckExpression.append(qInventoryItem.numCounts.eq(0)
              .or(qInventoryItem.numCounts.gt(0)
                  .and(qInventoryItem.completedBy.ne(criteria.getAssignedUserId())
                      .or(qInventoryItem.completedBy.isNull()))));
        }
        assignUserExpression.append(nullableUserCheckExpression.getExpression());
      }

      expressions.append(assignUserExpression.getExpression());
    }

    if (!CollectionUtils.isEmpty(criteria.getStatuses())) {
      expressions.append(qInventoryItem.status.in(criteria.getStatuses()));
    }
    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(InventoryItemSearchCriteria criteria, QInventoryItemEntity qInventoryItem) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qInventoryItem);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();
      EntityPathBase<?> qEntity = qInventoryItem;

      sort.setPath(qEntity);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}