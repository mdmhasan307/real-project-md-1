package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogMatsRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.QImportFileLogMatsRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class ImportFileLogMatsRptViewEntityRepositoryCustomImpl implements ImportFileLogMatsRptViewEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ImportFileLogMatsRptViewEntity> search(ImportFileLogMatsRptViewSearchCriteria criteria) {
    val qEntity = QImportFileLogMatsRptViewEntity.importFileLogMatsRptViewEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(ImportFileLogMatsRptViewSearchCriteria criteria, QImportFileLogMatsRptViewEntity qImportFileLogMatsRptViewEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    goe(qImportFileLogMatsRptViewEntity.createdDate, criteria.getStartDate(), expressions);
    loe(qImportFileLogMatsRptViewEntity.createdDate, criteria.getEndDate(), expressions);

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(ImportFileLogMatsRptViewSearchCriteria criteria, QImportFileLogMatsRptViewEntity qImportFileLogMatsRptViewEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qImportFileLogMatsRptViewEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.property();
      sort = sort.toBuilder().path(qImportFileLogMatsRptViewEntity).property(sortColumn).build();
      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}
