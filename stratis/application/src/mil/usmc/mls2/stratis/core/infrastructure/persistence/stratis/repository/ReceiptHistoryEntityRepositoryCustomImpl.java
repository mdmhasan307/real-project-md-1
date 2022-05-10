package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistorySearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QReceiptHistoryEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ReceiptHistoryEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class ReceiptHistoryEntityRepositoryCustomImpl implements ReceiptHistoryEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ReceiptHistoryEntity> search(ReceiptHistorySearchCriteria criteria) {
    val qEntity = QReceiptHistoryEntity.receiptHistoryEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  @Override
  public Long count(ReceiptHistorySearchCriteria criteria) {
    val qEntity = QReceiptHistoryEntity.receiptHistoryEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetchCount();
  }

  private Predicate createSearchPredicate(ReceiptHistorySearchCriteria criteria, QReceiptHistoryEntity qReceiptHistoryEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qReceiptHistoryEntity.documentNumber, criteria.getDocumentNumber(), expressions);

    match(qReceiptHistoryEntity.suffix, criteria.getSuffix(), expressions);
    if (criteria.getCheckSuffixNull() != null && criteria.getCheckSuffixNull()) {
      expressions.append(qReceiptHistoryEntity.suffix.isNull());
    }
    else if (criteria.getCheckSuffixNull() != null && !criteria.getCheckSuffixNull()) {
      expressions.append(qReceiptHistoryEntity.suffix.isNotNull());
    }

    match(qReceiptHistoryEntity.status, criteria.getStatus(), expressions);

    if (criteria.getDocumentIdMatch() == null || criteria.getDocumentIdMatch().booleanValue()) {
      match(qReceiptHistoryEntity.documentId, criteria.getDocumentId(), expressions);
    }
    else {
      notmatch(qReceiptHistoryEntity.documentId, criteria.getDocumentId(), expressions);
    }

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(ReceiptHistorySearchCriteria criteria, QReceiptHistoryEntity qReceiptHistoryEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qReceiptHistoryEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();
      EntityPathBase<?> qEntity = qReceiptHistoryEntity;

      sort.setPath(qEntity);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }
    return filteredSorts;
  }

}
