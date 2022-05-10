package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QReceiptEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ReceiptEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class ReceiptEntityRepositoryCustomImpl implements ReceiptEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ReceiptEntity> search(ReceiptSearchCriteria criteria) {
    val qEntity = QReceiptEntity.receiptEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  @Override
  public Long count(ReceiptSearchCriteria criteria) {
    val qEntity = QReceiptEntity.receiptEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetchCount();
  }

  private Predicate createSearchPredicate(ReceiptSearchCriteria criteria, QReceiptEntity qReceiptEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qReceiptEntity.documentNumber, criteria.getDocumentNumber(), expressions);

    match(qReceiptEntity.suffix, criteria.getSuffix(), expressions);
    if (criteria.getCheckSuffixNull() != null && criteria.getCheckSuffixNull()) {
      expressions.append(qReceiptEntity.suffix.isNull());
    }
    else if (criteria.getCheckSuffixNull() != null && !criteria.getCheckSuffixNull()) {
      expressions.append(qReceiptEntity.suffix.isNotNull());
    }

    match(qReceiptEntity.status, criteria.getStatus(), expressions);

    if (criteria.getDocumentIdMatch() == null || criteria.getDocumentIdMatch().booleanValue()) {
      match(qReceiptEntity.documentId, criteria.getDocumentId(), expressions);
    }
    else {
      notmatch(qReceiptEntity.documentId, criteria.getDocumentId(), expressions);
    }

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(ReceiptSearchCriteria criteria, QReceiptEntity qReceiptEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qReceiptEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();
      EntityPathBase<?> qEntity = qReceiptEntity;

      sort.setPath(qEntity);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;

  }
}