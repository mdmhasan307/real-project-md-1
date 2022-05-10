package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogDasfRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.QImportFileLogDasfRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class ImportFileLogDasfRptViewEntityRepositoryCustomImpl implements ImportFileLogDasfRptViewEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ImportFileLogDasfRptViewEntity> search(ImportFileLogDasfRptViewSearchCriteria criteria) {
    val qEntity = QImportFileLogDasfRptViewEntity.importFileLogDasfRptViewEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(ImportFileLogDasfRptViewSearchCriteria criteria, QImportFileLogDasfRptViewEntity qImportFileLogDasfRptViewEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    goe(qImportFileLogDasfRptViewEntity.createdDate, criteria.getStartDate(), expressions);
    loe(qImportFileLogDasfRptViewEntity.createdDate, criteria.getEndDate(), expressions);

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(ImportFileLogDasfRptViewSearchCriteria criteria, QImportFileLogDasfRptViewEntity qImportFileLogDasfRptViewEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qImportFileLogDasfRptViewEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.property();
      sort = sort.toBuilder().path(qImportFileLogDasfRptViewEntity).property(sortColumn).build();
      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}
