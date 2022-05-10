package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogMhifRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.QImportFileLogMhifRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class ImportFileLogMhifRptViewEntityRepositoryCustomImpl implements ImportFileLogMhifRptViewEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ImportFileLogMhifRptViewEntity> search(ImportFileLogMhifRptViewSearchCriteria criteria) {
    val qEntity = QImportFileLogMhifRptViewEntity.importFileLogMhifRptViewEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(ImportFileLogMhifRptViewSearchCriteria criteria, QImportFileLogMhifRptViewEntity qImportFileLogMhifRptViewEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    goe(qImportFileLogMhifRptViewEntity.createdDate, criteria.getStartDate(), expressions);
    loe(qImportFileLogMhifRptViewEntity.createdDate, criteria.getEndDate(), expressions);

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(ImportFileLogMhifRptViewSearchCriteria criteria, QImportFileLogMhifRptViewEntity qImportFileLogMhifRptViewEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qImportFileLogMhifRptViewEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.property();
      sort = sort.toBuilder().path(qImportFileLogMhifRptViewEntity).property(sortColumn).build();
      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}
