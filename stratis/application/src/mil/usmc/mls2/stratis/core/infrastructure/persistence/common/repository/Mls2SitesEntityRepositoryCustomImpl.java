package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.Mls2SitesSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.Mls2SitesEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.QMls2SitesEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
public class Mls2SitesEntityRepositoryCustomImpl implements Mls2SitesEntityRepositoryCustom {

  @PersistenceContext(unitName = GlobalConstants.COMMON_PERSISTENCE_UNIT)
  private EntityManager entityManager;

  @Override
  public Long count(Mls2SitesSearchCriteria criteria) {
    val qEntity = QMls2SitesEntity.mls2SitesEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    return query.fetchCount();
  }

  @Override
  public List<Mls2SitesEntity> search(Mls2SitesSearchCriteria criteria) {
    val qEntity = QMls2SitesEntity.mls2SitesEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(Mls2SitesSearchCriteria criteria, QMls2SitesEntity qMls2SitesEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qMls2SitesEntity.statusId, criteria.getStatusId(), expressions);
    match(qMls2SitesEntity.siteName, criteria.getSiteName(), expressions);
    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(Mls2SitesSearchCriteria criteria, QMls2SitesEntity qMls2SitesEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qMls2SitesEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();
      EntityPathBase<?> qEntity = qMls2SitesEntity;

      sort.setPath(qEntity);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}