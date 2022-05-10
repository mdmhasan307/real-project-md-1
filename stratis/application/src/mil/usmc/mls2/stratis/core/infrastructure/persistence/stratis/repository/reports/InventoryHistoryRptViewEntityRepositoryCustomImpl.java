package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.InventoryHistoryRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.QInventoryHistoryRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class InventoryHistoryRptViewEntityRepositoryCustomImpl implements InventoryHistoryRptViewEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<InventoryHistoryRptViewEntity> search(InventoryHistoryRptViewSearchCriteria criteria) {
    val qEntity = QInventoryHistoryRptViewEntity.inventoryHistoryRptViewEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(InventoryHistoryRptViewSearchCriteria criteria, QInventoryHistoryRptViewEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    goe(qEntity.completedDate, criteria.getStartDate(), expressions);
    loe(qEntity.completedDate, criteria.getEndDate(), expressions);

    match(qEntity.niin, criteria.getNiin(), expressions);
    match(qEntity.wac, criteria.getWac(), expressions);
    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(InventoryHistoryRptViewSearchCriteria criteria, QInventoryHistoryRptViewEntity qEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.property();
      sort = sort.toBuilder().path(qEntity).property(sortColumn).build();
      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}
