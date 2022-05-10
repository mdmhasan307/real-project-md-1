package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.QReceiptHistRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ReceiptHistRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class ReceiptHistRptViewEntityRepositoryCustomImpl implements ReceiptHistRptViewEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ReceiptHistRptViewEntity> search(ReceiptHistRptViewSearchCriteria criteria) {
    val qEntity = QReceiptHistRptViewEntity.receiptHistRptViewEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(ReceiptHistRptViewSearchCriteria criteria, QReceiptHistRptViewEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    goe(qEntity.dateReceived, criteria.getStartDate(), expressions);
    loe(qEntity.dateReceived, criteria.getEndDate(), expressions);

    match(qEntity.id().rcn, criteria.getRcn(), expressions);
    match(qEntity.niin, criteria.getNiin(), expressions);
    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(ReceiptHistRptViewSearchCriteria criteria, QReceiptHistRptViewEntity qEntity) {
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
