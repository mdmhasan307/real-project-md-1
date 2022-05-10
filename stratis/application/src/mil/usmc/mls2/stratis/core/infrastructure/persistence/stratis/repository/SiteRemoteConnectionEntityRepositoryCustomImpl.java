package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnectionSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QSiteRemoteConnectionEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteRemoteConnectionEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@SuppressWarnings("Duplicates")
public class SiteRemoteConnectionEntityRepositoryCustomImpl implements SiteRemoteConnectionEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Set<SiteRemoteConnectionEntity> search(SiteRemoteConnectionSearchCriteria criteria) {
    val qsiteRemoteConnection = QSiteRemoteConnectionEntity.siteRemoteConnectionEntity;
    val query = selectFrom(qsiteRemoteConnection, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qsiteRemoteConnection));
    configureOrderBy(query, createSortPairs(criteria, qsiteRemoteConnection));
    return new HashSet<>(query.fetch());
  }

  private Predicate createSearchPredicate(SiteRemoteConnectionSearchCriteria criteria, QSiteRemoteConnectionEntity qSiteRemoteConnectionEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);

    match(qSiteRemoteConnectionEntity.connectionId, criteria.getConnectionId(), expressions);
    match(qSiteRemoteConnectionEntity.hostName, criteria.getHostName(), expressions);

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(SiteRemoteConnectionSearchCriteria criteria, QSiteRemoteConnectionEntity qSiteRemoteConnectionEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qSiteRemoteConnectionEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();

      sort.setPath(qSiteRemoteConnectionEntity);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}