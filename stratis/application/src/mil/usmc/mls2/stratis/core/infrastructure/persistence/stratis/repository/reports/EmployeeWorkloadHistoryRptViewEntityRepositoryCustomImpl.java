package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.EmployeeWorkloadHistoryRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.QEmployeeWorkloadHistoryRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class EmployeeWorkloadHistoryRptViewEntityRepositoryCustomImpl implements EmployeeWorkloadHistoryRptViewEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<EmployeeWorkloadHistoryRptViewEntity> search(EmployeeWorkloadHistoryRptViewSearchCriteria criteria) {
    val qEntity = QEmployeeWorkloadHistoryRptViewEntity.employeeWorkloadHistoryRptViewEntity;

    // FUTURE review this for a better/cleaner way.  But this is fully functional
    JPAQuery<Tuple> query = new JPAQuery<>(entityManager).select(qEntity.id().user, qEntity.receiptCount.sum(), qEntity.receiptsDollarValue.sum(), qEntity.stowCount.sum(), qEntity.stowDollarValue.sum(),
        qEntity.pickCount.sum(), qEntity.pickDollarValue.sum(), qEntity.packConsolidationCount.sum(), qEntity.packDollarValue.sum(),
        qEntity.inventoryItemCount.sum(), qEntity.inventoryDollarValue.sum(), qEntity.otherCount.sum())
        .from(qEntity);

    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    query.groupBy(qEntity.id().user);
    val results = query.fetch();

    val convertedResults = new ArrayList<EmployeeWorkloadHistoryRptViewEntity>();
    results.forEach(x -> {
      val employeeWorkload = EmployeeWorkloadHistoryRptViewEntity.builder()
          .id(new EmployeeWorkloadHistoryRptViewEntity.EmployeeWorkloadHistoryKey(x.get(qEntity.id().user), null))
          .inventoryDollarValue(x.get(qEntity.inventoryDollarValue.sum()))
          .inventoryItemCount(x.get(qEntity.inventoryItemCount.sum()))
          .receiptCount(x.get(qEntity.receiptCount.sum()))
          .receiptsDollarValue(x.get(qEntity.receiptsDollarValue.sum()))
          .stowCount(x.get(qEntity.stowCount.sum()))
          .stowDollarValue(x.get(qEntity.stowDollarValue.sum()))
          .pickCount(x.get(qEntity.pickCount.sum()))
          .pickDollarValue(x.get(qEntity.pickDollarValue.sum()))
          .packConsolidationCount(x.get(qEntity.packConsolidationCount.sum()))
          .packDollarValue(x.get(qEntity.packDollarValue.sum()))
          .otherCount(x.get(qEntity.otherCount.sum()))
          .build();
      convertedResults.add(employeeWorkload);
    });

    return convertedResults;
  }

  private Predicate createSearchPredicate(EmployeeWorkloadHistoryRptViewSearchCriteria criteria, QEmployeeWorkloadHistoryRptViewEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    goe(qEntity.id().transDate, criteria.getStartDate(), expressions);
    loe(qEntity.id().transDate, criteria.getEndDate(), expressions);

    match(qEntity.id().user, criteria.getUser(), expressions);
    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(EmployeeWorkloadHistoryRptViewSearchCriteria criteria, QEmployeeWorkloadHistoryRptViewEntity qEntity) {
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
