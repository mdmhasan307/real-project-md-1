package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.InterfaceTransactionRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.QInterfaceTransactionRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class InterfaceTransactionRptViewEntityRepositoryCustomImpl implements InterfaceTransactionRptViewEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<InterfaceTransactionRptViewEntity> search(InterfaceTransactionRptViewSearchCriteria criteria) {
    val qEntity = QInterfaceTransactionRptViewEntity.interfaceTransactionRptViewEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(InterfaceTransactionRptViewSearchCriteria criteria, QInterfaceTransactionRptViewEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);

    like(qEntity.transactionType, criteria.getTransactionType(), expressions);
    like(qEntity.docNumber, criteria.getDocNumber(), expressions);
    match(qEntity.route, criteria.getRoute(), expressions);
    like(qEntity.niin, criteria.getNiin(), expressions);
    match(qEntity.ui, criteria.getUi(), expressions);
    match(qEntity.qty, criteria.getQty(), expressions);
    match(qEntity.cc, criteria.getCc(), expressions);

    goe(qEntity.date, criteria.getStartDate(), expressions);
    loe(qEntity.date, criteria.getEndDate(), expressions);

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(InterfaceTransactionRptViewSearchCriteria criteria, QInterfaceTransactionRptViewEntity qEntity) {
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
