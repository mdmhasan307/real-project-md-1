package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.SiteSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.QSiteEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.SiteEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.apache.commons.collections.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
public class SiteEntityRepositoryCustomImpl implements SiteEntityRepositoryCustom {

  @PersistenceContext(unitName = GlobalConstants.COMMON_PERSISTENCE_UNIT)
  private EntityManager entityManager;

  @Override
  public List<SiteEntity> search(SiteSearchCriteria criteria) {
    val qEntity = QSiteEntity.siteEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(SiteSearchCriteria criteria, QSiteEntity qSiteEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    if (CollectionUtils.isNotEmpty(criteria.getSitesAvailable()))
      expressions.append(qSiteEntity.siteName.in(criteria.getSitesAvailable()));
    match(qSiteEntity.statusId, criteria.getStatusId(), expressions);
    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(SiteSearchCriteria criteria, QSiteEntity qSiteEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qSiteEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();
      EntityPathBase<?> qEntity = qSiteEntity;

      sort.setPath(qEntity);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}