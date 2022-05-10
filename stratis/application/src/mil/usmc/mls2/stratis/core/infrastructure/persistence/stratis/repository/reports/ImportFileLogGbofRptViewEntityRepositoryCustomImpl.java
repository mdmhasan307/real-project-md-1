package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogGbofRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.QImportFileLogGbofRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class ImportFileLogGbofRptViewEntityRepositoryCustomImpl implements ImportFileLogGbofRptViewEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ImportFileLogGbofRptViewEntity> search(ImportFileLogGbofRptViewSearchCriteria criteria) {
    val qEntity = QImportFileLogGbofRptViewEntity.importFileLogGbofRptViewEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(ImportFileLogGbofRptViewSearchCriteria criteria, QImportFileLogGbofRptViewEntity qImportFileLogGbofRptViewEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    goe(qImportFileLogGbofRptViewEntity.createdDate, criteria.getStartDate(), expressions);
    loe(qImportFileLogGbofRptViewEntity.createdDate, criteria.getEndDate(), expressions);

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(ImportFileLogGbofRptViewSearchCriteria criteria, QImportFileLogGbofRptViewEntity qImportFileLogGbofRptViewEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qImportFileLogGbofRptViewEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.property();
      sort = sort.toBuilder().path(qImportFileLogGbofRptViewEntity).property(sortColumn).build();
      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}
